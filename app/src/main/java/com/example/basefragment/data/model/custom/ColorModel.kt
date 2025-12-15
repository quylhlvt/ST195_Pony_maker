package com.example.basefragment.data.model.custom

import com.google.gson.annotations.SerializedName

data class ColorModel(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("color_code")
    val colorCode: String = "", // Hex color code, ví dụ: "#FF5733"

    @SerializedName("image_url")
    val imageUrl: String = "", // URL hoặc path của layer với màu này

    @SerializedName("name")
    val name: String = "" // Tên màu (optional)
)