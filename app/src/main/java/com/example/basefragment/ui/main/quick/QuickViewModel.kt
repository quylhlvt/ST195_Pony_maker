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
        private const val TAG = "QuickRandomViewModel"
    }

    private val _quickRandomCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val quickRandomCharacters: StateFlow<List<CustomModel>> = _quickRandomCharacters.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow(GenerationProgress(0, 0, "", ""))
    val generationProgress: StateFlow<GenerationProgress> = _generationProgress.asStateFlow()

    data class GenerationProgress(
        val current: Int,
        val total: Int,
        val templateName: String,
        val step: String
    )

    init {
        loadQuickRandomCharacters()
    }

    /**
     * ✅ Load quick random characters từ JSON
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
     * ✅ Generate quick random characters
     */
    fun generateQuickRandomCharacters() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val characters = quickRandomManager.generateQuickRandomCharacters { current, total, templateName ->
                    _generationProgress.value = GenerationProgress(current, total, templateName, "")
                }

                _quickRandomCharacters.value = characters
                Log.d(TAG, "✅ Generated ${characters.size} quick random characters")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error generating quick random: ${e.message}", e)
            } finally {
                _isGenerating.value = false
                _generationProgress.value = GenerationProgress(0, 0, "", "")            }
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

                    // Delete image
                    if (character.imageSave.isNotEmpty()) {
                        // You need to add this method to QuickRandomManager
                        // quickRandomManager.deleteImage(character.imageSave)
                    }

                    Log.d(TAG, "✅ Deleted quick random character: $characterId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting character: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Refresh quick random characters
     */
    fun refreshQuickRandomCharacters() {
        loadQuickRandomCharacters()
    }

    /**
     * ✅ Check if quick random data exists
     */
    suspend fun hasQuickRandomData(): Boolean {
        return quickRandomManager.hasQuickRandomData()
    }
}