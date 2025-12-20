package com.example.basefragment.ui.main.quick

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.QuickRandomManager
import com.example.basefragment.data.model.custom.CustomModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickViewModel @Inject constructor(
    private val quickRandomManager: QuickRandomManager
) : ViewModel() {

    companion object {
        private const val TAG = "QuickViewModel"
    }

    // ✅ Quick random characters
    private val _quickRandomCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val quickRandomCharacters: StateFlow<List<CustomModel>> = _quickRandomCharacters.asStateFlow()

    // ✅ Loading state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        // ✅ Load data đã có sẵn
        loadQuickRandomCharacters()

        // ✅ 🔥 Observe real-time generation stream
        observeGeneratingCharacters()
    }

    /**
     * ✅ Load quick random characters từ JSON (data đã generate sẵn)
     */
    private fun loadQuickRandomCharacters() {
        viewModelScope.launch {
            try {
                val characters = quickRandomManager.loadQuickRandomCharacters()
                _quickRandomCharacters.value = characters
                Log.d(TAG, "✅ Loaded ${characters.size} quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading quick random: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ 🔥 Observe real-time generation - Update UI ngay khi có character mới
     */
    private fun observeGeneratingCharacters() {
        viewModelScope.launch {
            quickRandomManager.generatingCharacters.collect { characters ->
                if (characters.isNotEmpty()) {
                    _quickRandomCharacters.value = characters
                    Log.d(TAG, "🔥 Real-time update: ${characters.size} characters")
                }
            }
        }
    }

    /**
     * ✅ Refresh data (reload từ JSON)
     */
    fun refreshQuickRandomCharacters() {
        loadQuickRandomCharacters()
    }

    /**
     * ✅ Delete một quick random character
     */
    fun deleteQuickRandomCharacter(characterId: String) {
        viewModelScope.launch {
            try {
                val currentList = _quickRandomCharacters.value.toMutableList()
                val character = currentList.find { it.id == characterId }

                if (character != null) {
                    currentList.remove(character)
                    _quickRandomCharacters.value = currentList

                    // Save lại JSON
                    quickRandomManager.saveQuickRandomToJson(currentList)

                    Log.d(TAG, "✅ Deleted quick random character: $characterId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting character: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Regenerate quick random (nếu user muốn tạo lại)
     */
    fun regenerateQuickRandomCharacters() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val characters = quickRandomManager.generateQuickRandomCharacters()
                _quickRandomCharacters.value = characters
                Log.d(TAG, "✅ Regenerated ${characters.size} quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error regenerating quick random: ${e.message}", e)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * ✅ Clear all quick random characters
     */
    fun clearQuickRandomCharacters() {
        viewModelScope.launch {
            try {
                quickRandomManager.clearQuickRandomCharacters()
                _quickRandomCharacters.value = emptyList()
                Log.d(TAG, "✅ Cleared quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error clearing quick random: ${e.message}", e)
            }
        }
    }
}