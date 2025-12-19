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
    // ✅ TÁCH RIÊNG 2 file JSON
    private val templatesFileName = "templates.json"        // Templates gốc từ assets
    private val customizedFileName = "customized.json"      // Characters đã customize

    companion object {
        private const val TAG = "AppDataManager"
        private const val ASSET_PREFIX = "file:///android_asset"
    }

    // ✅ Templates (chỉ đọc từ assets, không thay đổi)
    private val _templates = MutableStateFlow<List<CustomModel>>(emptyList())
    val templates: StateFlow<List<CustomModel>> = _templates.asStateFlow()

    // ✅ Customized characters (đã lưu)
    private val _customizedCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val customizedCharacters: StateFlow<List<CustomModel>> = _customizedCharacters.asStateFlow()

    // ✅ Combined list (templates + customized) để hiển thị
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
     * Load data lần đầu
     */
    suspend fun loadInitialData() {
        if (isDataLoaded) return
        _isLoading.value = true
        _error.value = null

        withContext(Dispatchers.IO) {
            try {
                // ✅ Load templates từ assets (hoặc cache)
                val savedTemplates = loadTemplatesFromJson()
                if (savedTemplates.isNotEmpty()) {
                    _templates.value = savedTemplates
                    Log.d(TAG, "✅ Loaded templates from cache")
                } else {
                    loadTemplatesFromAssets()
                }

                // ✅ Load customized characters từ JSON
                val customized = loadCustomizedFromJson()
                _customizedCharacters.value = customized
                Log.d(TAG, "✅ Loaded ${customized.size} customized characters")

                // ✅ Combine lists
                updateCharactersList()

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

    /**
     * ✅ Combine templates + customized characters
     */
    private fun updateCharactersList() {
        val combined = _templates.value + _customizedCharacters.value
        _characters.value = combined
        Log.d(TAG, "📋 Total characters: ${combined.size} (${_templates.value.size} templates + ${_customizedCharacters.value.size} customized)")
    }

    /**
     * Load templates từ assets (chỉ load 1 lần)
     */
    private suspend fun loadTemplatesFromAssets() {
        try {
            val assetManager = context.assets
            val result = arrayListOf<CustomModel>()
            val folders = assetManager.list("data") ?: return

            for (folder in folders) {
                val basePath = "data/$folder"
                val itemsRaw = assetManager.list(basePath) ?: continue

                val items = itemsRaw.sortedBy { item ->
                    item.substringBefore("-").toIntOrNull() ?: 999
                }

                Log.d(TAG, "=== Loading template folder: $folder ===")

                val bodyParts = arrayListOf<BodyPartModel>()
                var avatar = ""

                for (item in items) {
                    val fullPath = "$basePath/$item"
                    val contents = assetManager.list(fullPath)

                    if (contents.isNullOrEmpty()) {
                        avatar = "$ASSET_PREFIX/$fullPath"
                        Log.d(TAG, "  📷 Avatar: $item")
                        continue
                    }

                    Log.d(TAG, "  📦 Processing item: $item")

                    val nav = contents.firstOrNull { it.startsWith("nav.") }
                        ?.let { "$ASSET_PREFIX/$fullPath/$it" } ?: ""
                    val colors = arrayListOf<ColorModel>()

                    contents.filter { !it.startsWith("nav.") }.forEach { layer ->
                        val layerPath = "$fullPath/$layer"
                        val files = assetManager.list(layerPath)
                        if (files.isNullOrEmpty()) {
                            colors.add(ColorModel("", arrayListOf("$ASSET_PREFIX/$layerPath")))
                        } else {
                            val paths = files.map { "$ASSET_PREFIX/$layerPath/$it" }
                            colors.add(ColorModel(layer, ArrayList(paths)))
                        }
                    }

                    processColorDefaults(colors, item)
                    bodyParts.add(BodyPartModel(nav, colors))
                }

                Log.d(TAG, "✅ Template '$folder' loaded with ${bodyParts.size} parts")

                // ✅ Template với ID cố định (dùng folder name)
                result.add(
                    CustomModel(
                        id = "template_$folder",  // ✅ ID cố định cho template
                        avatar = avatar,
                        listPath = ArrayList(bodyParts),
                        selections = arrayListOf(),
                        imageSave = ""
                    )
                )
            }

            _templates.value = result
            saveTemplatesToJson(result)  // Cache lại
            Log.d(TAG, "✅ Loaded ${result.size} templates from assets")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi loadTemplatesFromAssets: ${e.message}", e)
        }
    }

    private fun processColorDefaults(colors: ArrayList<ColorModel>, itemName: String) {
        try {
            val position = itemName.substringBefore("-").toIntOrNull() ?: return

            colors.forEach { colorModel ->
                val originalSize = colorModel.listPath.size

                when {
                    position == 1 -> {
                        if (colorModel.listPath.firstOrNull() != "dice") {
                            colorModel.listPath.add(0, "dice")
                        }
                    }
                    else -> {
                        if (colorModel.listPath.firstOrNull() != "none") {
                            colorModel.listPath.add(0, "none")
                            colorModel.listPath.add(1, "dice")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi processColorDefaults", e)
        }}
    /**
     * ✅ Save/Load templates cache
     */
    private suspend fun saveTemplatesToJson(templates: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(templates)
                val file = File(context.filesDir, templatesFileName)
                file.writeText(json)
                Log.d(TAG, "✅ Templates cached to JSON")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi saveTemplatesToJson: ${e.message}", e)
            }
        }
    }

    private suspend fun loadTemplatesFromJson(): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, templatesFileName)
                if (!file.exists()) return@withContext emptyList()
                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                Gson().fromJson<List<CustomModel>>(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi loadTemplatesFromJson: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * ✅ Save/Load customized characters
     */
    private suspend fun saveCustomizedToJson(characters: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(characters)
                val file = File(context.filesDir, customizedFileName)
                file.writeText(json)
                Log.d(TAG, "✅ Customized characters saved (${characters.size} items)")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi saveCustomizedToJson: ${e.message}", e)
            }
        }
    }

    private suspend fun loadCustomizedFromJson(): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, customizedFileName)
                if (!file.exists()) return@withContext emptyList()
                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                Gson().fromJson<List<CustomModel>>(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi loadCustomizedFromJson: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * ✅ Update hoặc add customized character
     */
    suspend fun updateCustomizedCharacter(character: CustomModel) {
        val list = _customizedCharacters.value.toMutableList()
        val index = list.indexOfFirst { it.id == character.id }

        if (index >= 0) {
            list[index] = character
            Log.d(TAG, "✅ Updated customized character: ${character.id}")
        } else {
            list.add(character)
            Log.d(TAG, "✅ Added new customized character: ${character.id}")
        }

        _customizedCharacters.value = list
        updateCharactersList()
        saveCustomizedToJson(list)
    }

    /**
     * ✅ Delete customized character
     */
    suspend fun deleteCustomizedCharacter(characterId: String) {
        val list = _customizedCharacters.value.toMutableList()
        val removed = list.removeIf { it.id == characterId }

        if (removed) {
            Log.d(TAG, "✅ Deleted customized character: $characterId")
            _customizedCharacters.value = list
            updateCharactersList()
            saveCustomizedToJson(list)
        }
    }

    /**
     * ✅ Check if character is a template
     */
    fun isTemplate(characterId: String): Boolean {
        return characterId.startsWith("template_")
    }

    /**
     * ✅ Get character by index (from combined list)
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        return _characters.value.getOrNull(index)
    }

    /**
     * ✅ Get character by ID
     */
    fun getCharacterById(id: String): CustomModel? {
        return _characters.value.find { it.id == id }
    }

    suspend fun refreshFromApi() {
        _isLoading.value = true
        _error.value = null
        try {
            // TODO: Implement API call
            Log.d(TAG, "🔄 Refresh API thành công")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Lỗi refresh API", e)
            _error.value = "Không thể cập nhật: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun forceReloadAll() {
        isDataLoaded = false
        loadInitialData()
    }

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

    fun clearData() {
        _templates.value = emptyList()
        _customizedCharacters.value = emptyList()
        _characters.value = emptyList()
        _backgrounds.value = emptyList()
        _backgroundTexts.value = emptyList()
        _stickers.value = emptyList()
        _speechs.value = emptyList()
        isDataLoaded = false
    }
}