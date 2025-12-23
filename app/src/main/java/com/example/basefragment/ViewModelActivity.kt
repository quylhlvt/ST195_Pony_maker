package com.example.basefragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.model.quick.QuickRandomProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelActivity @Inject constructor(
    val appDataManager: AppDataManager,
    private val quickRandomManager: QuickRandomManager
) : ViewModel() {

    companion object {
        private const val TAG = "ViewModelActivity"
    }

    // ✅ EXPOSE AppDataManager flows
    val characters: StateFlow<List<CustomModel>> = appDataManager.characters
    val templates: StateFlow<List<CustomModel>> = appDataManager.templates
    val customizedCharacters: StateFlow<List<CustomModel>> = appDataManager.customizedCharacters

    val backgrounds: StateFlow<List<String>> = appDataManager.backgrounds
    val backgroundTexts: StateFlow<List<String>> = appDataManager.backgroundTexts
    val stickers: StateFlow<List<String>> = appDataManager.stickers
    val speechs: StateFlow<List<String>> = appDataManager.speechs
    val myDesignPaths: StateFlow<List<String>> = appDataManager.myDesignPaths

    val isLoading: StateFlow<Boolean> = appDataManager.isLoading
    val error: StateFlow<String?> = appDataManager.error
    private val _quickRandomProgress = MutableStateFlow(QuickRandomProgress(0, 0, ""))
    val quickRandomProgress: StateFlow<QuickRandomProgress> = _quickRandomProgress.asStateFlow()

    private val _isQuickRandomLoading = MutableStateFlow(false)
    val isQuickRandomLoading: StateFlow<Boolean> = _isQuickRandomLoading.asStateFlow()

    private val _isTemplatesReady = MutableStateFlow(false)
    val isTemplatesReady: StateFlow<Boolean> = _isTemplatesReady.asStateFlow()

    init {
        loadTemplatesFirst()
        loadQuickRandomLater()

    }
    private fun loadTemplatesFirst() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🚀 PHASE 1: Loading templates (fast)...")

                appDataManager.loadInitialData()

                _isTemplatesReady.value = true

                Log.d(TAG, "✅ PHASE 1 COMPLETED - UI ready!")
                Log.d(TAG, "   📊 Templates: ${templates.value.size}")
                Log.d(TAG, "   📊 Customized: ${customizedCharacters.value.size}")
                Log.d(TAG, "   📊 Total: ${characters.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ PHASE 1 Error: ${e.message}", e)
            }
        }
    }
    private fun loadQuickRandomLater() {
        viewModelScope.launch {
            try {
                // ✅ Đợi templates load xong trước
                isTemplatesReady.collect { ready ->
                    if (ready) {
                        Log.d(TAG, "🚀 PHASE 2: Loading quick random (background)...")
                        _isQuickRandomLoading.value = true

                        appDataManager.loadQuickData(
                            quickRandomManager = quickRandomManager,
                            onQuickRandomProgress = { current, total, templateName ->
                                _quickRandomProgress.value = QuickRandomProgress(current, total, templateName)
                                Log.d(TAG, "📊 Quick random: $current/$total ($templateName)")
                            }
                        )

                        _isQuickRandomLoading.value = false
                        Log.d(TAG, "✅ PHASE 2 COMPLETED - Quick random ready!")
                        return@collect // ✅ Chỉ chạy 1 lần
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ PHASE 2 Error: ${e.message}", e)
                _isQuickRandomLoading.value = false
            }
        }
    }

    // ==================== CHARACTER OPERATIONS ====================

    /**
     * ✅ Get character by index (from combined list)
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        val character = appDataManager.getCharacterByIndex(index)
        Log.d(TAG, "getCharacterByIndex($index): ${character?.id}")
        return character
    }

    /**
     * ✅ Get character by ID
     */
    fun getCharacterById(characterId: String): CustomModel? {
        val character = appDataManager.getCharacterById(characterId)
        Log.d(TAG, "getCharacterById($characterId): found=${character != null}")
        return character
    }
    fun retryLoadData() {
        if (templates.value.isEmpty() && !isLoading.value) {
            viewModelScope.launch {
                appDataManager.loadInitialData()
            }
        }
    }
    /**
     * ✅ Check if character is a template
     */
    fun isTemplate(characterId: String): Boolean {
        return appDataManager.isTemplate(characterId)
    }

    /**
     * ✅ Update or add customized character
     * - Nếu là template → tạo character mới với ID mới
     * - Nếu đã customize → update existing
     */
    fun updateOrAddCharacter(character: CustomModel, index: Int = -1) {
        viewModelScope.launch {
            try {
                val isTemplate = isTemplate(character.id)

                if (isTemplate) {
                    // ✅ Template → Tạo character mới với ID mới
                    val newCharacter = character.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        updatedAt = System.currentTimeMillis()
                    )
                    appDataManager.updateCustomizedCharacter(newCharacter)
                    Log.d(TAG, "✅ Created new character from template:")
                    Log.d(TAG, "   - Template ID: ${character.id}")
                    Log.d(TAG, "   - New ID: ${newCharacter.id}")
                } else {
                    // ✅ Customized → Update existing
                    val updatedCharacter = character.copy(
                        updatedAt = System.currentTimeMillis()
                    )
                    appDataManager.updateCustomizedCharacter(updatedCharacter)
                    Log.d(TAG, "✅ Updated customized character: ${updatedCharacter.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating character: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Delete customized character (không thể xóa template)
     */
    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            if (isTemplate(characterId)) {
                Log.w(TAG, "⚠️ Cannot delete template: $characterId")
                return@launch
            }
            appDataManager.deleteCustomizedCharacter(characterId)
            Log.d(TAG, "✅ Deleted character: $characterId")
        }
    }

    // ==================== REFRESH & RELOAD ====================

    /**
     * ✅ Refresh templates from API/Assets
     * KHÔNG ẢNH HƯỞNG đến customized characters
     */
    fun refreshApiData() {
        viewModelScope.launch {
            appDataManager.refreshFromApi()
        }
    }
    /**
     * ✅ Force reload all data
     */
    fun forceReloadAll() {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Force reloading all data...")
            appDataManager.forceReloadAll()
        }
    }

    // ==================== UTILITY ====================

    fun getCharacterIndexById(characterId: String): Int {
        val index = characters.value.indexOfFirst { it.id == characterId }
        Log.d(TAG, "getCharacterIndexById($characterId): $index")
        return index
    }

    fun getTemplateById(templateId: String): CustomModel? {
        return templates.value.find { it.id == templateId }
    }

    fun clearData() {
        appDataManager.clearData()
        Log.d(TAG, "🗑️ All data cleared")
    }
}