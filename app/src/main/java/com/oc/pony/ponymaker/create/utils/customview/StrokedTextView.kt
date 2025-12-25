package com.oc.pony.ponymaker.create.utils.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt

class StrokedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidth: Float = 4f  // Độ dày của viền
    private var strokeColor: Int = "#FF7674".toColorInt()  // Màu viền

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = this@StrokedTextView.strokeWidth
        color = strokeColor
    }

    override fun onDraw(canvas: Canvas) {
        // Vẽ stroke trước
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        canvas?.drawText(text.toString(), paddingLeft.toFloat(), baseline.toFloat(), paint)

        // Vẽ text đè lên
        paint.style = Paint.Style.FILL
        paint.color = currentTextColor
        canvas?.drawText(text.toString(), paddingLeft.toFloat(), baseline.toFloat(), paint)
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        invalidate()
    }
}