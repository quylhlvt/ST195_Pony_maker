package com.example.basefragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.CharacterConfiguration
import com.example.basefragment.data.model.custom.CustomModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class ViewModelActivity  @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    val characters: StateFlow<List<CustomModel>> = appDataManager.characters
    val backgrounds: StateFlow<List<String>> = appDataManager.backgrounds
    val stickers: StateFlow<List<String>> = appDataManager.stickers
    val isLoading: StateFlow<Boolean> = appDataManager.isLoading
    val loadError: StateFlow<String?> = appDataManager.loadError

    // Current character configuration (dùng chung cho custom flow)
    private val _currentConfiguration = MutableStateFlow<CharacterConfiguration?>(null)
    val currentConfiguration: StateFlow<CharacterConfiguration?> = _currentConfiguration.asStateFlow()

    init {
        // Load data ngay khi ViewModel được khởi tạo
        loadAppData()
    }

    /**
     * Load toàn bộ data của app
     */
    private fun loadAppData() {
        viewModelScope.launch {
            appDataManager.loadAllAppData()
        }
    }

    /**
     * Reload data (nếu cần refresh)
     */
    fun reloadData() {
        viewModelScope.launch {
            appDataManager.reloadData()
        }
    }

    /**
     * Set character configuration đang được customize
     */
    fun setCurrentConfiguration(config: CharacterConfiguration) {
        _currentConfiguration.value = config
    }

    /**
     * Get character theo ID
     */
    fun getCharacterById(id: String): CustomModel? {
        return appDataManager.getCharacterById(id)
    }

    /**
     * Get character theo index
     */
    fun getCharacterByIndex(index: Int): CustomModel? {
        return appDataManager.getCharacterByIndex(index)
    }

    /**
     * Clear current configuration
     */
    fun clearConfiguration() {
        _currentConfiguration.value = null
    }
}