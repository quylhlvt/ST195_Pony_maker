package com.example.basefragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.usecase.GetCatalogueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelActivity @Inject constructor(
    private val getCatalogueUseCase: GetCatalogueUseCase,
    private val appDataManager: AppDataManager
) : ViewModel() {

    // ✅ Combined list (templates + customized)
    private val _characters = MutableStateFlow<List<CustomModel>>(emptyList())
    val characters: StateFlow<List<CustomModel>> = _characters.asStateFlow()

    // ✅ Templates only (cho template selector)
    private val _templates = MutableStateFlow<List<CustomModel>>(emptyList())
    val templates: StateFlow<List<CustomModel>> = _templates.asStateFlow()

    // ✅ Customized only (cho MyPony screen)
    private val _customizedCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val customizedCharacters: StateFlow<List<CustomModel>> = _customizedCharacters.asStateFlow()

    private val _backgrounds = MutableStateFlow<List<String>>(emptyList())
    val backgrounds: StateFlow<List<String>> = _backgrounds.asStateFlow()

    private val _backgroundTexts = MutableStateFlow<List<String>>(emptyList())
    val backgroundTexts: StateFlow<List<String>> = _backgroundTexts.asStateFlow()

    private val _stickers = MutableStateFlow<List<String>>(emptyList())
    val stickers: StateFlow<List<String>> = _stickers.asStateFlow()

    private val _speechs = MutableStateFlow<List<String>>(emptyList())
    val speechs: StateFlow<List<String>> = _speechs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _selectedCharacterPath = MutableStateFlow<String?>(null)
    val selectedCharacterPath: StateFlow<String?> = _selectedCharacterPath.asStateFlow()

    private val _savedImagePath = MutableStateFlow<String?>(null)
    val savedImagePath: StateFlow<String?> = _savedImagePath.asStateFlow()

    private val _selectedCharacterId = MutableStateFlow<String?>(null)
    val selectedCharacterId: StateFlow<String?> = _selectedCharacterId.asStateFlow()


    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                appDataManager.loadInitialData()

                // ✅ Collect all data streams
                launch {
                    appDataManager.characters.collect { _characters.value = it }
                }
                launch {
                    appDataManager.templates.collect { _templates.value = it }
                }
                launch {
                    appDataManager.customizedCharacters.collect {
                        _customizedCharacters.value = it
                    }
                }
                launch {
                    appDataManager.backgrounds.collect { _backgrounds.value = it }
                }
                launch {
                    appDataManager.backgroundTexts.collect { _backgroundTexts.value = it }
                }
                launch {
                    appDataManager.stickers.collect { _stickers.value = it }
                }
                launch {
                    appDataManager.speechs.collect { _speechs.value = it }
                }
                launch {
                    appDataManager.isLoading.collect { _isLoading.value = it }
                }
                launch {
                    appDataManager.error.collect { _error.value = it }
                }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshApiData() {
        viewModelScope.launch {
            appDataManager.refreshFromApi()
        }
    }

    fun forceReloadAll() {
        viewModelScope.launch {
            appDataManager.forceReloadAll()
        }
    }

    /**
     * ✅ Get character by index (from combined list)
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        val character = _characters.value.getOrNull(index)
        Log.d("ViewModelActivity", "getCharacterByIndex($index): ${character?.id}")
        return character
    }

    /**
     * ✅ Get character by ID
     */
    fun getCharacterById(characterId: String): CustomModel? {
        return appDataManager.getCharacterById(characterId)
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
            // ✅ Nếu là template, tạo character mới
            if (isTemplate(character.id)) {
                val newCharacter = character.copy(
                    id = java.util.UUID.randomUUID().toString(),  // ✅ ID mới
                    updatedAt = System.currentTimeMillis()
                )
                appDataManager.updateCustomizedCharacter(newCharacter)
                Log.d("ViewModelActivity", "✅ Created new character from template: ${newCharacter.id}")
            } else {
                // ✅ Update existing customized character
                val updatedCharacter = character.copy(
                    updatedAt = System.currentTimeMillis()
                )
                appDataManager.updateCustomizedCharacter(updatedCharacter)
                Log.d("ViewModelActivity", "✅ Updated customized character: ${updatedCharacter.id}")
            }
        }
    }

    /**
     * ✅ Delete customized character (không thể xóa template)
     */
    fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            if (isTemplate(characterId)) {
                Log.w("ViewModelActivity", "⚠️ Cannot delete template: $characterId")
                return@launch
            }
            appDataManager.deleteCustomizedCharacter(characterId)
            Log.d("ViewModelActivity", "✅ Deleted character: $characterId")
        }
    }

    fun clearData() {
        appDataManager.clearData()
        _characters.value = emptyList()
        _templates.value = emptyList()
        _customizedCharacters.value = emptyList()
        _backgrounds.value = emptyList()
        _backgroundTexts.value = emptyList()
        _stickers.value = emptyList()
        _speechs.value = emptyList()
    }

    fun getCharacterIndexById(characterId: String): Int {
        return _characters.value.indexOfFirst { it.id == characterId }
    }

    fun getTemplateById(templateId: String): CustomModel? {
        return _templates.value.find { it.id == templateId }
    }
}