package com.example.basefragment.data.model.custom

import com.google.gson.annotations.SerializedName

data class CustomModel(  @SerializedName("id")
                         val id: String = "",

                         @SerializedName("name")
                         val name: String = "",

                         @SerializedName("thumbnail")
                         val thumbnail: String = "", // URL hoặc path ảnh preview

                         @SerializedName("body_parts")
                         val bodyParts: List<BodyPartModel> = emptyList(), // Danh sách các bộ phận cơ thể

                         @SerializedName("is_premium")
                         val isPremium: Boolean = false,

                         @SerializedName("category")
                         val category: String = "" // Ví dụ: "animal", "human", "fantasy")
