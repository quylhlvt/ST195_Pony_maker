package com.example.basefragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.datalocal.manager.QuickRandomManager
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

    // ✅ Quick random progress
    private val _quickRandomProgress = MutableStateFlow(QuickRandomProgress(0, 0, ""))
    val quickRandomProgress: StateFlow<QuickRandomProgress> = _quickRandomProgress.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🚀 Starting data load...")

                // Load main data
                appDataManager.loadInitialData()

                // Load quick random data
                appDataManager.loadQuickData(
                    quickRandomManager = quickRandomManager,
                    onQuickRandomProgress = { current, total, templateName ->
                        _quickRandomProgress.value = QuickRandomProgress(current, total, templateName)
                        Log.d(TAG, "📊 Quick random progress: $current/$total ($templateName)")
                    }
                )

                Log.d(TAG, "✅ All data loaded successfully")
                Log.d(TAG, "   📊 Templates: ${templates.value.size}")
                Log.d(TAG, "   📊 Customized: ${customizedCharacters.value.size}")
                Log.d(TAG, "   📊 Total: ${characters.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading initial data: ${e.message}", e)
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
            try {
                Log.d(TAG, "🔄 Refreshing API data...")

                val customizedBefore = customizedCharacters.value.size

                appDataManager.refreshFromApi()

                val customizedAfter = customizedCharacters.value.size

                Log.d(TAG, "✅ API refresh completed")
                Log.d(TAG, "   - Templates: ${templates.value.size}")
                Log.d(TAG, "   - Customized (before): $customizedBefore")
                Log.d(TAG, "   - Customized (after): $customizedAfter")
                Log.d(TAG, "   - Total: ${characters.value.size}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error refreshing API: ${e.message}", e)
            }
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