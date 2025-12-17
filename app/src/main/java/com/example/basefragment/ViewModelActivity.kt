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
class ViewModelActivity @Inject constructor( private  val  getCatalogueUseCase: GetCatalogueUseCase, private val  appDataManager: AppDataManager) : ViewModel() {

    // Data StateFlows - load trong init{}
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
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 🔥 Load tất cả data ngay khi ViewModel được tạo
        loadInitialData()
    }

    /**
     * Load data lần đầu (tự động gọi trong init)
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load data từ AppDataManager
                appDataManager.loadInitialData()

                // Collect data từ AppDataManager và update local StateFlows
                launch {
                    appDataManager.characters.collect { _characters.value = it }
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

    /**
     * Refresh chỉ API data (không load lại assets)
     */
    fun refreshApiData() {
        viewModelScope.launch {
            appDataManager.refreshFromApi()
        }
    }

    /**
     * Force reload toàn bộ (hiếm khi cần)
     */
    fun forceReloadAll() {
        viewModelScope.launch {
            appDataManager.forceReloadAll()
        }
    }

    /**
     * Get character by index
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        val character = _characters.value.getOrNull(index)
        Log.d("ViewModelActivity", "getCharacterByIndex($index): ${character?.listPath?.size} parts, total=${_characters.value.size}")
        return character
    }

    /**
     * Clear all data
     */
    fun clearData() {
        appDataManager.clearData()
        _characters.value = emptyList()
        _backgrounds.value = emptyList()
        _backgroundTexts.value = emptyList()
        _stickers.value = emptyList()
        _speechs.value = emptyList()
    }

    /* Update data Custom*/
    fun updateOrAddCharacter(
        character: CustomModel,
        index: Int = -1
    ) {
        val currentList = _characters.value.toMutableList()

        if (index >= 0 && index < currentList.size) {
            // update
            currentList[index] = character
            Log.d("ViewModelActivity", "updateCharacter at index=$index")
        } else {
            // add new
            currentList.add(character)
            Log.d("ViewModelActivity", "addNewCharacter, size=${currentList.size}")
        }

        _characters.value = currentList

        // persist xuống local json
        viewModelScope.launch {
            appDataManager.saveCharactersToJson(currentList)
        }
    }
    fun getCharacterById(characterId: String): CustomModel? {
        val character = _characters.value.find { it.id == characterId }
        return character
    }
    fun getCharacterIndexById(characterId: String): Int {
        val index = _characters.value.indexOfFirst { it.id == characterId }
        return index // trả về -1 nếu không tìm thấy
    }
    fun getTemplateById(templateId: String): CustomModel? {
        // Có 3 cách implement, bạn chọn cách phù hợp
        return _characters.value.find { it.id == templateId }
    }
}