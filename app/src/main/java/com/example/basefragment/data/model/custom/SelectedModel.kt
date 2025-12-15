package com.example.basefragment.data.model.custom


/**
 * Model lưu trạng thái đang chọn trong UI
 */
data class SelectedModel(
    val bodyPartId: String = "",
    val colorIndex: Int = 0,
    val isSelected: Boolean = false
)

/**
 * Model lưu toàn bộ cấu hình nhân vật đã customize
 */
data class CharacterConfiguration(
    val characterId: String = "",
    val selectedParts: Map<BodyPartType, SelectedModel> = emptyMap(), // Key: BodyPartType, Value: SelectedModel
    val backgroundImageUrl: String = "",
    val stickers: List<StickerItem> = emptyList()
)

/**
 * Model cho sticker/item được thêm vào
 */
data class StickerItem(
    val id: String = "",
    val imageUrl: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)
