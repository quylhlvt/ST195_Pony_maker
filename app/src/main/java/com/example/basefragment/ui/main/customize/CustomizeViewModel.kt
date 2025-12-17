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

        val sortedListPath = character.listPath.sortedBy { it.zIndex }
        _currentCharacter.value = character.copy(listPath = ArrayList(sortedListPath))

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
        Log.d("CustomizeViewModel", "📋 Nav order (as stored in list):")
        originalBodyParts.forEachIndexed { idx, bodyPart ->
            Log.d(
                "CustomizeViewModel",
                "   [$idx] Nav position=${bodyPart.position}, z-index=${bodyPart.zIndex}"
            )
        }

        baseCharacter = character
        _selectedBodyParts.value = originalBodyParts

        // Restore selection hoặc tạo mới
        navSelections.clear()
        originalBodyParts.forEachIndexed { navIndex, bodyPart ->
            val selection = restoreSelectionForNav(navIndex, bodyPart)
            navSelections.add(selection)
        }

        _currentNavIndex.value = 0

        // Auto-select cho nav 0 nếu chưa có selection (tạo mới)
        val firstSelection = navSelections.getOrNull(0)
        if (firstSelection != null && firstSelection.layer == -1) {
            autoSelectFirstVariantForNav(0)
        }
    }

    private fun restoreSelectionForNav(navIndex: Int, bodyPart: BodyPartModel): SelectionPart {
        if (bodyPart.listPath.isEmpty()) return SelectionPart(nav = navIndex, color = -1, layer = -1)

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            val realImages = bodyPart.listPath.mapNotNull { cm ->
                cm.listPath.firstOrNull { it.contains("/") && it != "none" && it != "dice" }
            }

            val position = bodyPart.position.toIntOrNull() ?: 0
            val layerImages = mutableListOf<String>()
            if (position != 1) layerImages.add("none")
            layerImages.add("dice")
            layerImages.addAll(realImages)

            val savedPath = bodyPart.listPath.firstOrNull()?.listPath?.firstOrNull { it.isNotBlank() }
            val layerIndex = when (savedPath) {
                null, "" -> -1
                in realImages -> layerImages.indexOf(savedPath)
                else -> -1
            }
            return SelectionPart(nav = navIndex, color = -1, layer = layerIndex)
        } else {
            bodyPart.listPath.forEachIndexed { colorIndex, colorModel ->
                val layerIndex = colorModel.listPath.indexOfFirst { it.isNotBlank() }
                if (layerIndex != -1) {
                    return SelectionPart(nav = navIndex, color = colorIndex, layer = layerIndex)
                }
            }
            return SelectionPart(nav = navIndex, color = -1, layer = -1)
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
            val realImagesCount = bodyPart.listPath.sumOf { cm -> cm.listPath.count { it.contains("/") } }

            if (realImagesCount > 0) {
                // Chọn variant đầu tiên trong các ảnh thật
                val firstRealIndex = if (position != 1) 2 else 1
                navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = firstRealIndex)
                applyPreviewSingleImage(navIndex, firstRealIndex)
            }
        }
    }

    fun selectNav(navIndex: Int) {
        _currentNavIndex.value = navIndex

        // Chỉ auto-select khi chuyển sang nav chưa có selection (trừ nav 0 đã xử lý ở init)
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
            // Build danh sách layer để kiểm tra giá trị
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
                    navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = 0) // index 0 = none
                    applyPreviewSingleImage(navIndex, 0)
                    _triggerLayerUpdate.value += 1
                    return
                }
                else -> {
                    // Ảnh thật bình thường
                    navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = layerIndex)
                    applyPreviewSingleImage(navIndex, layerIndex)
                    _triggerLayerUpdate.value += 1
                    return
                }
            }
        }

        // Có color
        val currentSelection = navSelections.getOrNull(navIndex) ?: SelectionPart(navIndex, 0, 0)
        val actualColorIndex = if (currentSelection.color == -1) 0 else currentSelection.color
        val color = bodyPart.listPath.getOrNull(actualColorIndex) ?: return
        val clampedLayerIndex = layerIndex.coerceIn(0, color.listPath.size - 1)

        navSelections[navIndex] = SelectionPart(nav = navIndex, color = actualColorIndex, layer = clampedLayerIndex)
        applyPreview(navIndex, actualColorIndex, clampedLayerIndex)
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

        // Auto-select lại nav 0 với variant đầu tiên cố định
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
        _triggerLayerUpdate.value += 1
    }

    fun saveCharacter(mainViewModel: ViewModelActivity) {
        val base = baseCharacter ?: return

        val updatedParts = originalBodyParts.mapIndexed { navIndex, originalPart ->
            val selectedPath = getImagePathForSelection(navIndex)
            originalPart.copy(
                listPath = arrayListOf(ColorModel("", arrayListOf(selectedPath.orEmpty())))
            )
        }

        val finalCharacter = base.copy(
            listPath = ArrayList(updatedParts),
            updatedAt = System.currentTimeMillis()
        )

        mainViewModel.updateOrAddCharacter(finalCharacter, characterIndex)
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
}