package com.example.basefragment.ui.main.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.data.model.custom.CustomModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
@HiltViewModel
class CustomizeViewModel @Inject constructor() : ViewModel() {

    private val _currentCharacter = MutableStateFlow<CustomModel?>(null)
    val currentCharacter: StateFlow<CustomModel?> = _currentCharacter.asStateFlow()

    private val _selectedBodyParts = MutableStateFlow<List<BodyPartModel>>(emptyList())
    val selectedBodyParts: StateFlow<List<BodyPartModel>> = _selectedBodyParts.asStateFlow()

    private var characterIndex: Int = -1

    /** Khởi tạo character từ Fragment */
    fun initCharacter(
        mainViewModel: ViewModelActivity,
        index: Int? = null,
        template: CustomModel? = null
    ) {
        when {
            index != null -> {
                val character = mainViewModel.getCharacterByIndex(index)
                character?.let {
                    _currentCharacter.value = it
                    _selectedBodyParts.value = it.listPath
                    characterIndex = index
                }
            }
            template != null -> {
                val newCharacter = template.copy(listPath = ArrayList(template.listPath))
                _currentCharacter.value = newCharacter
                _selectedBodyParts.value = newCharacter.listPath
                characterIndex = -1
            }
        }
    }

    /** Cập nhật body part */
    fun updateBodyPart(bodyPart: BodyPartModel, partIndex: Int) {
        val character = _currentCharacter.value ?: return
        val list = character.listPath.toMutableList()
        if (partIndex in list.indices) list[partIndex] = bodyPart
        _selectedBodyParts.value = list
        _currentCharacter.value = character.copy(listPath = ArrayList(list))
    }

    fun resetCharacter() {
        val character = _currentCharacter.value ?: return
        val resetParts = character.listPath.map { bodyPart ->
            val firstColor = bodyPart.listPath.firstOrNull()
            bodyPart.copy(listPath = arrayListOf(firstColor ?: ColorModel("", arrayListOf())))
        }
        _selectedBodyParts.value = resetParts
        _currentCharacter.value = character.copy(listPath = ArrayList(resetParts))
    }

    fun randomizeCharacter() {
        val character = _currentCharacter.value ?: return
        val randomParts = character.listPath.map { bodyPart ->
            val randomColor = bodyPart.listPath.randomOrNull() ?: bodyPart.listPath.firstOrNull()
            bodyPart.copy(listPath = arrayListOf(randomColor ?: ColorModel("", arrayListOf())))
        }
        _selectedBodyParts.value = randomParts
        _currentCharacter.value = character.copy(listPath = ArrayList(randomParts))
    }

    /** Lưu character vào MainViewModel */
    fun saveCharacter(mainViewModel: ViewModelActivity) {
        val character = _currentCharacter.value ?: return
        viewModelScope.launch {
            val updatedList = mainViewModel.characters.value.toMutableList()
            if (characterIndex >= 0 && characterIndex < updatedList.size) {
                updatedList[characterIndex] = character
            } else {
                updatedList.add(character)
            }
            mainViewModel.updateCharacters(updatedList)
        }
    }
}

