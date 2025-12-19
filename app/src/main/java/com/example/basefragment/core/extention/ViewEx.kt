package com.example.basefragment.core.extention

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import com.example.basefragment.utils.DataLocal.KEY_LAST_CLICK_TIME
import kotlin.math.roundToInt

fun Int.dp(context: Context): Int =
    (this * context.resources.displayMetrics.density).roundToInt()

fun Float.dp(context: Context): Int =
    (this * context.resources.displayMetrics.density).roundToInt()

fun TextView.setFont(@FontRes resId: Int) {
    typeface = ResourcesCompat.getFont(context, resId)
}

fun Context.strings(resId: Int): String {
    return getString(resId)
}

fun setImageActionBar(imageView: ImageView, res: Int) {
    imageView.setImageResource(res)
    imageView.visible()
}

fun setTextActionBar(textView: TextView, text: String) {
    textView.text = text
    textView.visible()
    textView.visible()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.toggetShow() {
    visibility = if (visibility == View.VISIBLE) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}

fun View.select() {
    isSelected = true
}

fun View.onClick(interval: Long = 500, action: (View) -> Unit) {
    setOnClickListener {
        val lastClickTime = (this.getTag(KEY_LAST_CLICK_TIME) as? Long) ?: 0L
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastClickTime >= interval) {
            action(it)

            this.setTag(KEY_LAST_CLICK_TIME, currentTime)
        }
    }
}
fun View.drawToBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(
        width.coerceAtLeast(1),
        height.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)

    // Draw background nếu có
    background?.draw(canvas)

    // Draw view
    draw(canvas)

    return bitmap
}

/**
 * Check xem view đã được layout chưa
 */
fun View.isLaidOut(): Boolean {
    return width > 0 && height > 0
}
