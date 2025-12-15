package com.example.basefragment.data.model.custom

import android.os.Parcelable
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Parcelize
data class BodyPartModel(
val nav: String,
    val listPath: ArrayList<ColorModel>
): Parcelable{
    val position : String by lazy {
        nav.substringBeforeLast("/").substringAfterLast("/").substringAfter("-")
    }
    inline  val colorCount:Int get() = listPath.size
    inline  fun getColor(index: Int)= listPath.getOrNull(index)
}

