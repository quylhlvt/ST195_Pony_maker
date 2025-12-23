package com.example.basefragment.ui.main.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.CustomModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeViewModel @Inject constructor(
    private val appDataManager: AppDataManager
) : ViewModel() {

    private val _selectedCharacterIndex = MutableStateFlow(0)
    private val _hotTrendPreset = MutableStateFlow<List<List<Int>>?>(null)

    private val _currentCharacter = MutableStateFlow<CustomModel?>(null)
    val currentCharacter: StateFlow<CustomModel?> = _currentCharacter.asStateFlow()

    private val _selectedNavPosition = MutableStateFlow(0)
    val selectedNavPosition: StateFlow<Int> = _selectedNavPosition.asStateFlow()

    private val _layerStates = MutableStateFlow<List<LayerState>>(emptyList())
    val layerStates: StateFlow<List<LayerState>> = _layerStates.asStateFlow()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _isControlsHidden = MutableStateFlow(false)
    val isControlsHidden: StateFlow<Boolean> = _isControlsHidden.asStateFlow()

    private val _randomCount = MutableStateFlow(0)
    val randomCount: StateFlow<Int> = _randomCount.asStateFlow()

    private val _showColorPanelForNav = MutableStateFlow<List<Boolean>>(emptyList())
    val showColorPanelForNav: StateFlow<List<Boolean>> = _showColorPanelForNav.asStateFlow()

    data class LayerState(
        var layerPosition: Int = 0,
        var colorPosition: Int = 0
    )

    fun initialize(characterIndex: Int, hotTrendPreset: List<List<Int>>? = null) {
        _selectedCharacterIndex.value = characterIndex
        _hotTrendPreset.value = hotTrendPreset

        viewModelScope.launch {
            appDataManager.characters.collect { characters ->
                val character = characters.getOrNull(characterIndex)
                if (character != null && _currentCharacter.value != character) {
                    _currentCharacter.value = character
                    setupInitialLayerStates(character, hotTrendPreset)
                    _selectedNavPosition.value = 0
                    _randomCount.value = 0
                }
            }
        }
    }

    private fun setupInitialLayerStates(character: CustomModel, preset: List<List<Int>>?) {
        val states = mutableListOf<LayerState>()
        val showColorList = mutableListOf<Boolean>()

        character.listPath.forEachIndexed { navIndex, part ->
            val defaultLayer = if (preset != null && navIndex < preset.size) {
                preset[navIndex][0].coerceIn(0, part.listPath.flatMap { it.listPath }.size - 1)
            } else if (navIndex == 0) 1 else 0

            val defaultColor = if (preset != null && navIndex < preset.size) {
                preset[navIndex][1].coerceIn(0, part.listPath.size - 1)
            } else 0

            states.add(LayerState(defaultLayer, defaultColor))
            showColorList.add(part.listPath.size > 1) // Chỉ hiện nếu có >1 màu
        }

        _layerStates.value = states
        _showColorPanelForNav.value = showColorList
    }

    fun selectNav(position: Int) {
        if (_currentCharacter.value?.listPath?.indices?.contains(position) == true) {
            _selectedNavPosition.value = position
        }
    }

    fun updateLayerPosition(navPos: Int, newLayerPos: Int) {
        val current = _layerStates.value.toMutableList()
        if (navPos in current.indices) {
            current[navPos].layerPosition = newLayerPos.coerceIn(0, Int.MAX_VALUE)
            _layerStates.value = current
        }
    }

    fun updateColorPosition(navPos: Int, newColorPos: Int) {
        val current = _layerStates.value.toMutableList()
        if (navPos in current.indices) {
            current[navPos].colorPosition = newColorPos.coerceIn(0, Int.MAX_VALUE)
            _layerStates.value = current
        }
    }

    fun toggleFlip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun toggleControls() {
        _isControlsHidden.value = !_isControlsHidden.value
    }

    fun toggleColorPanel(navPos: Int) {
        val current = _showColorPanelForNav.value.toMutableList()
        if (navPos in current.indices) {
            current[navPos] = !current[navPos]
            _showColorPanelForNav.value = current
        }
    }

    fun resetAll() {
        val character = _currentCharacter.value ?: return
        val newStates = character.listPath.mapIndexed { index, _ ->
            LayerState(
                layerPosition = if (index == 0) 1 else 0,
                colorPosition = 0
            )
        }
        _layerStates.value = newStates
        _randomCount.value = 0
        _selectedNavPosition.value = 0
    }

    fun randomizeAll() {
        val character = _currentCharacter.value ?: return
        val newStates = _layerStates.value.toMutableList()

        character.listPath.forEachIndexed { navIndex, part ->
            val newColorPos = if (part.listPath.size > 1) {
                (0 until part.listPath.size).random()
            } else 0
            newStates[navIndex].colorPosition = newColorPos

            val layers = part.listPath[newColorPos].listPath
            val start = if (layers.getOrNull(0) == "none") 2 else 1
            val randomLayerPos = if (layers.size > start) {
                (start until layers.size).random()
            } else start
            newStates[navIndex].layerPosition = randomLayerPos
        }

        _layerStates.value = newStates
        _randomCount.value = (_randomCount.value + 1).coerceAtMost(3)
    }

    fun getCurrentPart(): BodyPartModel? {
        val navPos = _selectedNavPosition.value
        return _currentCharacter.value?.listPath?.getOrNull(navPos)
    }

    fun canRandomMore(): Boolean = _randomCount.value < 3
}