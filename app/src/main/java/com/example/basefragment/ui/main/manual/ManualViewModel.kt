package com.example.basefragment.ui.main.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.core.helper.SharedPreferencesManager
import com.example.basefragment.data.model.manual.ManualModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferencesManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_PLAYER1_LIST = "player1_list"
        private const val KEY_PLAYER2_LIST = "player2_list"
    }

    // ===== PLAYER 1 STATE =====
    private val _player1List = MutableStateFlow<List<ManualModel>>(emptyList())
    val player1List: StateFlow<List<ManualModel>> = _player1List.asStateFlow()

    // ===== PLAYER 2 STATE =====
    private val _player2List = MutableStateFlow<List<ManualModel>>(emptyList())
    val player2List: StateFlow<List<ManualModel>> = _player2List.asStateFlow()

    fun initPlayer1List() {
        if (_player1List.value.isEmpty()) {
            viewModelScope.launch {
                val savedList = savedStateHandle.get<Array<ManualModel>>(KEY_PLAYER1_LIST)

                _player1List.value = if (savedList != null) {
                    savedList.toList()
                } else {
                    List(9) { ManualModel(false) }
                }
            }
        }
    }

    fun updatePlayer1List(list: List<ManualModel>) {
        viewModelScope.launch {
            _player1List.value = list
            savedStateHandle[KEY_PLAYER1_LIST] = list.toTypedArray()
        }
    }

    fun initPlayer2List() {
        if (_player2List.value.isEmpty()) {
            viewModelScope.launch {
                val savedList = savedStateHandle.get<Array<ManualModel>>(KEY_PLAYER2_LIST)

                _player2List.value = if (savedList != null) {
                    savedList.toList()
                } else {
                    List(9) { ManualModel(false) }
                }
            }
        }
    }

    fun updatePlayer2List(list: List<ManualModel>) {
        viewModelScope.launch {
            _player2List.value = list
            savedStateHandle[KEY_PLAYER2_LIST] = list.toTypedArray()
        }
    }

    /**
     * Lấy danh sách Player 1 hiện tại
     */
    fun getCurrentPlayer1List(): List<ManualModel> {
        return _player1List.value.ifEmpty {
            List(9) { ManualModel(false) }
        }
    }

    /**
     * Lấy danh sách Player 2 hiện tại
     */
    fun getCurrentPlayer2List(): List<ManualModel> {
        return _player2List.value.ifEmpty {
            List(9) { ManualModel(false) }
        }
    }

    /**
     * Reset cả 2 danh sách (khi finish game)
     */
    fun resetAllLists() {
        viewModelScope.launch {
            val newList = List(9) { ManualModel(false) }

            _player1List.emit(newList)
            _player2List.emit(newList)

            savedStateHandle[KEY_PLAYER1_LIST] = newList.toTypedArray()
            savedStateHandle[KEY_PLAYER2_LIST] = newList.toTypedArray()
        }
    }

    /**
     * Reset chỉ Player 2 (nếu cần)
     */
    fun resetPlayer2List() {
        viewModelScope.launch {
            val newList = List(9) { ManualModel(false) }
            _player2List.value = newList
            savedStateHandle[KEY_PLAYER2_LIST] = newList.toTypedArray()
        }
    }
}