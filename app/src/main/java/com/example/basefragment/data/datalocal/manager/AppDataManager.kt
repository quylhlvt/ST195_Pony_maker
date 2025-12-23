package com.example.basefragment.data.datalocal.manager

import android.content.Context
import android.util.Log
import androidx.collection.LruCache
import com.example.basefragment.data.model.api.PartAPI
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.usecase.GetDataCustomUseCase
import com.example.basefragment.utils.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDataCustomUseCase: GetDataCustomUseCase
) {

    companion object {
        private const val TAG = "AppDataManager"
        private const val ASSET_PREFIX = "file:///android_asset/"
        private const val TEMPLATES_FILE = "templates.json"
        private const val CUSTOMIZED_FILE = "customized.json"
        private const val MY_AVATARS_FILE = "my_avatars.json"
        private const val MY_DESIGNS_FILE = "my_designs.json"
    }

    // Memory cache cho dữ liệu assets (tối đa 10 categories)
    private val assetDataCache = LruCache<String, List<CustomModel>>(10)

    // ==================== STATE FLOWS ====================
    private val _characters = MutableStateFlow<List<CustomModel>>(emptyList())
    val characters: StateFlow<List<CustomModel>> = _characters.asStateFlow()

    private val _templates = MutableStateFlow<List<CustomModel>>(emptyList())
    val templates: StateFlow<List<CustomModel>> = _templates.asStateFlow()

    private val _customizedCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val customizedCharacters: StateFlow<List<CustomModel>> = _customizedCharacters.asStateFlow()

    private val _backgrounds = MutableStateFlow<List<String>>(emptyList())
    val backgrounds: StateFlow<List<String>> = _backgrounds.asStateFlow()

    private val _backgroundTexts = MutableStateFlow<List<String>>(emptyList())
    val backgroundTexts: StateFlow<List<String>> = _backgroundTexts.asStateFlow()

    private val _stickers = MutableStateFlow<List<String>>(emptyList())
    val stickers: StateFlow<List<String>> = _stickers.asStateFlow()

    private val gson = Gson()

    // ==================== PUBLIC: LOAD ALL DATA ====================
    suspend fun loadAllData() {
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val jobCharacters = async { loadCharactersFromAssetsAndCache() }
                    val jobBg = async { loadBackgroundsFromAssets() }
                    val jobBgText = async { loadBackgroundTextsFromAssets() }
                    val jobSticker = async { loadStickersFromAssets() }

                    awaitAll(jobCharacters, jobBg, jobBgText, jobSticker)
                }

                // Load user data (my avatars, designs) - không ảnh hưởng đến characters
                loadMyAvatarsFromJson()
                loadMyDesignsFromJson()

                // Load từ API (nếu có) - có thể thêm hoặc override assets
                loadCharactersFromApi()

                // Cuối cùng: gộp templates + customized thành characters
                combineAllCharacters()
            } catch (e: Exception) {
                Log.e(TAG, "loadAllData failed", e)
            }
        }
    }

    // ==================== CHARACTERS FROM ASSETS + JSON CACHE ====================
    private suspend fun loadCharactersFromAssetsAndCache() {
        withContext(Dispatchers.IO) {
            // Ưu tiên 1: Load từ file cache JSON (nhanh nhất)
            if (loadTemplatesFromJsonBlocking()) {
                combineAllCharacters()
                return@withContext
            }

            // Ưu tiên 2: Load từ memory cache (LruCache)
            val memoryCached = assetDataCache["assetData"]
            if (memoryCached != null) {
                _templates.value = memoryCached
                saveTemplatesToJsonBlocking(memoryCached)
                combineAllCharacters()
                return@withContext
            }

            // Ưu tiên 3: Load mới từ assets
            try {
                val assetManager = context.assets
                val dataFolders = assetManager.list("data") ?: emptyArray()

                if (dataFolders.isEmpty()) {
                    Log.w(TAG, "No 'data' folder found in assets")
                    combineAllCharacters()
                    return@withContext
                }

                val categories = dataFolders.mapNotNull { category ->
                    loadCategory(assetManager, category)
                }

                _templates.value = categories
                assetDataCache.put("assetData", categories)
                saveTemplatesToJsonBlocking(categories)
                combineAllCharacters()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load characters from assets", e)
                combineAllCharacters()
            }
        }
    }

    private fun loadCategory(
        assetManager: android.content.res.AssetManager,
        category: String
    ): CustomModel? {
        return try {
            val bodyPartFolders = assetManager.list("data/$category") ?: return null
            val customModel = CustomModel(avatar = "", listPath = arrayListOf())

            for (bodyPart in bodyPartFolders) {
                val partPaths = assetManager.list("data/$category/$bodyPart")
                    ?.map { "data/$category/$bodyPart/$it" } ?: emptyList()

                // Trường hợp là avatar chính (không có subfolder)
                if (partPaths.isEmpty()) {
                    customModel.avatar = "$ASSET_PREFIX/data/$category/$bodyPart"
                    continue
                }

                // Tìm nav icon
                val navPath = partPaths.find { it.contains("nav.") }
                    ?.let { "$ASSET_PREFIX$it" } ?: continue

                val bodyPartModel = BodyPartModel(nav = navPath, listPath = arrayListOf())

                // Các folder màu
                partPaths.filterNot { it.contains("nav.") }.forEach { colorFolderPath ->
                    val imageFiles = assetManager.list(colorFolderPath)
                        ?.map { "$colorFolderPath/$it" } ?: emptyList()

                    val fullImagePaths = imageFiles.map { "$ASSET_PREFIX$it" } as ArrayList<String>

                    if (imageFiles.isEmpty()) {
                        // Không có file ảnh → là một ảnh đơn
                        val singlePath = "$ASSET_PREFIX$colorFolderPath"
                        if (bodyPartModel.listPath.isEmpty()) {
                            bodyPartModel.listPath.add(ColorModel("", arrayListOf(singlePath)))
                        } else {
                            bodyPartModel.listPath[0].listPath.add(singlePath)
                        }
                    } else {
                        // Có nhiều ảnh → là một color variant
                        val colorName = colorFolderPath.substringAfterLast("/")
                        bodyPartModel.listPath.add(ColorModel(colorName, fullImagePaths))
                    }
                }

                // Xác định layer cơ bản (y = 1 trong filename: x-y-name)
                val filename = navPath.substringAfterLast("/").substringBefore(".")
                val isBaseLayer = filename.split("-").getOrNull(1)?.toIntOrNull() == 1

                // Thêm "dice" và "none" đúng logic
                bodyPartModel.listPath.forEach { colorModel ->
                    val paths = colorModel.listPath.toMutableSet()
                    paths.add("dice")
                    if (!isBaseLayer) paths.add("none")
                    colorModel.listPath = ArrayList(paths.sorted()) // giữ thứ tự ổn định
                }

                customModel.listPath.add(bodyPartModel)
            }

            // Chỉ trả về nếu có dữ liệu
            customModel.takeIf { it.listPath.isNotEmpty() || it.avatar.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading category '$category'", e)
            null
        }
    }

    // ==================== API DATA ====================
    private suspend fun loadCharactersFromApi() {
        withContext(Dispatchers.IO) {
            try {
                val response = getDataCustomUseCase()
                if (response.status == Status.SUCCESS && response.data != null) {
                    val parsed = parseApiData(response.data)
                    _customizedCharacters.value = parsed
                    saveCustomizedToJsonBlocking(parsed)
                    combineAllCharacters()
                } else {
                    Log.w(TAG, "API returned no data or failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading from API", e)
            }
        }
    }

    private fun parseApiData(apiData: Map<String, List<PartAPI>>?): List<CustomModel> {
        if (apiData.isNullOrEmpty()) return emptyList()

        return apiData.mapNotNull { (_, parts) ->
            val customModel = CustomModel(avatar = "", listPath = arrayListOf())

            parts.forEach { part ->
                val colorPaths: List<String> = try {
                    gson.fromJson(part.colorArray, object : TypeToken<List<String>>() {}.type) ?: emptyList()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse colorArray", e)
                    emptyList()
                }

                if (colorPaths.isEmpty()) return@forEach

                val navPath = "$ASSET_PREFIX${part.parts}"
                val isBaseLayer = part.level == 1

                val bodyPartModel = BodyPartModel(
                    nav = navPath,
                    listPath = arrayListOf(
                        ColorModel(
                            part.parts.substringAfterLast("/"),
                            colorPaths.map { "$ASSET_PREFIX$it" } as ArrayList<String>
                        )
                    )
                )

                bodyPartModel.listPath.forEach { colorModel ->
                    val set = colorModel.listPath.toMutableSet()
                    set.add("dice")
                    if (!isBaseLayer) set.add("none")
                    colorModel.listPath = ArrayList(set.sorted())
                }

                customModel.listPath.add(bodyPartModel)
            }

            customModel.takeIf { it.listPath.isNotEmpty() }
        }
    }

    // ==================== JSON CACHE HELPERS ====================
    private fun loadTemplatesFromJsonBlocking(): Boolean {
        return try {
            val file = File(context.filesDir, TEMPLATES_FILE)
            if (!file.exists()) return false

            val json = file.readText()
            val type = object : TypeToken<List<CustomModel>>() {}.type
            val data = gson.fromJson<List<CustomModel>>(json, type) ?: emptyList()
            _templates.value = data
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load templates from JSON cache", e)
            false
        }
    }

    private fun saveTemplatesToJsonBlocking(data: List<CustomModel>) {
        try {
            val file = File(context.filesDir, TEMPLATES_FILE)
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save templates to JSON cache", e)
        }
    }

    private fun saveCustomizedToJsonBlocking(data: List<CustomModel>) {
        try {
            val file = File(context.filesDir, CUSTOMIZED_FILE)
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save customized to JSON cache", e)
        }
    }

    // ==================== MY AVATARS ====================
    suspend fun addMyAvatar(avatar: CustomModel) = withContext(Dispatchers.IO) {
        val currentList = loadMyAvatarsFromJson().toMutableList()
        currentList.add(avatar)
        saveMyAvatarsToJson(currentList)
    }

    suspend fun getMyAvatars(): List<CustomModel> = withContext(Dispatchers.IO) {
        loadMyAvatarsFromJson()
    }

    suspend fun deleteMyAvatar(path: String) = withContext(Dispatchers.IO) {
        val currentList = loadMyAvatarsFromJson().toMutableList()
        currentList.removeAll { it.avatar == path }
        saveMyAvatarsToJson(currentList)
    }

    private fun loadMyAvatarsFromJson(): List<CustomModel> {
        return try {
            val file = File(context.filesDir, MY_AVATARS_FILE)
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<CustomModel>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load my_avatars.json", e)
            emptyList()
        }
    }

    private fun saveMyAvatarsToJson(avatars: List<CustomModel>) {
        try {
            val file = File(context.filesDir, MY_AVATARS_FILE)
            file.writeText(gson.toJson(avatars))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save my_avatars.json", e)
        }
    }

    private fun loadMyDesignsFromJson() {
        // Tương tự nếu bạn dùng my_designs
        // Hiện tại chưa dùng nên để trống hoặc implement sau
    }

    // ==================== BACKGROUND & STICKERS ====================
    private suspend fun loadBackgroundsFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                val files = context.assets.list("bg") ?: emptyArray()
                val sorted = files
                    .sortedBy { it.substringBeforeLast(".").toIntOrNull() ?: 999 }
                    .map { "$ASSET_PREFIX/bg/$it" }
                _backgrounds.value = listOf("") + sorted
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load backgrounds", e)
            }
        }
    }

    private suspend fun loadBackgroundTextsFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                val files = context.assets.list("BG_Text") ?: emptyArray()
                _backgroundTexts.value = files.map { "$ASSET_PREFIX/BG_Text/$it" }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load background texts", e)
            }
        }
    }

    private suspend fun loadStickersFromAssets() {
        withContext(Dispatchers.IO) {
            try {
                val files = context.assets.list("sticker") ?: emptyArray()
                _stickers.value = files.map { "$ASSET_PREFIX/sticker/$it" }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load stickers", e)
            }
        }
    }

    // ==================== UTILS ====================
    private fun combineAllCharacters() {
        _characters.value = _templates.value + _customizedCharacters.value
    }

    fun getCharacterByIndex(index: Int): CustomModel? = _characters.value.getOrNull(index)
}