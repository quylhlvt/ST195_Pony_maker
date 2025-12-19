package com.example.basefragment.ui.main.customize

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.basefragment.ViewModelActivity
import com.example.basefragment.data.model.custom.BodyPartModel
import com.example.basefragment.data.model.custom.ColorModel
import com.example.basefragment.data.model.custom.CustomModel
import com.example.basefragment.data.model.custom.SelectionPart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CustomizeViewModel @Inject constructor() : ViewModel() {

    private val _currentCharacter = MutableStateFlow<CustomModel?>(null)
    val currentCharacter: StateFlow<CustomModel?> = _currentCharacter.asStateFlow()

    private val _selectedBodyParts = MutableStateFlow<List<BodyPartModel>>(emptyList())
    val selectedBodyParts: StateFlow<List<BodyPartModel>> = _selectedBodyParts.asStateFlow()

    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val navSelections = mutableListOf<SelectionPart>()

    private val _triggerLayerUpdate = MutableStateFlow(0)
    val triggerLayerUpdate: StateFlow<Int> = _triggerLayerUpdate.asStateFlow()

    private val _triggerColorUpdate = MutableStateFlow(0)
    val triggerColorUpdate: StateFlow<Int> = _triggerColorUpdate.asStateFlow()

    private var baseCharacter: CustomModel? = null
    private var characterIndex: Int = -1
    private var originalBodyParts: List<BodyPartModel> = emptyList()

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    fun toggleFlip() {
        _isFlipped.value = !_isFlipped.value
    }

    fun initCharacter(
        mainViewModel: ViewModelActivity,
        index: Int? = null,
        template: CustomModel? = null
    ) {
        val character = when {
            index != null && index >= 0 -> {
                characterIndex = index
                mainViewModel.getCharacterByIndex(index)
            }
            template != null -> {
                characterIndex = -1
                template
            }
            else -> null
        } ?: return

        // ✅ KHÔNG sort, giữ nguyên thứ tự nav trong listPath
        val sortedListPath = character.listPath.sortedBy { it.navOrder }
        _currentCharacter.value = character.copy(listPath = ArrayList(sortedListPath))

        // ✅ Deep copy toàn bộ dữ liệu gốc (GIỮ TẤT CẢ colors và layers)
        originalBodyParts = sortedListPath.map { bp ->
            bp.copy(
                listPath = ArrayList(
                    bp.listPath.map { color ->
                        color.copy(listPath = ArrayList(color.listPath))
                    }
                )
            )
        }

        Log.d("CustomizeViewModel", "=== Init character ===")
        Log.d("CustomizeViewModel", "   ID: ${character.id}")
        Log.d("CustomizeViewModel", "   Has selections: ${character.selections.isNotEmpty()}")

        baseCharacter = character
        _selectedBodyParts.value = originalBodyParts

        // ✅ Restore selections từ character.selections
        navSelections.clear()
        if (character.selections.isNotEmpty()) {
            // Edit mode: restore selections
            Log.d("CustomizeViewModel", "   📋 Restoring selections from saved character")
            character.selections.forEach { selection ->
                navSelections.add(selection)
                Log.d("CustomizeViewModel", "      Nav ${selection.nav}: color=${selection.color}, layer=${selection.layer}")
            }
        } else {
            // New character: initialize empty selections
            Log.d("CustomizeViewModel", "   📋 New character, initializing empty selections")
            originalBodyParts.forEachIndexed { navIndex, _ ->
                navSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
            }
        }

        _currentNavIndex.value = 0

        // Auto-select cho nav 0 nếu chưa có selection
        val firstSelection = navSelections.getOrNull(0)
        if (firstSelection != null && firstSelection.layer == -1) {
            autoSelectFirstVariantForNav(0)
        }

        // ✅ Apply preview cho tất cả nav đã có selection
        applyAllSelections()
    }

    private fun applyAllSelections() {
        navSelections.forEachIndexed { navIndex, selection ->
            if (selection.layer != -1) {
                val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return@forEachIndexed
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

                if (hasColor && selection.color != -1) {
                    applyPreview(navIndex, selection.color, selection.layer)
                } else if (!hasColor) {
                    applyPreviewSingleImage(navIndex, selection.layer)
                }
            }
        }
    }

    private fun autoSelectFirstVariantForNav(navIndex: Int) {
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return
        if (bodyPart.listPath.isEmpty()) return

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (hasColor) {
            navSelections[navIndex] = SelectionPart(nav = navIndex, color = 0, layer = 0)
            applyPreview(navIndex, 0, 0)
        } else {
            val position = bodyPart.position.toIntOrNull() ?: 0
            val realImagesCount = bodyPart.listPath.sumOf { cm ->
                cm.listPath.count { it.contains("/") }
            }

            if (realImagesCount > 0) {
                val firstRealIndex = if (position != 1) 2 else 1
                navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = firstRealIndex)
                applyPreviewSingleImage(navIndex, firstRealIndex)
            }
        }
    }

    fun selectNav(navIndex: Int) {
        _currentNavIndex.value = navIndex

        if (navIndex != 0) {
            val selection = navSelections.getOrNull(navIndex)
            if (selection?.layer == -1 && selection?.color == -1) {
                autoSelectFirstVariantForNav(navIndex)
            }
        }
    }

    fun selectColor(colorIndex: Int, currentLayerIndex: Int) {
        val navIndex = _currentNavIndex.value
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val newColor = bodyPart.listPath.getOrNull(colorIndex) ?: return
        val maxLayer = newColor.listPath.size - 1
        val safeLayerIndex = currentLayerIndex.coerceIn(0, maxLayer)

        navSelections[navIndex] = SelectionPart(nav = navIndex, color = colorIndex, layer = safeLayerIndex)
        applyPreview(navIndex, colorIndex, safeLayerIndex)
        _triggerLayerUpdate.value += 1
    }

    fun selectLayer(layerIndex: Int) {
        val navIndex = _currentNavIndex.value
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            val position = bodyPart.position.toIntOrNull() ?: 0
            val layerImages = mutableListOf<String>()
            if (position != 1) layerImages.add("none")
            layerImages.add("dice")
            layerImages.addAll(
                bodyPart.listPath.mapNotNull { it.listPath.firstOrNull { p -> p.contains("/") } }
            )

            val selectedVariant = layerImages.getOrNull(layerIndex)

            when (selectedVariant) {
                "dice" -> {
                    val realImages = layerImages.filter { it != "none" && it != "dice" }
                    if (realImages.isNotEmpty()) {
                        val realStartIndex = if (position != 1) 2 else 1
                        val randomOffset = Random.nextInt(realImages.size)
                        val fixedLayerIndex = realStartIndex + randomOffset

                        navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = fixedLayerIndex)
                        applyPreviewSingleImage(navIndex, fixedLayerIndex)
                    }
                    _triggerLayerUpdate.value += 1
                    return
                }
                "none" -> {
                    navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = 0)
                    applyPreviewSingleImage(navIndex, 0)
                    _triggerLayerUpdate.value += 1
                    return
                }
                else -> {
                    navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = layerIndex)
                    applyPreviewSingleImage(navIndex, layerIndex)
                    _triggerLayerUpdate.value += 1
                    return
                }
            }
        }

        val currentSelection = navSelections.getOrNull(navIndex) ?: SelectionPart(navIndex, 0, 0)
        val actualColorIndex = if (currentSelection.color == -1) 0 else currentSelection.color
        val color = bodyPart.listPath.getOrNull(actualColorIndex) ?: return

        val layerPath = color.listPath.getOrNull(layerIndex)

        if (layerPath == "dice") {
            val realLayers = color.listPath.withIndex()
                .filter { it.value != "none" && it.value != "dice" && it.value.contains("/") }

            if (realLayers.isNotEmpty()) {
                val randomLayer = realLayers.random()
                navSelections[navIndex] = SelectionPart(nav = navIndex, color = actualColorIndex, layer = randomLayer.index)
                applyPreview(navIndex, actualColorIndex, randomLayer.index)
            }
        } else {
            val clampedLayerIndex = layerIndex.coerceIn(0, color.listPath.size - 1)
            navSelections[navIndex] = SelectionPart(nav = navIndex, color = actualColorIndex, layer = clampedLayerIndex)
            applyPreview(navIndex, actualColorIndex, clampedLayerIndex)
        }

        _triggerLayerUpdate.value += 1
    }

    private fun applyPreview(navIndex: Int, colorIndex: Int, layerIndex: Int) {
        val character = _currentCharacter.value ?: return
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val color = bodyPart.listPath.getOrNull(colorIndex) ?: return
        val imagePath = color.listPath.getOrNull(layerIndex) ?: return

        val previewBodyPart = bodyPart.copy(
            listPath = arrayListOf(color.copy(listPath = arrayListOf(imagePath)))
        )

        val newList = character.listPath.toMutableList()
        newList[navIndex] = previewBodyPart
        _currentCharacter.value = character.copy(listPath = ArrayList(newList))
    }

    private fun applyPreviewSingleImage(navIndex: Int, layerIndex: Int) {
        val character = _currentCharacter.value ?: return
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val position = bodyPart.position.toIntOrNull() ?: 0
        val layerImages = mutableListOf<String>()
        if (position != 1) layerImages.add("none")
        layerImages.add("dice")
        layerImages.addAll(
            bodyPart.listPath.mapNotNull { it.listPath.firstOrNull { p -> p.contains("/") } }
        )

        val selectedVariant = layerImages.getOrNull(layerIndex) ?: return
        val finalImagePath = if (selectedVariant == "none") "" else selectedVariant

        val previewBodyPart = bodyPart.copy(
            listPath = arrayListOf(ColorModel("", arrayListOf(finalImagePath)))
        )

        val newList = character.listPath.toMutableList()
        newList[navIndex] = previewBodyPart
        _currentCharacter.value = character.copy(listPath = ArrayList(newList))
    }

    fun resetCurrentVariant() {
        navSelections.clear()
        originalBodyParts.forEachIndexed { navIndex, _ ->
            navSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
        }

        autoSelectFirstVariantForNav(0)
        _currentNavIndex.value = 0
        _triggerLayerUpdate.value += 1
    }

    fun randomizeCharacter() {
        originalBodyParts.forEachIndexed { nav, bodyPart ->
            if (bodyPart.listPath.isEmpty()) return@forEachIndexed

            val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

            if (!hasColor) {
                val realImages = bodyPart.listPath.mapNotNull {
                    it.listPath.firstOrNull { p -> p.contains("/") }
                }
                if (realImages.isNotEmpty()) {
                    val position = bodyPart.position.toIntOrNull() ?: 0
                    val realStartIndex = if (position != 1) 2 else 1
                    val randomOffset = Random.nextInt(realImages.size)
                    val fixedLayerIndex = realStartIndex + randomOffset

                    navSelections[nav] = SelectionPart(nav = nav, color = -1, layer = fixedLayerIndex)
                    applyPreviewSingleImage(nav, fixedLayerIndex)
                }
            } else {
                val c = Random.nextInt(bodyPart.listPath.size)
                val l = Random.nextInt(bodyPart.listPath[c].listPath.size)
                navSelections[nav] = SelectionPart(nav = nav, color = c, layer = l)
                applyPreview(nav, c, l)
            }
        }
        _triggerColorUpdate.value += 1
        _triggerLayerUpdate.value += 1
    }

    fun getSelection(navIndex: Int): SelectionPart {
        return navSelections.getOrNull(navIndex)
            ?: SelectionPart(nav = navIndex, color = -1, layer = -1)
    }

    fun hasAnyRealImageSelected(): Boolean {
        return originalBodyParts.indices.any {
            !getImagePathForSelection(it).isNullOrBlank()
        }
    }

    fun getImagePathForSelection(navIndex: Int): String? {
        val selection = navSelections.getOrNull(navIndex) ?: return null
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return null

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            if (selection.layer == -1) return null

            val position = bodyPart.position.toIntOrNull() ?: 0
            val layerImages = mutableListOf<String>()
            if (position != 1) layerImages.add("none")
            layerImages.add("dice")
            layerImages.addAll(
                bodyPart.listPath.mapNotNull { it.listPath.firstOrNull { p -> p.contains("/") } }
            )

            val selectedVariant = layerImages.getOrNull(selection.layer) ?: return null
            return if (selectedVariant == "none") "" else selectedVariant
        } else {
            if (selection.color == -1 || selection.layer == -1) return null
            val color = bodyPart.listPath.getOrNull(selection.color) ?: return null
            return color.listPath.getOrNull(selection.layer)
        }
    }

    fun getLayerDisplayList(navIndex: Int): List<String> {
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return emptyList()

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (hasColor) {
            val colorIndex = navSelections.getOrNull(navIndex)?.color ?: 0
            val safeColorIndex = if (colorIndex == -1) 0 else colorIndex
            return bodyPart.listPath.getOrNull(safeColorIndex)?.listPath ?: emptyList()
        }

        val realImages = bodyPart.listPath.mapNotNull {
            it.listPath.firstOrNull { p -> p.contains("/") }
        }

        val list = mutableListOf<String>()
        val position = bodyPart.position.toIntOrNull() ?: 0
        if (position != 1) list.add("none")
        list.add("dice")
        list.addAll(realImages)
        return list
    }

    /**
     * ✅ FIXED: Save character với TOÀN BỘ dữ liệu gốc + selections riêng
     */
    fun saveCharacter(mainViewModel: ViewModelActivity, capturedImagePath: String = "") {
        val base = baseCharacter ?: return

        // ✅ Lưu TOÀN BỘ originalBodyParts (không thay đổi)
        val finalCharacter = base.copy(
            listPath = ArrayList(originalBodyParts),  // ✅ Giữ nguyên TOÀN BỘ dữ liệu
            selections = ArrayList(navSelections),     // ✅ Lưu selections riêng
            imageSave = capturedImagePath,
            updatedAt = System.currentTimeMillis()
        )

        mainViewModel.updateOrAddCharacter(finalCharacter, characterIndex)

        Log.d("CustomizeViewModel", "✅ Character saved:")
        Log.d("CustomizeViewModel", "   - ID: ${finalCharacter.id}")
        Log.d("CustomizeViewModel", "   - Body parts: ${finalCharacter.listPath.size}")
        Log.d("CustomizeViewModel", "   - Selections: ${finalCharacter.selections.size}")
        Log.d("CustomizeViewModel", "   - Image: $capturedImagePath")

        finalCharacter.selections.forEachIndexed { idx, selection ->
            Log.d("CustomizeViewModel", "   - Selection[$idx]: nav=${selection.nav}, color=${selection.color}, layer=${selection.layer}")
        }
    }
}