package com.example.basefragment.ui.main.customize

import android.util.Log
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
    private var originalBodyParts: List<BodyPartModel> = emptyList()

    /** Khởi tạo character từ Fragment */
    fun initCharacter(
        mainViewModel: ViewModelActivity,
        index: Int? = null,
        template: CustomModel? = null
    ) {
        Log.d("CustomizeViewModel", "initCharacter called - index=$index")

        when {
            index != null -> {
                val character = mainViewModel.getCharacterByIndex(index)
                character?.let {
                    _currentCharacter.value = it

                    // ✅ Deep copy để giữ nguyên data gốc
                    originalBodyParts = it.listPath.map { bp ->
                        bp.copy(listPath = ArrayList(bp.listPath.map { color ->
                            color.copy(listPath = ArrayList(color.listPath))
                        }))
                    }

                    // ✅ Emit selectedBodyParts CHỈ 1 lần
                    _selectedBodyParts.value = originalBodyParts
                    characterIndex = index

                    Log.d("CustomizeViewModel", "Emitted ${it.listPath.size} body parts")
                }
            }
            template != null -> {
                val newCharacter = template.copy(listPath = ArrayList(template.listPath))
                _currentCharacter.value = newCharacter

                originalBodyParts = newCharacter.listPath.map { bp ->
                    bp.copy(listPath = ArrayList(bp.listPath.map { color ->
                        color.copy(listPath = ArrayList(color.listPath))
                    }))
                }

                _selectedBodyParts.value = originalBodyParts
                characterIndex = -1
            }
        }
    }

    /** Cập nhật body part */
    fun updateBodyPart(bodyPart: BodyPartModel, partIndex: Int) {
        val character = _currentCharacter.value ?: return
        val list = character.listPath.toMutableList()

        if (partIndex in list.indices) {
            list[partIndex] = bodyPart

            // ✅ CHỈ update currentCharacter để preview
            _currentCharacter.value = character.copy(listPath = ArrayList(list))

            Log.d("CustomizeViewModel", "Updated bodyPart at index $partIndex (currentCharacter only)")

            // ❌ KHÔNG update selectedBodyParts
            // _selectedBodyParts.value = list
        }
    }

    fun resetCharacter() {
        val character = _currentCharacter.value ?: return

        // ✅ Reset từ originalBodyParts (có đầy đủ màu)
        val resetParts = originalBodyParts.map { bodyPart ->
            val firstColor = bodyPart.listPath.firstOrNull()
            bodyPart.copy(listPath = arrayListOf(firstColor ?: ColorModel("", arrayListOf())))
        }

        _currentCharacter.value = character.copy(listPath = ArrayList(resetParts))

    }

    fun randomizeCharacter() {
        val character = _currentCharacter.value ?: return

        // ✅ Random từ originalBodyParts (có đầy đủ màu)
        val randomParts = originalBodyParts.map { bodyPart ->
            val randomColor = bodyPart.listPath.randomOrNull()
            bodyPart.copy(listPath = arrayListOf(randomColor ?: ColorModel("", arrayListOf())))
        }

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

