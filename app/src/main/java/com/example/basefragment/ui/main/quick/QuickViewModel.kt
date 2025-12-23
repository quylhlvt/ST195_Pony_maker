package com.example.basefragment.ui.main.quick

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.model.custom.SelectionPart
import com.example.basefragment.utils.QuickRandomEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickViewModel @Inject constructor(
    private val appDataManager: AppDataManager,
    private val quickRandomManager: QuickRandomManager
) : ViewModel() {

    companion object {
        private const val TAG = "QuickRandomViewModel"
        private const val MAX_WAIT_TIME = 5000L // 5 seconds max wait
    }
    val characters = QuickRandomEngine(appDataManager).getCached()

    private val _quickRandomCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val quickRandomCharacters: StateFlow<List<CustomModel>> = _quickRandomCharacters.asStateFlow()

    // ✅ Flow này sẽ emit ngay khi có item mới được generate
    private val _currentGeneratedCharacters = MutableStateFlow<List<CustomModel>>(emptyList())
    val currentGeneratedCharacters: StateFlow<List<CustomModel>> = _currentGeneratedCharacters.asStateFlow()

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
//        if (characters != null) {
//            _quickRandomCharacters.value = characters
//            _currentGeneratedCharacters.value = characters
//
//        }
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
                _currentGeneratedCharacters.value = characters

                Log.d(TAG, "✅ Loaded ${characters.size} quick random characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading quick random: ${e.message}", e)
            }
        }
    }

    /**
     * ✅ Wait for templates to be ready before generating
     */
    private suspend fun waitForTemplates(): Boolean {
        var waitTime = 0L
        val checkInterval = 100L

        while (appDataManager.templates.value.isEmpty() && waitTime < MAX_WAIT_TIME) {
            Log.d(TAG, "⏳ Waiting for templates... ($waitTime ms)")
            delay(checkInterval)
            waitTime += checkInterval
        }

        val hasTemplates = appDataManager.templates.value.isNotEmpty()
        if (hasTemplates) {
            Log.d(TAG, "✅ Templates ready: ${appDataManager.templates.value.size}")
        } else {
            Log.e(TAG, "❌ Timeout waiting for templates")
        }

        return hasTemplates
    }

    /**
     * ✅ Generate quick random characters với real-time display
     * Mỗi khi generate xong 1 item, emit ngay lập tức
     */
    suspend fun generateQuickRandomCharacters() {
        if (_isGenerating.value) {
            Log.d(TAG, "⚠️ Already generating, skip")
            return
        }

        _isGenerating.value = true

        try {
            // ✅ Đợi templates load xong
            if (!waitForTemplates()) {
                Log.e(TAG, "❌ Cannot generate: templates not available")
                _isGenerating.value = false
                return
            }

            val generatedList = mutableListOf<CustomModel>()
            val templates = appDataManager.templates.value
            val total = 10

            for (i in 0 until total) {
                try {
                    // Generate 1 character với selections ngẫu nhiên
                    val template = templates.random()
                    val characterWithSelections = generateSingleCharacter(template)

                    // ✅ MỚI: Render ảnh ngẫu nhiên và lưu vào imageSave
                    val imagePath = quickRandomManager.generateRandomImage(characterWithSelections)  // Giả sử method này tồn tại trong QuickRandomManager
                    val characterWithImage = characterWithSelections.copy(imageSave = imagePath)

                    generatedList.add(characterWithImage)

                    // ✅ EMIT NGAY: Hiển thị item ngay khi generate xong
                    _currentGeneratedCharacters.value = generatedList.toList()

                    // Update progress
                    _generationProgress.value = GenerationProgress(
                        current = i + 1,
                        total = total,
                        templateName = template.id,
                        step = "Generated ${i + 1}/$total"
                    )

                    Log.d(TAG, "✅ Generated and displayed character ${i + 1}/$total: ${characterWithImage.id}, imageSave: ${characterWithImage.imageSave}")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error generating character $i: ${e.message}", e)
                }
            }

            // ✅ Save toàn bộ list sau khi generate xong
            try {
                quickRandomManager.saveQuickRandomToJson(generatedList)
                _quickRandomCharacters.value = generatedList
                _generationProgress.value = GenerationProgress(total, total, "", "Completed")
                Log.d(TAG, "✅ Generation completed: ${generatedList.size} characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving generated characters: ${e.message}", e)
            }
        } finally {
            _isGenerating.value = false
        }
    }

    /**
     * ✅ Regenerate - dùng cho pull-to-refresh
     * Clear trước rồi mới generate
     */
    suspend fun regenerateQuickRandomCharacters() {
        if (_isGenerating.value) {
            Log.d(TAG, "⚠️ Already generating, skip")
            return
        }

        _isGenerating.value = true

        try {
            // ✅ Đợi templates load xong
            if (!waitForTemplates()) {
                Log.e(TAG, "❌ Cannot regenerate: templates not available")
                _isGenerating.value = false
                return
            }

            // ✅ Clear UI ngay
            _currentGeneratedCharacters.value = emptyList()

            val generatedList = mutableListOf<CustomModel>()
            val templates = appDataManager.templates.value
            val total = 30

            for (i in 0 until total) {
                try {
                    // Generate 1 character
                    val template = templates.random()
                    val character = generateSingleCharacter(template)
                    generatedList.add(character)

                    // ✅ EMIT NGAY: Hiển thị item ngay khi generate xong
                    _currentGeneratedCharacters.value = generatedList.toList()

                    // Update progress
                    _generationProgress.value = GenerationProgress(
                        current = i + 1,
                        total = total,
                        templateName = template.id,
                        step = "Regenerating ${i + 1}/$total"
                    )

                    Log.d(TAG, "✅ Regenerated character ${i + 1}/$total: ${character.id}")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error regenerating character $i: ${e.message}", e)
                }
            }

            // ✅ Save toàn bộ list sau khi generate xong
            try {
                quickRandomManager.saveQuickRandomToJson(generatedList)
                _quickRandomCharacters.value = generatedList
                _generationProgress.value = GenerationProgress(total, total, "", "Completed")
                Log.d(TAG, "✅ Regeneration completed: ${generatedList.size} characters")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving regenerated characters: ${e.message}", e)
            }
        } finally {
            _isGenerating.value = false
        }
    }

    /**
     * ✅ Generate single character từ template
     */
    private fun generateSingleCharacter(template: CustomModel): CustomModel {
        val randomSelections = template.listPath.mapIndexed { index, bodyPart ->
            val colorIndex = (1 until bodyPart.listPath.size).random()
            val layerIndex = (2 until bodyPart.listPath[colorIndex].listPath.size).random()

            SelectionPart(index, colorIndex, layerIndex)

    }

        return template.copy(
            id = "quick_${System.currentTimeMillis()}_${(0..999).random()}",
            selections = ArrayList(randomSelections),
            imageSave = "",
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * ✅ Clear all quick random characters
     */
    fun clearQuickRandomCharacters() {
        viewModelScope.launch {
            try {
                quickRandomManager.clearQuickRandomCharacters()
                _quickRandomCharacters.value = emptyList()
                _currentGeneratedCharacters.value = emptyList()
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
                    _currentGeneratedCharacters.value = currentList

                    quickRandomManager.saveQuickRandomToJson(currentList)

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