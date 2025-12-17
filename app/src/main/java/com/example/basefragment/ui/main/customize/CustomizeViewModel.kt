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

    // 🔥 Helper function: Lấy đường dẫn ảnh thật từ ColorModel
    private fun ColorModel.getImagePath(): String? {
        return listPath.firstOrNull { path ->
            path != "none" && path != "dice" && path.contains("/")
        }
    }

    // 🔥 Helper function: Lấy tất cả ảnh để hiển thị (bao gồm none, dice)
    private fun ColorModel.getAllDisplayPaths(): List<String> {
        return listPath.filter { path ->
            path == "none" || path == "dice" || path.contains("/")
        }
    }

// Trong CustomizeViewModel.kt

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

        // 🔥 Sort character.listPath theo navOrder để đảm bảo thứ tự nav đúng
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

        // 🔥 LOG cả nav order và z-index
        Log.d("CustomizeViewModel", "=== Init character ===")
        Log.d("CustomizeViewModel", "📋 Nav order (as stored in list):")
        originalBodyParts.forEachIndexed { idx, bodyPart ->
            Log.d("CustomizeViewModel",
                "   [$idx] Nav position=${bodyPart.position}, z-index=${bodyPart.zIndex}")
        }
        baseCharacter = character
        _selectedBodyParts.value = originalBodyParts

        // 🔥 Restore selection từ data đã lưu (cho edit) hoặc set -1 (cho tạo mới)
        navSelections.clear()
        originalBodyParts.forEachIndexed { navIndex, bodyPart ->
            val selection = restoreSelectionForNav(navIndex, bodyPart)
            navSelections.add(selection)
        }

        _currentNavIndex.value = 0

        // 🔥 Nếu là tạo mới (không có selection), auto-select nav 0
        val firstSelection = navSelections.getOrNull(0)
        if (firstSelection != null && firstSelection.layer == -1) {
            val firstBodyPart = originalBodyParts.getOrNull(0)
            if (firstBodyPart != null && firstBodyPart.listPath.isNotEmpty()) {
                val hasColor = firstBodyPart.listPath.any { it.color.isNotEmpty() }
                val firstColorModel = firstBodyPart.listPath[0]

                if (firstColorModel.listPath.isNotEmpty()) {
                    if (!hasColor && firstBodyPart.listPath.isNotEmpty()) {
                        // Không có color → select layer 0
                        navSelections[0] = SelectionPart(nav = 0, color = -1, layer = 1)
                        applyPreviewSingleImage(0, 1)
                    } else {
                        // Có color → select color 0, layer 0
                        navSelections[0] = SelectionPart(nav = 0, color = 0, layer = 0)
                        applyPreview(0, 0, 0)
                    }
                }
            }
        }
    }
    private fun restoreSelectionForNav(navIndex: Int, bodyPart: BodyPartModel): SelectionPart {
        if (bodyPart.listPath.isEmpty()) return SelectionPart(nav = navIndex, color = -1, layer = -1)

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            // 🔥 Không có color: Tìm layer đã chọn (dựa trên path không rỗng)
            val realImages = bodyPart.listPath.mapNotNull { colorModel ->
                colorModel.listPath.firstOrNull { path ->
                    path != "none" && path != "dice" && path.contains("/")
                }
            }

            val layerImages = mutableListOf<String>()
            val position = bodyPart.position.toIntOrNull() ?: 0

            if (position != 1) {
                layerImages.add("none")
                layerImages.add("dice")
            } else {
                layerImages.add("dice")
            }
            layerImages.addAll(realImages)

            // Tìm index của path đã lưu (nếu có)
            val savedPath = bodyPart.listPath.firstOrNull()?.listPath?.firstOrNull { it.isNotBlank() }
            val layerIndex = when (savedPath) {
                null, "" -> -1  // Không có
                in realImages -> layerImages.indexOf(savedPath)  // Ảnh thật
                else -> -1  // Không match
            }

            return SelectionPart(nav = navIndex, color = -1, layer = layerIndex)
        } else {
            // 🔥 Có color: Tìm color và layer đã chọn
            bodyPart.listPath.forEachIndexed { colorIndex, colorModel ->
                val layerIndex = colorModel.listPath.indexOfFirst { it.isNotBlank() }
                if (layerIndex != -1) {
                    return SelectionPart(nav = navIndex, color = colorIndex, layer = layerIndex)
                }
            }
            return SelectionPart(nav = navIndex, color = -1, layer = -1)  // Không có
        }
    }
    fun selectNav(navIndex: Int) {
        _currentNavIndex.value = navIndex

        // 🔥 Chỉ auto-select nếu là nav 0 VÀ chưa có selection
        if (navIndex == 0) {
            val currentSelection = navSelections.getOrNull(navIndex)
            if (currentSelection != null && currentSelection.layer == -1) {
                val bodyPart = originalBodyParts.getOrNull(navIndex)
                if (bodyPart != null && bodyPart.listPath.isNotEmpty()) {
                    val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }
                    val firstColorModel = bodyPart.listPath[0]

                    if (firstColorModel.listPath.isNotEmpty()) {
                        if (!hasColor) {
                            // Không có color → select layer 0
                            navSelections[navIndex] = SelectionPart(nav = navIndex, color = -1, layer = 0)
                            applyPreviewSingleImage(navIndex, 0)
                        } else {
                            // Có color → select color 0, layer 0
                            navSelections[navIndex] = SelectionPart(nav = navIndex, color = 0, layer = 0)
                            applyPreview(navIndex, 0, 0)
                        }
                    }
                }
            }
        }
        // 🔥 Nav khác KHÔNG auto-select, để user tự chọn
    }

    fun selectColor(colorIndex: Int, indexLayer: Int) {
        val navIndex = _currentNavIndex.value
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val newColor = bodyPart.listPath.getOrNull(colorIndex)
        val maxLayerIndex = (newColor?.listPath?.size ?: 1) - 1

        // Clamp layerIndex cho color mới (nếu không hợp lệ, reset về 0)
        val safeLayerIndex = (if (indexLayer >= 0) indexLayer else 0).coerceIn(0, maxLayerIndex)

        navSelections[navIndex] = SelectionPart(
            nav = navIndex,
            color = colorIndex,
            layer = safeLayerIndex
        )

        applyPreview(navIndex, colorIndex, safeLayerIndex)
        _triggerLayerUpdate.value = _triggerLayerUpdate.value + 1
    }

    fun selectLayer(layerIndex: Int) {
        val navIndex = _currentNavIndex.value
        val currentSelection = navSelections[navIndex]
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            // Logic không đổi cho trường hợp không có color
            navSelections[navIndex] = SelectionPart(
                nav = navIndex,
                color = -1,
                layer = layerIndex
            )
            applyPreviewSingleImage(navIndex, layerIndex)
            return
        }

        // Có color: clamp layerIndex
        val colorIndex = currentSelection.color
        val actualColorIndex = if (colorIndex == -1) 0 else colorIndex
        val color = bodyPart.listPath.getOrNull(actualColorIndex) ?: return
        val clampedLayerIndex = layerIndex.coerceIn(0, color.listPath.size - 1)

        navSelections[navIndex] = SelectionPart(
            nav = navIndex,
            color = actualColorIndex,
            layer = clampedLayerIndex
        )

        applyPreview(navIndex, actualColorIndex, clampedLayerIndex)
    }

    private fun applyPreview(
        navIndex: Int,
        colorIndex: Int,
        layerIndex: Int
    ) {
        val character = _currentCharacter.value ?: return
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        if (bodyPart.listPath.isEmpty()) return

        val color = bodyPart.listPath.getOrNull(colorIndex) ?: return
        val imagePath = color.listPath.getOrNull(layerIndex) ?: return

        val previewBodyPart = bodyPart.copy(
            listPath = arrayListOf(
                color.copy(listPath = arrayListOf(imagePath))
            )
        )

        val newList = character.listPath.toMutableList()
        newList[navIndex] = previewBodyPart

        _currentCharacter.value = character.copy(listPath = ArrayList(newList))
    }

    private fun applyPreviewSingleImage(navIndex: Int, layerIndex: Int) {
        val character = _currentCharacter.value ?: return
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        // 🔥 Build lại layerImages để lấy đúng path
        val realImages = bodyPart.listPath.mapNotNull { colorModel ->
            colorModel.listPath.firstOrNull { path ->
                path != "none" && path != "dice" && path.contains("/")
            }
        }

        val layerImages = mutableListOf<String>()
        val position = bodyPart.position.toIntOrNull() ?: 0

        if (position != 1) {
            layerImages.add("none")
            layerImages.add("dice")
        } else {
            layerImages.add("dice")
        }
        layerImages.addAll(realImages)

        // 🔥 Lấy variant được chọn
        val selectedVariant = layerImages.getOrNull(layerIndex) ?: return

        // 🔥 Xử lý 3 trường hợp
        val finalImagePath = when (selectedVariant) {
            "none" -> ""  // Không hiển thị
            "dice" -> realImages.randomOrNull() ?: ""
            else -> selectedVariant  // Ảnh thật
        }

        // 🔥 Update preview
        val previewBodyPart = bodyPart.copy(
            listPath = arrayListOf(
                ColorModel("", arrayListOf(finalImagePath))
            )
        )

        val newList = character.listPath.toMutableList()
        newList[navIndex] = previewBodyPart
        _currentCharacter.value = character.copy(listPath = ArrayList(newList))
    }


    fun resetCurrentVariant() {
        // Reset tất cả navSelections về -1 (không chọn gì)
        navSelections.clear()
        originalBodyParts.forEachIndexed { navIndex, _ ->
            navSelections.add(SelectionPart(nav = navIndex, color = -1, layer = -1))
        }

        // Chỉ auto-select nav0 như lần đầu vào
        val firstBodyPart = originalBodyParts.getOrNull(0)
        if (firstBodyPart != null && firstBodyPart.listPath.isNotEmpty()) {
            val hasColor = firstBodyPart.listPath.any { it.color.isNotEmpty() }
            val firstColorModel = firstBodyPart.listPath[0]

            if (firstColorModel.listPath.isNotEmpty()) {
                if (!hasColor) {
                    // Không có color → select layer 0
                    navSelections[0] = SelectionPart(nav = 0, color = -1, layer = 1)
                    applyPreviewSingleImage(0, 1)
                } else {
                    // Có color → select color 0, layer 0
                    navSelections[0] = SelectionPart(nav = 0, color = 0, layer = 0)
                    applyPreview(0, 0, 0)
                }
            }
        }

        // Set nav hiện tại về 0 và trigger update
        _currentNavIndex.value = 0
        _triggerLayerUpdate.value = _triggerLayerUpdate.value + 1
    }

    fun randomizeCharacter() {
        originalBodyParts.forEachIndexed { nav, bodyPart ->
            if (bodyPart.listPath.isNotEmpty()) {
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

                if (!hasColor) {
                    val layers = getLayerDisplayList(nav)
                    if (layers.isNotEmpty()) {
                        val randomLayerIndex = Random.nextInt(layers.size)
                        navSelections[nav] = SelectionPart(nav, -1, randomLayerIndex)
                        applyPreviewSingleImage(nav, randomLayerIndex)
                    }

                } else {
                    // Có màu → logic cũ
                    val c = Random.nextInt(bodyPart.listPath.size)
                    val l = Random.nextInt(bodyPart.listPath[c].listPath.size)
                    navSelections[nav] = SelectionPart(nav = nav, color = c, layer = l)
                    applyPreview(nav, c, l)
                }
            }
        }
    }

    fun saveCharacter(mainViewModel: ViewModelActivity) {
        val base = baseCharacter ?: return

        val updatedParts = originalBodyParts.mapIndexed { navIndex, originalPart ->
            val selectedPath = getImagePathForSelection(navIndex)
            if (selectedPath.isNullOrBlank()) {
                // Không chọn → để trống hoặc giữ cấu trúc none
                originalPart.copy(
                    listPath = arrayListOf(ColorModel("", arrayListOf("")))
                )
            } else {
                originalPart.copy(
                    listPath = arrayListOf(ColorModel("", arrayListOf(selectedPath)))
                )
            }
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
            // 🔥 Không có color

            // 🔥 Nếu chưa chọn layer → return null
            if (selection.layer == -1) return null

            // 🔥 Build lại layerImages để map đúng index
            val realImages = bodyPart.listPath.mapNotNull { colorModel ->
                colorModel.listPath.firstOrNull { path ->
                    path != "none" && path != "dice" && path.contains("/")
                }
            }

            val layerImages = mutableListOf<String>()
            val position = bodyPart.position.toIntOrNull() ?: 0

            if (position != 1) {
                layerImages.add("none")
                layerImages.add("dice")
            } else {
                layerImages.add("dice")
            }
            layerImages.addAll(realImages)

            // 🔥 Lấy variant theo selection.layer
            val selectedVariant = layerImages.getOrNull(selection.layer) ?: return null

            return when (selectedVariant) {
                "none" -> null  // Không hiển thị
                "dice" -> realImages.randomOrNull()
                else -> selectedVariant  // Ảnh thật
            }
        } else {
            // 🔥 Có màu

            // 🔥 Nếu chưa chọn → return null
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
            return bodyPart.listPath
                .getOrNull(if (colorIndex == -1) 0 else colorIndex)
                ?.listPath ?: emptyList()
        }

        val realImages = bodyPart.listPath.mapNotNull {
            it.listPath.firstOrNull { p -> p.contains("/") }
        }

        val list = mutableListOf<String>()
        val position = bodyPart.position.toIntOrNull() ?: 0

        if (position != 1) {
            list.add("none")
            list.add("dice")
        } else {
            list.add("dice")
        }

        list.addAll(realImages)
        return list
    }

}