package com.example.basefragment.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt

class StrokeTextView  @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var strokeWidth = 4f
    private var strokeColor = "#FF7674".toColorInt()

    override fun onDraw(canvas: Canvas) {
        val textColor = currentTextColor

        // Vẽ stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        setTextColor(strokeColor)
        super.onDraw(canvas)

        // Vẽ chữ chính
        paint.style = Paint.Style.FILL
        setTextColor(textColor)
        super.onDraw(canvas)
    }
}