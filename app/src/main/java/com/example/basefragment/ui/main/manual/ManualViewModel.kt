package com.example.basefragment.ui.main.manual

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.basefragment.data.model.manual.ManualModel

@HiltViewModel
class ManualViewModel @Inject constructor() : ViewModel() {

    // ===== PLAYER 1 STATE =====
    private val _player1List = MutableStateFlow<List<ManualModel>>(List(9) { ManualModel(false) })
    val player1List: StateFlow<List<ManualModel>> = _player1List.asStateFlow()

    // ===== PLAYER 2 STATE =====
    private val _player2List = MutableStateFlow<List<ManualModel>>(List(9) { ManualModel(false) })
    val player2List: StateFlow<List<ManualModel>> = _player2List.asStateFlow()

    /**
     * Update Player 1 selection
     */
    fun updatePlayer1List(list: List<ManualModel>) {
        _player1List.value = list
    }

    /**
     * Update Player 2 selection
     */
    fun updatePlayer2List(list: List<ManualModel>) {
        _player2List.value = list
    }

    /**
     * Get current Player 1 list
     */
    fun getPlayer1List(): List<ManualModel> = _player1List.value

    /**
     * Get current Player 2 list
     */
    fun getPlayer2List(): List<ManualModel> = _player2List.value

    /**
     * Reset all lists - CHỈ GỌI KHI ẤN NEXT Ở MÀN 2
     */

    fun resetAll() {
        _player1List.value = List(9) { ManualModel(false) }
        _player2List.value = List(9) { ManualModel(false) }
    }
    fun reset2() {

        _player2List.value = List(9) { ManualModel(false) }
    }
}