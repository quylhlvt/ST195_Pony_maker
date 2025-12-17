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

    private var characterIndex: Int = -1
    private var originalBodyParts: List<BodyPartModel> = emptyList()

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

        _currentCharacter.value = character

        // Deep copy originalBodyParts
        originalBodyParts = character.listPath.map { bp ->
            bp.copy(
                listPath = ArrayList(
                    bp.listPath.map { color ->
                        color.copy(listPath = ArrayList(color.listPath))
                    }
                )
            )
        }

        _selectedBodyParts.value = originalBodyParts

        navSelections.clear()
        originalBodyParts.forEachIndexed { index, _ ->
            navSelections.add(SelectionPart(nav = index, color = -1, layer = -1))
        }

        _currentNavIndex.value = 0

        // 🔥 CHỈ nav 0 mới auto-select layer 0
        val firstBodyPart = originalBodyParts.getOrNull(0)
        if (firstBodyPart != null && firstBodyPart.listPath.isNotEmpty()) {
            val hasColor = firstBodyPart.listPath.any { it.color.isNotEmpty() }
            val firstColorModel = firstBodyPart.listPath[0]

            if (firstColorModel.listPath.isNotEmpty()) {
                if (!hasColor) {
                    // Không có color → select layer 0
                    navSelections[0] = SelectionPart(nav = 0, color = -1, layer = 0)
                    applyPreviewSingleImage(0, 0)
                } else {
                    // Có color → select color 0, layer 0
                    navSelections[0] = SelectionPart(nav = 0, color = 0, layer = 0)
                    applyPreview(0, 0, 0)
                }
            }
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

        val safeLayerIndex = if (indexLayer >= 0) indexLayer else 0
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

        // 🔥 Kiểm tra xem có color hay không
        val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

        if (!hasColor) {
            // 🔥 Không có color → layerIndex là index trong listPath của ColorModel đầu tiên
            navSelections[navIndex] = SelectionPart(
                nav = navIndex,
                color = -1,
                layer = layerIndex
            )

            applyPreviewSingleImage(navIndex, layerIndex)
            return
        }

        // 🔥 Có color → logic bình thường
        val colorIndex = currentSelection.color
        val actualColorIndex = if (colorIndex == -1) 0 else colorIndex

        navSelections[navIndex] = SelectionPart(
            nav = navIndex,
            color = actualColorIndex,
            layer = layerIndex
        )

        applyPreview(navIndex, actualColorIndex, layerIndex)
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

    // 🔥 Apply preview cho trường hợp không có màu
    private fun applyPreviewSingleImage(navIndex: Int, layerIndex: Int) {
        val character = _currentCharacter.value ?: return
        val bodyPart = originalBodyParts.getOrNull(navIndex) ?: return

        // 🔥 Vì không có màu → lấy listPath từ ColorModel đầu tiên (tất cả giống nhau)
        val referenceColorModel = bodyPart.listPath.firstOrNull() ?: return
        val allVariants = referenceColorModel.listPath

        if (allVariants.isEmpty()) return

        // 🔥 Lấy variant mà người dùng đã chọn theo layerIndex
        val selectedVariant = allVariants.getOrNull(layerIndex) ?: return

        // 🔥 Xử lý 3 trường hợp: none, dice, ảnh thật
        val finalImagePath = when (selectedVariant) {
            "none" -> {
                "" // Không hiển thị layer này
            }
            "dice" -> {
                // Random một ảnh thật bất kỳ (lọc bỏ none và dice)
                val realImages = allVariants.filter {
                    it != "none" && it != "dice" && it.contains("/")
                }
                if (realImages.isEmpty()) "" else realImages.random()
            }
            else -> {
                // Là đường dẫn ảnh thật → dùng luôn
                selectedVariant
            }
        }

        // 🔥 Tạo preview: chỉ giữ lại 1 ColorModel (color rỗng) với 1 path duy nhất
        val previewBodyPart = bodyPart.copy(
            listPath = arrayListOf(
                ColorModel(
                    color = "",
                    listPath = arrayListOf(finalImagePath)
                )
            )
        )

        val newList = character.listPath.toMutableList()
        newList[navIndex] = previewBodyPart

        _currentCharacter.value = character.copy(listPath = ArrayList(newList))
    }
    fun resetCurrentVariant() {
        val nav = _currentNavIndex.value
        val bodyPart = originalBodyParts.getOrNull(nav) ?: return

        if (bodyPart.listPath.isNotEmpty()) {
            val firstColor = bodyPart.listPath[0]
            if (firstColor.listPath.isNotEmpty()) {
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

                if (!hasColor) {
                    // 🔥 Không có màu
                    if (nav == 0) {
                        // Nav 0 → reset về layer 0
                        navSelections[nav] = SelectionPart(nav = nav, color = -1, layer = 0)
                        applyPreviewSingleImage(nav, 0)
                    } else {
                        // Nav khác → reset về -1 (không select gì)
                        navSelections[nav] = SelectionPart(nav = nav, color = -1, layer = -1)
                    }
                } else {
                    // Có màu → reset về color 0, layer 0
                    navSelections[nav] = SelectionPart(nav = nav, color = 0, layer = 0)
                    applyPreview(nav, 0, 0)
                }
            }
        }
    }

    fun randomizeCharacter() {
        originalBodyParts.forEachIndexed { nav, bodyPart ->
            if (bodyPart.listPath.isNotEmpty()) {
                val hasColor = bodyPart.listPath.any { it.color.isNotEmpty() }

                if (!hasColor) {
                    // 🔥 Không có màu → random một layer
                    val firstColorModel = bodyPart.listPath.firstOrNull() ?: return@forEachIndexed
                    val randomLayerIndex = Random.nextInt(firstColorModel.listPath.size)

                    navSelections[nav] = SelectionPart(nav = nav, color = -1, layer = randomLayerIndex)
                    applyPreviewSingleImage(nav, randomLayerIndex)
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
        val character = _currentCharacter.value ?: return
        mainViewModel.updateOrAddCharacter(
            character = character,
            index = characterIndex
        )
    }

    fun getSelection(navIndex: Int): SelectionPart {
        return navSelections.getOrNull(navIndex)
            ?: SelectionPart(nav = navIndex, color = -1, layer = -1)
    }
}