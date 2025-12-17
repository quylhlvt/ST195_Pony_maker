package com.example.basefragment.data.model.custom

import android.os.Parcelable
import androidx.room.Index
import kotlinx.parcelize.Parcelize


@Parcelize
data class BodyPartModel(
    val nav: String,
    val listPath: ArrayList<ColorModel>
): Parcelable {

    // 🔥 Nav order (số trước "-"): Thứ tự hiển thị trong nav RecyclerView
    // "1-1" → position=1, "2-4" → position=2, "3-5" → position=3
    val position: String by lazy {
        nav.substringBeforeLast("/")
            .substringAfterLast("/")
            .substringBefore("-")
    }

    // 🔥 Render order (số sau "-"): Thứ tự z-index khi render lên màn hình
    // "1-1" → zIndex=1, "2-4" → zIndex=4, "3-5" → zIndex=5
    val zIndex: Int by lazy {
        nav.substringBeforeLast("/")
            .substringAfterLast("/")
            .substringAfter("-", "0")
            .toIntOrNull() ?: 0
    }

    // 🔥 Nav order as Int (để sort)
    val navOrder: Int by lazy {
        position.toIntOrNull() ?: 0
    }

    inline val colorCount: Int get() = listPath.size
    inline fun getColor(index: Int) = listPath.getOrNull(index)
}