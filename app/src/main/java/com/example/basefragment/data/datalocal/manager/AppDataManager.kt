package com.example.basefragment.data.datalocal.manager

import android.content.Context
import android.util.Log
import com.example.basefragment.data.model.custom.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val charactersFileName = "characters.json"

    companion object {
        private const val TAG = "AppDataManager"
        private const val ASSET_PREFIX = "file:///android_asset/"
    }

    private val _characters = MutableStateFlow<List<CustomModel>>(emptyList())
    val characters: StateFlow<List<CustomModel>> = _characters.asStateFlow()

    private val _backgrounds = MutableStateFlow<List<String>>(emptyList())
    val backgrounds: StateFlow<List<String>> = _backgrounds.asStateFlow()

    private val _backgroundTexts = MutableStateFlow<List<String>>(emptyList())
    val backgroundTexts: StateFlow<List<String>> = _backgroundTexts.asStateFlow()

    private val _stickers = MutableStateFlow<List<String>>(emptyList())
    val stickers: StateFlow<List<String>> = _stickers.asStateFlow()

    private val _speechs = MutableStateFlow<List<String>>(emptyList())
    val speechs: StateFlow<List<String>> = _speechs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var isDataLoaded = false

    /**
     * Load data lần đầu, ưu tiên JSON nếu đã có
     */
    suspend fun loadInitialData() {
        if (isDataLoaded) return
        _isLoading.value = true
        _error.value = null

        withContext(Dispatchers.IO) {
            try {
                val savedCharacters = loadCharactersFromJson()
                if (savedCharacters.isNotEmpty()) {
                    _characters.value = savedCharacters
                    Log.d(TAG, "✅ Loaded characters from JSON")
                } else {
                    // Load từ assets nếu chưa có JSON
                    loadCharactersFromAssets()
                }

                // Load assets khác
                coroutineScope {
                    launch { loadBackgrounds() }
                    launch { loadBackgroundTexts() }
                    launch { loadStickers() }
                    launch { loadSpeechs() }
                }

                Log.d(TAG, "✅ Load initial data xong")
                isDataLoaded = true

            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi loadInitialData: ${e.message}", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadCharactersFromAssets() {
        try {
            val assetManager = context.assets
            val result = arrayListOf<CustomModel>()
            val folders = assetManager.list("data") ?: return

            for (folder in folders) {
                val basePath = "data/$folder"
                val itemsRaw = assetManager.list(basePath) ?: continue

                // 🔥 QUAN TRỌNG: Sort items theo position (số trước "-")
                // assetManager.list() không đảm bảo thứ tự!
                val items = itemsRaw.sortedBy { item ->
                    item.substringBefore("-").toIntOrNull() ?: 999
                }

                Log.d(TAG, "=== Loading character folder: $folder ===")
                Log.d(TAG, "   Raw items: ${itemsRaw.toList()}")
                Log.d(TAG, "   Sorted items: $items")

                val bodyParts = arrayListOf<BodyPartModel>()
                var avatar = ""

                for (item in items) {
                    val fullPath = "$basePath/$item"
                    val contents = assetManager.list(fullPath)

                    if (contents.isNullOrEmpty()) {
                        avatar = "$ASSET_PREFIX$fullPath"
                        Log.d(TAG, "  📷 Avatar: $item")
                        continue
                    }

                    Log.d(TAG, "  📦 Processing item: $item")

                    val nav = contents.firstOrNull { it.startsWith("nav.") }
                        ?.let { "$ASSET_PREFIX$fullPath/$it" } ?: ""
                    val colors = arrayListOf<ColorModel>()

                    contents.filter { !it.startsWith("nav.") }.forEach { layer ->
                        val layerPath = "$fullPath/$layer"
                        val files = assetManager.list(layerPath)
                        if (files.isNullOrEmpty()) {
                            colors.add(ColorModel("", arrayListOf("$ASSET_PREFIX$layerPath")))
                        } else {
                            val paths = files.map { "$ASSET_PREFIX$layerPath/$it" }
                            colors.add(ColorModel(layer, ArrayList(paths)))
                        }
                    }

                    processColorDefaults(colors, item)
                    bodyParts.add(BodyPartModel(nav, colors))
                }

                // 🔥 KHÔNG CẦN sort lại vì đã sort items ở trên rồi
                // bodyParts đã theo đúng thứ tự position: 1, 2, 3, ...

                Log.d(TAG, "✅ Character '$folder' loaded with ${bodyParts.size} parts")
                Log.d(TAG, "   📋 Body parts order:")
                bodyParts.forEachIndexed { idx, part ->
                    Log.d(TAG, "      [$idx] position=${part.position}, navOrder=${part.navOrder}, z-index=${part.zIndex}")
                }

                result.add(CustomModel(avatar = avatar, listPath = ArrayList(bodyParts)))
            }

            _characters.value = result
            saveCharactersToJson(result)
            Log.d(TAG, "✅ Loaded ${result.size} characters from assets")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadCharactersFromAssets: ${e.message}", e)
        }
    }


    private suspend fun loadDataFromApi() { // try { // val apiData = apiRepository.getCharacters() // Log.d(TAG, "✅ API call thành công") // } catch (e: Exception) { // Log.e(TAG, "⚠️ API error: ${e.message}") // }
    }

    suspend fun refreshFromApi() {
        _isLoading.value = true
        _error.value = null
        try {
            loadDataFromApi()
            Log.d(TAG, "🔄 Refresh API thành công")
        } catch (e: Exception) {
            Log.e(
                TAG, "❌ Lỗi refresh API", e
            )
            _error.value = "Không thể cập nhật: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun forceReloadAll() {
        isDataLoaded = false
        loadInitialData()
    }

    private fun processColorDefaults(colors: ArrayList<ColorModel>, itemName: String) {
        try {
            // 🔥 itemName format: "1-1", "2-4", "3-5"
            // → Lấy số TRƯỚC dấu "-" (position), KHÔNG phải số sau (z-index)
            val position = itemName.substringBefore("-").toIntOrNull() ?: return

            Log.d(TAG, " processColorDefaults: itemName=$itemName, position=$position")

            colors.forEach { colorModel ->
                val originalSize = colorModel.listPath.size

                when {
                    position == 1 -> {
                        // Position 1 → chỉ thêm "dice" (không có "none")
                        if (colorModel.listPath.firstOrNull() != "dice") {
                            colorModel.listPath.add(0, "dice")
                            Log.d(TAG, "       → Added 'dice' at position 1 (size: $originalSize → ${colorModel.listPath.size})")
                        }
                    }
                    else -> {
                        // Position khác → thêm cả "none" và "dice"
                        if (colorModel.listPath.firstOrNull() != "none") {
                            colorModel.listPath.add(0, "none")
                            colorModel.listPath.add(1, "dice")
                            Log.d(TAG, "       → Added 'none' and 'dice' at position $position (size: $originalSize → ${colorModel.listPath.size})")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi processColorDefaults", e)
        }
    }

    suspend fun saveCharactersToJson(characters: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(characters)
                val file = File(context.filesDir, charactersFileName)
                file.writeText(json)
                Log.d(TAG, "✅ Characters saved to JSON")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi saveCharactersToJson: ${e.message}", e)
            }
        }
    }

    suspend fun loadCharactersFromJson(): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, charactersFileName)
                if (!file.exists()) return@withContext emptyList()
                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                Gson().fromJson<List<CustomModel>>(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi loadCharactersFromJson: ${e.message}", e)
                emptyList()
            }
        }
    }

    suspend fun updateCharacter(character: CustomModel) {
        val list = _characters.value.toMutableList()
        val index = list.indexOfFirst { it.id == character.id }
        if (index >= 0) list[index] = character else list.add(character)
        _characters.value = list
        saveCharactersToJson(list)
    }

    suspend fun resetCharacter(characterId: String) {
        val list = _characters.value.toMutableList()
        val index = list.indexOfFirst { it.id == characterId }
        if (index >= 0) {
            val old = list[index]
            val resetChar = old.copy(
                listPath = ArrayList(old.listPath.map { it.copy(listPath = ArrayList(it.listPath)) }),
                updatedAt = System.currentTimeMillis()
            )
            list[index] = resetChar
            _characters.value = list
            saveCharactersToJson(list)
        }
    }

    suspend fun randomCharacter(characterId: String) {
        val list = _characters.value.toMutableList()
        val index = list.indexOfFirst { it.id == characterId }
        if (index >= 0) {
            val current = list[index]
            val shuffledParts =
                current.listPath.map { it.copy(listPath = ArrayList(it.listPath.shuffled())) }
            list[index] = current.copy(
                listPath = ArrayList(shuffledParts), updatedAt = System.currentTimeMillis()
            )
            _characters.value = list
            saveCharactersToJson(list)
        }
    }

    fun getCharacterByIndex(index: Int): CustomModel? = _characters.value.getOrNull(index)

    /**
     * --- Backgrounds / Stickers / Speechs ---
     */
    private suspend fun loadBackgrounds() {
        try {
            val files = context.assets.list("bg") ?: emptyArray()
            val sorted = files.sortedBy { it.substringBeforeLast(".").toIntOrNull() ?: 0 }
                .map { "$ASSET_PREFIX/bg/$it" }
            _backgrounds.value = listOf("") + sorted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadBackgrounds: ${e.message}", e)
        }
    }

    private suspend fun loadBackgroundTexts() {
        try {
            val files = context.assets.list("BG_Text") ?: emptyArray()
            _backgroundTexts.value = files.map { "$ASSET_PREFIX/BG_Text/$it" }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadBackgroundTexts: ${e.message}", e)
        }
    }

    private suspend fun loadStickers() {
        try {
            val files = context.assets.list("sticker") ?: emptyArray()
            _stickers.value = files.map { "$ASSET_PREFIX/sticker/$it" }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadStickers: ${e.message}", e)
        }
    }

    private suspend fun loadSpeechs() {
        try {
            val files = context.assets.list("BG_Text") ?: emptyArray()
            _speechs.value = files.map { "$ASSET_PREFIX/BG_Text/$it" }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadSpeechs: ${e.message}", e)
        }
    }

    /**
     * Clear all data
     */
    fun clearData() {
        _characters.value = emptyList()
        _backgrounds.value = emptyList()
        _backgroundTexts.value = emptyList()
        _stickers.value = emptyList()
        _speechs.value = emptyList()
        isDataLoaded = false
    }
}
