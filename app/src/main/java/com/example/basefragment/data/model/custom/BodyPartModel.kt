package com.example.basefragment.data.model.custom

import com.google.gson.annotations.SerializedName


data class BodyPartModel(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("type")
    val type: BodyPartType = BodyPartType.HEAD, // Loại bộ phận

    @SerializedName("name")
    val name: String = "",

    @SerializedName("colors")
    val colors: List<ColorModel> = emptyList(), // Các màu có thể chọn

    @SerializedName("default_color_index")
    val defaultColorIndex: Int = 0,

    @SerializedName("layer_order")
    val layerOrder: Int = 0 // Thứ tự hiển thị (số càng nhỏ càng ở dưới)
)

/**
 * Enum định nghĩa các loại bộ phận
 */
enum class BodyPartType {
    HEAD,
    EYES,
    MOUTH,
    BODY,
    ARMS,
    LEGS,
    HAIR,
    ACCESSORIES,
    BACKGROUND
}
