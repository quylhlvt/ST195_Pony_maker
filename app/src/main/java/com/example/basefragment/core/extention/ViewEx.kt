package com.example.basefragment.core.extention

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import com.example.basefragment.utils.DataLocal.KEY_LAST_CLICK_TIME


fun TextView.setFont(@FontRes resId: Int) {
    typeface = ResourcesCompat.getFont(context, resId)
}
fun Context.strings(resId: Int) : String {
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
fun View.visible() { visibility = View.VISIBLE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.select() { isSelected = true }
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