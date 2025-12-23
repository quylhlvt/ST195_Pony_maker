package com.example.basefragment.data.model.custom

import android.os.Parcelable
import androidx.room.Index
import kotlinx.parcelize.Parcelize
import kotlin.text.toIntOrNull


@Parcelize
data class BodyPartModel(
    val nav: String,
    val listPath: ArrayList<ColorModel>
): Parcelable {


    inline fun getColor(index: Int) = listPath.getOrNull(index)
}