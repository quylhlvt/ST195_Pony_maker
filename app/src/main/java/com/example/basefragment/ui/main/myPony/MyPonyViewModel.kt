package com.example.basefragment.ui.main.myPony

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.mypony.MyAlbumModel
import com.example.basefragment.utils.share.telegram.TelegramSharing
import com.example.basefragment.utils.share.whatsapp.IdGenerator
import com.example.basefragment.utils.share.whatsapp.StickerBook
import com.example.basefragment.utils.share.whatsapp.StickerPack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MyPonyViewModel @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    companion object {
        private const val TAG = "MyPonyViewModel"
    }

    // Avatar list
    private val _myAvatarList = MutableStateFlow<List<MyAlbumModel>>(emptyList())
    val myAvatarList: StateFlow<List<MyAlbumModel>> = _myAvatarList.asStateFlow()

    // Design list
    private val _myDesignList = MutableStateFlow<List<MyAlbumModel>>(emptyList())
    val myDesignList: StateFlow<List<MyAlbumModel>> = _myDesignList.asStateFlow()

    // Download state
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.IDLE)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    enum class DownloadState {
        IDLE, LOADING, SUCCESS, ERROR
    }

    // ==================== LOAD AVATAR DATA ====================

    /**
     * ✅ Load avatars TỪ customized characters (KHÔNG scan storage folder)
     */
    fun loadMyAvatar(context: Context, forceReload: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            appDataManager.loadMyAvatars()  // Load từ file riêng
        }
    }

    // ==================== LOAD DESIGN DATA ====================

    fun loadMyDesign(context: Context, forceReload: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Loading designs...")

                val paths = appDataManager.myDesignPaths.value

                Log.d(TAG, "📦 Raw paths from AppDataManager: ${paths.size}")

                // Filter only existing files
                val existingPaths = paths.filter { path ->
                    val exists = File(path).exists()
                    if (!exists) {
                        Log.w(TAG, "⚠️ File not found: $path")
                    }
                    exists
                }

                Log.d(TAG, "✅ Existing paths: ${existingPaths.size}")

                val list = existingPaths.map { path ->
                    MyAlbumModel(
                        path = path,
                        isSelected = false,
                        isShowSelection = false,
                        characterId="",
                        type = 2
                    )
                }

                withContext(Dispatchers.Main) {
                    _myDesignList.value = list
                    Log.d(TAG, "✅ Design list updated: ${list.size} items")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading designs: ${e.message}", e)
            }
        }
    }

    // ==================== SELECTION MANAGEMENT ====================

    fun showLongClick(position: Int, isAvatar: Boolean) {
        val currentList = if (isAvatar) _myAvatarList.value else _myDesignList.value
        val updatedList = currentList.mapIndexed { index, item ->
            if (index == position) {
                item.copy(isSelected = true, isShowSelection = true)
            } else {
                item.copy(isShowSelection = true)
            }
        }

        if (isAvatar) {
            _myAvatarList.value = updatedList
        } else {
            _myDesignList.value = updatedList
        }
    }

    fun toggleSelect(position: Int, isAvatar: Boolean = false) {
        val currentList = if (isAvatar) _myAvatarList.value else _myDesignList.value
        val updatedList = currentList.mapIndexed { index, item ->
            if (index == position) {
                item.copy(isSelected = !item.isSelected)
            } else {
                item
            }
        }

        if (isAvatar) {
            _myAvatarList.value = updatedList
        } else {
            _myDesignList.value = updatedList
        }
    }

    fun selectAll(shouldSelectAll: Boolean, isAvatar: Boolean = true) {
        val currentList = if (isAvatar) _myAvatarList.value else _myDesignList.value
        val updatedList = currentList.map { item ->
            item.copy(
                isSelected = shouldSelectAll,
                isShowSelection = shouldSelectAll || item.isShowSelection
            )
        }

        if (isAvatar) {
            _myAvatarList.value = updatedList
        } else {
            _myDesignList.value = updatedList
        }
    }

    fun getPathSelected(isAvatar: Boolean = true): ArrayList<String> {
        val currentList = if (isAvatar) _myAvatarList.value else _myDesignList.value
        return ArrayList(currentList.filter { it.isSelected }.map { it.path })
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * ✅ Delete avatar - Xóa từ customized characters
     */
    /**
     * ✅ Delete avatar - Xóa từ CẢ customized.json VÀ my_avatars.json
     */
    fun deleteItem(context: Context, paths: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting ${paths.size} avatar items")

                paths.forEach { imagePath ->
                    // ✅ Tìm character có imageSave trùng với path
                    val character = appDataManager.customizedCharacters.value
                        .find { it.imageSave == imagePath }

                    if (character != null) {
                        Log.d(TAG, "   🗑️ Deleting character: ${character.id}")

                        // 🔥 1. Xóa từ customized.json
                        appDataManager.deleteCustomizedCharacter(character.id)

                        // 🔥 2. Xóa từ my_avatars.json
                        appDataManager.deleteMyAvatar(character.id)

                        // 🔥 3. Xóa file ảnh
                        deleteFileFromStorage(context, imagePath)

                        Log.d(TAG, "   ✅ Deleted character: ${character.id}")
                    } else {
                        Log.w(TAG, "   ⚠️ Character not found for path: $imagePath")
                    }
                }

                withContext(Dispatchers.Main) {
                    // 🔥 Reload data sau khi xóa
                    loadMyAvatar(context, true)
                }

                Log.d(TAG, "✅ Delete completed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting items: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Delete design
     */
    fun deleteItemDesign(paths: ArrayList<String>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🗑️ Deleting ${paths.size} design items")

                paths.forEach { path ->
                    deleteFileFromStorage(context, path)
                }

                // ✅ Update AppDataManager
                val current = appDataManager.myDesignPaths.value.toMutableList()
                val sizeBefore = current.size
                current.removeAll(paths)

                Log.d(TAG, "📦 Removed ${sizeBefore - current.size} paths from AppDataManager")

                appDataManager.saveMyDesignToJson(current)

                withContext(Dispatchers.Main) {
                    loadMyDesign(context, true)
                }

                Log.d(TAG, "✅ Delete design completed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting designs: ${e.message}", e)
            }
        }
    }

    private suspend fun deleteFileFromStorage(context: Context, path: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "🗑️ File deleted: $path (success: $deleted)")
                } else {
                    Log.w(TAG, "⚠️ File not found: $path")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting file: ${e.message}", e)
            }
        }
    }

    // ==================== DOWNLOAD ====================

    fun downloadFiles(context: Context, paths: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _downloadState.value = DownloadState.LOADING

                paths.forEach { path ->
                    downloadFileToPublicStorage(context, path)
                }

                withContext(Dispatchers.Main) {
                    _downloadState.value = DownloadState.SUCCESS
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error downloading files: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _downloadState.value = DownloadState.ERROR
                }
            }
        }
    }

    private suspend fun downloadFileToPublicStorage(context: Context, path: String) {
        withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(path)
                if (!sourceFile.exists()) {
                    Log.w(TAG, "⚠️ Source file not found: $path")
                    return@withContext
                }

                // TODO: Implement MediaStore API for Android 10+
                Log.d(TAG, "📥 Download file: ${sourceFile.name}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error downloading file: ${e.message}", e)
            }
        }
    }

    // ==================== WHATSAPP INTEGRATION ====================

    fun addToWhatsapp(
        context: Context,
        packageName: String,
        paths: ArrayList<String>,
        callback: (StickerPack?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                StickerBook.init(context)

                val uriList = ArrayList<Uri>()
                paths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        uriList.add(uri)
                    }
                }

                if (uriList.size < 3) {
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                    return@launch
                }

                val identifier = IdGenerator.generateIdFromUrl(context, paths[0])
                val stickerPack = StickerPack(identifier, packageName, uriList, context)
                StickerBook.addPackIfNotAlreadyAdded(stickerPack)

                withContext(Dispatchers.Main) {
                    callback(stickerPack)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error adding to WhatsApp: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    // ==================== TELEGRAM INTEGRATION ====================

    fun addToTelegram(context: Context, paths: ArrayList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uriList = mutableListOf<Uri>()
                paths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        uriList.add(uri)
                    }
                }

                if (uriList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        TelegramSharing.importToTelegram(context, uriList)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error adding to Telegram: ${e.message}", e)
            }
        }
    }

}
