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

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.objectbox.BoxStore

class LazyTypeAdapter<T>(private val defaultValue: T) : TypeAdapter<Lazy<T>>() {
    override fun write(out: JsonWriter, value: Lazy<T>) {
        out.value(value.value.toString())  // Serialize giá trị
    }

    override fun read(`in`: JsonReader): Lazy<T> {
        val value = `in`.nextString()
        return lazyOf(value as T)  // Deserialize thành Lazy
    }
}

// 🔥 Tạo Gson với TypeAdapter
private val gson = GsonBuilder()
    .registerTypeAdapter(Lazy::class.java, LazyTypeAdapter<String>(""))  // Giả sử Lazy<String>, điều chỉnh nếu khác
    .create()
@Singleton
class AppDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppDataManager"
        private const val ASSET_PREFIX = "file:///android_asset"

        private const val MY_AVATARS_FILE = "my_avatars.json"
        private const val TEMPLATES_FILE = "templates.json"           // Templates từ API/Assets
        private const val CUSTOMIZED_FILE = "customized.json"         // Characters do user tạo
        private const val MY_DESIGNS_FILE = "my_designs.json"         // Saved images
    }
    private val gson = Gson()
    private var boxStore: BoxStore? = null
    // ==================== TEMPLATES (Read-only, từ API/Assets) ====================

    private val _templates = MutableStateFlow<List<CustomModel>>(emptyList())
    val templates: StateFlow<List<CustomModel>> = _templates.asStateFlow()

    // ==================== CUSTOMIZED CHARACTERS (User-created, persistent) ====================

    private val _myAvatars = MutableStateFlow<List<DataModel>>(emptyList())
    val myAvatars: StateFlow<List<DataModel>> = _myAvatars.asStateFlow()

    private val _customizedCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val customizedCharacters: StateFlow<List<CustomModel>> = _customizedCharacters.asStateFlow()

    // ==================== COMBINED LIST (Templates + Customized) ====================

    private val _characters = MutableStateFlow<List<CustomModel>>(emptyList())
    val characters: StateFlow<List<CustomModel>> = _characters.asStateFlow()

    // ==================== ASSETS DATA ====================

    private val _backgrounds = MutableStateFlow<List<String>>(emptyList())
    val backgrounds: StateFlow<List<String>> = _backgrounds.asStateFlow()

    private val _backgroundTexts = MutableStateFlow<List<String>>(emptyList())
    val backgroundTexts: StateFlow<List<String>> = _backgroundTexts.asStateFlow()

    private val _stickers = MutableStateFlow<List<String>>(emptyList())
    val stickers: StateFlow<List<String>> = _stickers.asStateFlow()

    private val _speechs = MutableStateFlow<List<String>>(emptyList())
    val speechs: StateFlow<List<String>> = _speechs.asStateFlow()

    private val _myDesignPaths = MutableStateFlow<List<String>>(emptyList())
    val myDesignPaths: StateFlow<List<String>> = _myDesignPaths.asStateFlow()

    // ==================== LOADING STATES ====================

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isQuickLoading = MutableStateFlow(false)
    val isQuickLoading = _isQuickLoading.asStateFlow()

    private val _errorQuick = MutableStateFlow<String?>(null)
    val errorQuick = _errorQuick.asStateFlow()

    private var isDataLoaded = false
    private var isDataQuickLoaded = false

    // ==================== INIT: LOAD ALL DATA ====================

    suspend fun loadInitialData() {
        if (isDataLoaded) {
            Log.d(TAG, "⚠️ Data already loaded, skipping")
            return
        }

        _isLoading.value = true
        _error.value = null

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🚀 Starting initial data load...")

                // 1. Load templates (từ cache hoặc assets)
                loadTemplates()

                // 2. Load customized characters (từ JSON)
                loadCustomizedCharacters()
                loadMyAvatars()
                // 3. Combine lists
                combineCharacterLists()

                // 4. Load assets khác
                coroutineScope {
                    launch { loadBackgrounds() }
                    launch { loadBackgroundTexts() }
                    launch { loadStickers() }
                    launch { loadSpeechs() }
                    launch { loadMyDesigns() }
                }

                isDataLoaded = true
                Log.d(TAG, "✅ Initial data loaded successfully")
                Log.d(TAG, "   📊 Templates: ${_templates.value.size}")
                Log.d(TAG, "   📊 Customized: ${_customizedCharacters.value.size}")
                Log.d(TAG, "   📊 Total characters: ${_characters.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading initial data: ${e.message}", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== LOAD TEMPLATES ====================
    suspend fun loadMyAvatars() {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, MY_AVATARS_FILE)
                if (!file.exists()) {
                    _myAvatars.value = emptyList()
                    Log.d(TAG, "📋 No my avatars found")
                    return@withContext
                }

                val json = file.readText()
                val type = object : TypeToken<List<DataModel>>() {}.type
                val avatars = gson.fromJson<List<DataModel>>(json, type) ?: emptyList()  // 🔥 Dùng gson

                _myAvatars.value = avatars
                Log.d(TAG, "✅ Loaded ${avatars.size} my avatars")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading my avatars: ${e.message}", e)
                _myAvatars.value = emptyList()
            }
        }
    }

    suspend fun saveMyAvatar(dataModel: DataModel) {
        withContext(Dispatchers.IO) {
            val list = _myAvatars.value.toMutableList()
            val index = list.indexOfFirst { it.custom.id == dataModel.custom.id }
            if (index >= 0) list[index] = dataModel else list.add(dataModel)
            _myAvatars.value = list

            try {
                val json = gson.toJson(list)  // 🔥 Dùng gson
                val file = File(context.filesDir, MY_AVATARS_FILE)
                file.writeText(json)
                Log.d(TAG, "💾 File saved: ${file.absolutePath}, Size: ${list.size} items")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving my avatar: ${e.message}", e)
            }
        }
    }

    suspend fun deleteMyAvatar(characterId: String) {
        withContext(Dispatchers.IO) {
            val list = _myAvatars.value.toMutableList()
            val removed = list.removeIf { it.custom.id == characterId }

            if (removed) {
                _myAvatars.value = list
                try {
                    val json = Gson().toJson(list)
                    val file = File(context.filesDir, MY_AVATARS_FILE)
                    file.writeText(json)
                    Log.d(TAG, "🗑️ Deleted my avatar: $characterId")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error deleting my avatar: ${e.message}", e)
                }
            }
        }
    }
    private suspend fun loadTemplates() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📦 Loading templates...")

                // Try load from cache first
                val cached = loadTemplatesFromJson()
                if (cached.isNotEmpty()) {
                    _templates.value = cached
                    Log.d(TAG, "✅ Loaded ${cached.size} templates from cache")
                    return@withContext
                }

                // If no cache, load from assets
                Log.d(TAG, "📂 No cache found, loading from assets...")
                loadTemplatesFromAssets()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading templates: ${e.message}", e)
            }
        }
    }

    private suspend fun loadTemplatesFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val result = arrayListOf<CustomModel>()
                val folders = assetManager.list("data") ?: return@withContext

                Log.d(TAG, "📁 Found ${folders.size} template folders")

                folders.forEach { folder ->
                    val basePath = "data/$folder"
                    val itemsRaw = assetManager.list(basePath) ?: return@forEach

                    val items = itemsRaw.sortedBy {
                        it.substringBefore("-").toIntOrNull() ?: 999
                    }

                    Log.d(TAG, "=== Processing template: $folder ===")

                    val bodyParts = arrayListOf<BodyPartModel>()
                    var avatar = ""
                    var position = 0

                    items.forEach { item ->
                        val fullPath = "$basePath/$item"
                        val contents = assetManager.list(fullPath)

                        // Avatar image (file, not folder)
                        if (contents.isNullOrEmpty()) {
                            avatar = "$ASSET_PREFIX/$fullPath"
                            Log.d(TAG, "  📷 Avatar: $item")
                            return@forEach
                        }

                        Log.d(TAG, "  📦 Body part: $item")

                        // Nav image
                        val nav = contents.firstOrNull { it.startsWith("nav.") }
                            ?.let { "$ASSET_PREFIX/$fullPath/$it" } ?: ""

                        val colors = arrayListOf<ColorModel>()

                        // Process layers
                        contents.filter { !it.startsWith("nav.") }.forEach { layer ->
                            val layerPath = "$fullPath/$layer"
                            val files = assetManager.list(layerPath)

                            if (files.isNullOrEmpty()) {
                                // Single image layer
                                colors.add(ColorModel("", arrayListOf("$ASSET_PREFIX/$layerPath")))
                            } else {
                                // Multiple variants
                                val paths = files.map { "$ASSET_PREFIX/$layerPath/$it" }
                                colors.add(ColorModel(layer, ArrayList(paths)))
                            }
                        }

                        processColorDefaults(colors, item)

                        bodyParts.add(BodyPartModel(
                            nav = nav,
                            listPath = colors,
                            position = position,
                            zIndex = position
                        ))

                        position++
                    }

                    result.add(
                        CustomModel(
                            id = "template_$folder",
                            avatar = avatar,
                            listPath = ArrayList(bodyParts),
                            selections = arrayListOf(),
                            imageSave = "",
                            updatedAt = System.currentTimeMillis()
                        )
                    )

                    Log.d(TAG, "✅ Template '$folder' loaded with ${bodyParts.size} body parts")
                }

                _templates.value = result
                saveTemplatesToJson(result)
                Log.d(TAG, "✅ Loaded ${result.size} templates from assets")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading templates from assets: ${e.message}", e)
            }
        }
    }

    private fun processColorDefaults(colors: ArrayList<ColorModel>, itemName: String) {
        try {
            val position = itemName.substringBefore("-").toIntOrNull() ?: return

            colors.forEach { colorModel ->
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
            Log.e(TAG, "❌ Error processing color defaults", e)
        }
    }

    // ==================== SAVE/LOAD TEMPLATES (Cache) ====================

    private suspend fun saveTemplatesToJson(templates: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(templates)
                val file = File(context.filesDir, TEMPLATES_FILE)
                file.writeText(json)
                Log.d(TAG, "💾 Templates cached: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving templates: ${e.message}", e)
            }
        }
    }

    private suspend fun loadTemplatesFromJson(): List<CustomModel> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, TEMPLATES_FILE)
                if (!file.exists()) {
                    Log.d(TAG, "📄 Templates cache not found")
                    return@withContext emptyList()
                }

                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                Gson().fromJson<List<CustomModel>>(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading templates from JSON: ${e.message}", e)
                emptyList()
            }
        }
    }

    // ==================== CUSTOMIZED CHARACTERS ====================

    private suspend fun loadCustomizedCharacters() {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, CUSTOMIZED_FILE)
                if (!file.exists()) {
                    _customizedCharacters.value = emptyList()
                    Log.d(TAG, "📋 No customized characters found")
                    return@withContext
                }

                val json = file.readText()
                val type = object : TypeToken<List<CustomModel>>() {}.type
                val characters = Gson().fromJson<List<CustomModel>>(json, type)

                _customizedCharacters.value = characters
                Log.d(TAG, "✅ Loaded ${characters.size} customized characters")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading customized characters: ${e.message}", e)
                _customizedCharacters.value = emptyList()
            }
        }
    }

    private suspend fun saveCustomizedCharacters(characters: List<CustomModel>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(characters)
                val file = File(context.filesDir, CUSTOMIZED_FILE)
                file.writeText(json)
                Log.d(TAG, "💾 Saved ${characters.size} customized characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving customized characters: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ ADD hoặc UPDATE customized character
     */
    suspend fun updateCustomizedCharacter(character: CustomModel) {
        withContext(Dispatchers.IO) {
            val list = _customizedCharacters.value.toMutableList()
            val index = list.indexOfFirst { it.id == character.id }

            if (index >= 0) {
                list[index] = character
                Log.d(TAG, "📝 Updated character: ${character.id}")
            } else {
                list.add(character)
                Log.d(TAG, "➕ Added new character: ${character.id}")
            }

            _customizedCharacters.value = list
            saveCustomizedCharacters(list)
            combineCharacterLists()
        }
    }

    /**
     * ✅ DELETE customized character
     */
    suspend fun deleteCustomizedCharacter(characterId: String) {
        withContext(Dispatchers.IO) {
            val list = _customizedCharacters.value.toMutableList()
            val removed = list.removeIf { it.id == characterId }

            if (removed) {
                _customizedCharacters.value = list
                saveCustomizedCharacters(list)
                combineCharacterLists()
                Log.d(TAG, "🗑️ Deleted character: $characterId")
            }
        }
    }

    // ==================== COMBINE LISTS ====================

    private fun combineCharacterLists() {
        val combined = _templates.value + _customizedCharacters.value
        _characters.value = combined

        Log.d(TAG, "📊 Combined characters: ${combined.size} total")
        Log.d(TAG, "   - Templates: ${_templates.value.size}")
        Log.d(TAG, "   - Customized: ${_customizedCharacters.value.size}")
    }

    // ==================== UTILITY FUNCTIONS ====================

    fun isTemplate(characterId: String): Boolean {
        return characterId.startsWith("template_")
    }

    fun getCharacterByIndex(index: Int): CustomModel? {
        return _characters.value.getOrNull(index)
    }

    fun getCharacterById(id: String): CustomModel? {
        return _characters.value.find { it.id == id }
    }

    // ==================== REFRESH FROM API ====================

    suspend fun refreshFromApi() {
        _isLoading.value = true
        _error.value = null

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Refreshing templates from API/Assets...")

                // ✅ CHỈ reload templates
                loadTemplatesFromAssets()

                // ✅ Combine lại (customized vẫn giữ nguyên)
                combineCharacterLists()

                Log.d(TAG, "✅ Refresh completed")
                Log.d(TAG, "   - Templates: ${_templates.value.size}")
                Log.d(TAG, "   - Customized (preserved): ${_customizedCharacters.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error refreshing: ${e.message}", e)
                _error.value = "Cannot refresh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun forceReloadAll() {
        isDataLoaded = false
        loadInitialData()
    }

    // ==================== LOAD ASSETS ====================

    private suspend fun loadBackgrounds() {
        try {
            val files = context.assets.list("bg") ?: emptyArray()
            val sorted = files.sortedBy { it.substringBeforeLast(".").toIntOrNull() ?: 0 }
                .map { "$ASSET_PREFIX/bg/$it" }
            _backgrounds.value = listOf("") + sorted
            Log.d(TAG, "✅ Loaded ${files.size} backgrounds")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading backgrounds: ${e.message}", e)
        }
    }

    private suspend fun loadBackgroundTexts() {
        try {
            val files = context.assets.list("BG_Text") ?: emptyArray()
            _backgroundTexts.value = files.map { "$ASSET_PREFIX/BG_Text/$it" }
            Log.d(TAG, "✅ Loaded ${files.size} background texts")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading background texts: ${e.message}", e)
        }
    }

    private suspend fun loadStickers() {
        try {
            val files = context.assets.list("sticker") ?: emptyArray()
            _stickers.value = files.map { "$ASSET_PREFIX/sticker/$it" }
            Log.d(TAG, "✅ Loaded ${files.size} stickers")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading stickers: ${e.message}", e)
        }
    }

    private suspend fun loadSpeechs() {
        try {
            val files = context.assets.list("BG_Text") ?: emptyArray()
            _speechs.value = files.map { "$ASSET_PREFIX/BG_Text/$it" }
            Log.d(TAG, "✅ Loaded ${files.size} speeches")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading speeches: ${e.message}", e)
        }
    }

    // ==================== MY DESIGNS ====================

    private suspend fun loadMyDesigns() {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, MY_DESIGNS_FILE)
                if (!file.exists()) {
                    _myDesignPaths.value = emptyList()
                    Log.d(TAG, "📋 No my designs found")
                    return@withContext
                }

                val json = file.readText()
                val type = object : TypeToken<List<String>>() {}.type
                val paths = Gson().fromJson<List<String>>(json, type) ?: emptyList()

                _myDesignPaths.value = paths
                Log.d(TAG, "✅ Loaded ${paths.size} my designs")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading my designs: ${e.message}", e)
                _myDesignPaths.value = emptyList()
            }
        }
    }

    suspend fun saveMyDesignToJson(paths: List<String>) {
        withContext(Dispatchers.IO) {
            try {
                val json = Gson().toJson(paths)
                val file = File(context.filesDir, MY_DESIGNS_FILE)
                file.writeText(json)
                Log.d(TAG, "💾 Saved ${paths.size} my designs")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving my designs: ${e.message}", e)
            }
        }
    }

    suspend fun addMyDesignPath(imagePath: String) {
        val currentList = _myDesignPaths.value.toMutableList()
        if (!currentList.contains(imagePath)) {
            currentList.add(0, imagePath)
            _myDesignPaths.value = currentList
            saveMyDesignToJson(currentList)
            Log.d(TAG, "➕ Added new design: $imagePath")
        }
    }

    suspend fun loadMyDesignData() {
        loadMyDesigns()
    }

    // ==================== QUICK RANDOM ====================

    suspend fun loadQuickData(
        quickRandomManager: QuickRandomManager? = null,
        onQuickRandomProgress: (current: Int, total: Int, templateName: String) -> Unit = { _, _, _ -> }
    ) {
        if (isDataQuickLoaded) {
            Log.d(TAG, "⚠️ Quick data already loaded, skipping")
            return
        }

        _isQuickLoading.value = true
        _errorQuick.value = null

        withContext(Dispatchers.IO) {
            try {
                if (quickRandomManager != null) {
                    val hasQuickRandom = quickRandomManager.hasQuickRandomData()
                    if (!hasQuickRandom) {
                        Log.d(TAG, "🚀 Starting quick random generation...")
                        quickRandomManager.generateQuickRandomCharacters(onQuickRandomProgress)
                        Log.d(TAG, "✅ Quick random generation completed")
                    } else {
                        Log.d(TAG, "✅ Quick random data already exists")
                    }
                    isDataQuickLoaded = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading quick data: ${e.message}", e)
                _errorQuick.value = e.message
            } finally {
                _isQuickLoading.value = false
            }
        }
    }

    // ==================== CLEAR DATA ====================

    fun clearData() {
        _templates.value = emptyList()
        _customizedCharacters.value = emptyList()
        _characters.value = emptyList()
        _backgrounds.value = emptyList()
        _backgroundTexts.value = emptyList()
        _stickers.value = emptyList()
        _speechs.value = emptyList()
        _myDesignPaths.value = emptyList()
        isDataLoaded = false
        isDataQuickLoaded = false
        Log.d(TAG, "🗑️ All data cleared")
    }
}