package com.example.basefragment.data.datalocal.manager

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.repository.ApiRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class quản lý tải và lưu trữ toàn bộ data của app
 * Sử dụng Singleton pattern để đảm bảo chỉ load 1 lần
 */
@Singleton
class AppDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiRepository: ApiRepository
) {

    companion object {
        private const val TAG = "AppDataManager"
        private const val CHARACTERS_ASSET_PATH = "characters.json"
        private const val BACKGROUNDS_ASSET_PATH = "backgrounds.json"
        private const val STICKERS_ASSET_PATH = "stickers.json"
    }

    // StateFlow để các Fragment có thể observe
    private val _characters = MutableStateFlow<List<CustomModel>>(emptyList())
    val characters: StateFlow<List<CustomModel>> = _characters.asStateFlow()

    private val _backgrounds = MutableStateFlow<List<String>>(emptyList())
    val backgrounds: StateFlow<List<String>> = _backgrounds.asStateFlow()

    private val _stickers = MutableStateFlow<List<String>>(emptyList())
    val stickers: StateFlow<List<String>> = _stickers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var isDataLoaded = false

    /**
     * Load toàn bộ data của app (characters, backgrounds, stickers)
     * Sử dụng parallel loading để tăng tốc độ
     */
    suspend fun loadAllAppData() {
        if (isDataLoaded) {
            Log.d(TAG, "Data đã được load rồi, bỏ qua")
            return
        }

        _isLoading.value = true
        _loadError.value = null

        withContext(Dispatchers.IO) {
            try {
                // Load song song để nhanh hơn
                val charactersDeferred = async { loadCharacters() }
                val backgroundsDeferred = async { loadBackgrounds() }
                val stickersDeferred = async { loadStickers() }

                // Đợi tất cả hoàn thành
                charactersDeferred.await()
                backgroundsDeferred.await()
                stickersDeferred.await()

                isDataLoaded = true
                Log.d(TAG, "✅ Load data thành công!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Lỗi khi load data: ${e.message}", e)
                _loadError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load characters từ assets hoặc API
     */
    private suspend fun loadCharacters() {
        try {
            // Ưu tiên load từ assets trước (nhanh hơn)
            val assetsCharacters = loadCharactersFromAssets()
            if (assetsCharacters.isNotEmpty()) {
                _characters.value = assetsCharacters
                Log.d(TAG, "Loaded ${assetsCharacters.size} characters từ assets")
            }

            // Sau đó load từ API để cập nhật (nếu cần)
            val apiResult = apiRepository.getCharacters()
            if (apiResult.isSuccess) {
                val apiCharacters = apiResult.getOrNull() ?: emptyList()
                if (apiCharacters.isNotEmpty()) {
                    _characters.value = apiCharacters
                    Log.d(TAG, "Updated ${apiCharacters.size} characters từ API")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi load characters: ${e.message}")
            // Nếu lỗi, vẫn giữ data từ assets
        }
    }

    /**
     * Load backgrounds từ assets hoặc API
     */
// Trong AppDataManager.kt
    private fun loadBackgroundsFromDevice(): List<String> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Backgrounds"
        )
        return dir.listFiles { file ->
            file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
        }?.map { it.absolutePath } ?: emptyList()
    }

    /**
     * Load stickers từ assets hoặc API
     */
    private suspend fun loadStickers() {
        try {
            val assetsStickers = loadStickersFromAssets()
            if (assetsStickers.isNotEmpty()) {
                _stickers.value = assetsStickers
                Log.d(TAG, "Loaded ${assetsStickers.size} stickers từ assets")
            }

            val apiResult = apiRepository.getStickers()
            if (apiResult.isSuccess) {
                val apiStickers = apiResult.getOrNull() ?: emptyList()
                if (apiStickers.isNotEmpty()) {
                    _stickers.value = apiStickers
                    Log.d(TAG, "Updated ${apiStickers.size} stickers từ API")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi load stickers: ${e.message}")
        }
    }

    /**
     * Đọc characters từ assets/characters.json
     */
    private fun loadCharactersFromAssets(): List<CustomModel> {
        return try {
            val json = context.assets.open(CHARACTERS_ASSET_PATH).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<CustomModel>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "Không tìm thấy file $CHARACTERS_ASSET_PATH trong assets")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi parse characters JSON: ${e.message}")
            emptyList()
        }
    }

    /**
     * Đọc backgrounds từ assets/backgrounds.json
     */
    private fun loadBackgroundsFromAssets(): List<String> {
        return try {
            val json = context.assets.open(BACKGROUNDS_ASSET_PATH).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "Không tìm thấy file $BACKGROUNDS_ASSET_PATH trong assets")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi parse backgrounds JSON: ${e.message}")
            emptyList()
        }
    }

    /**
     * Đọc stickers từ assets/stickers.json
     */
    private fun loadStickersFromAssets(): List<String> {
        return try {
            val json = context.assets.open(STICKERS_ASSET_PATH).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "Không tìm thấy file $STICKERS_ASSET_PATH trong assets")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi parse stickers JSON: ${e.message}")
            emptyList()
        }
    }

    /**
     * Lấy character theo ID
     */
    fun getCharacterById(id: String): CustomModel? {
        return _characters.value.find { it.id == id }
    }

    /**
     * Lấy character theo index
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        return _characters.value.getOrNull(index)
    }

    /**
     * Force reload data (để refresh)
     */
    suspend fun reloadData() {
        isDataLoaded = false
        loadAllAppData()
    }

    /**
     * Clear all data
     */
    fun clearData() {
        _characters.value = emptyList()
        _backgrounds.value = emptyList()
        _stickers.value = emptyList()
        isDataLoaded = false
    }
}