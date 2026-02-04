package com.example.basefragment.core.helper

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

object StripeProgressHelper {

    fun applyStripe(progressBar: View) {
        val stripeSize = 40f
        val tileSize = (stripeSize * 4).toInt()

        val bitmap = createBitmap(tileSize, tileSize)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#F4AE23".toColorInt()
            style = Paint.Style.FILL
        }

        val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#8B015A".toColorInt()
            style = Paint.Style.FILL
        }

        // nền vàng
        canvas.drawRect(0f, 0f, tileSize.toFloat(), tileSize.toFloat(), bgPaint)

        var x: Int = -tileSize
        while (x < tileSize * 2) {
            val path = android.graphics.Path().apply {
                moveTo(x.toFloat(), tileSize.toFloat())
                lineTo(x + stripeSize, tileSize.toFloat())
                lineTo(x + stripeSize + tileSize, 0f)
                lineTo((x + tileSize).toFloat(), 0f)

                close()
            }
            canvas.drawPath(path, stripePaint)
            x += (stripeSize * 2).toInt()
        }

        val drawable = BitmapDrawable(progressBar.resources, bitmap)
        drawable.tileModeX = Shader.TileMode.REPEAT
        drawable.tileModeY = Shader.TileMode.REPEAT

        progressBar.background = drawable
    }


    fun animateStripe(progressBar: View) {
        val drawable = progressBar.background as? BitmapDrawable ?: return
        val shader = drawable.paint.shader ?: return
        val matrix = Matrix()

        ValueAnimator.ofFloat(0f, 80f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val offset = it.animatedValue as Float
                matrix.setTranslate(-offset, 0f) // ⬅️ chỉ trượt X
                shader.setLocalMatrix(matrix)
                progressBar.invalidate()
            }
            start()
        }
    }

    fun animateCharacter(
        bgBar: View,
        progressBar: View,
        character: ImageView
    ) {
        bgBar.post {
            val maxWidth = bgBar.width
            val charHalf = character.width / 2f

            ValueAnimator.ofInt(0, maxWidth).apply {
                duration = 2500
                interpolator = LinearInterpolator()

                addUpdateListener { anim ->
                    val value = anim.animatedValue as Int

                    // === DI CHUYỂN THEO PROGRESS ===
                    val x = value - charHalf
                    character.translationX = x.coerceAtLeast(0f)

                    // === NHÚN NHẢY ===
                    val phase = (value / 20) % 2
                    character.translationY = if (phase == 0) -4f else 0f

                    // === LẮC NHẸ ===
                    character.rotation = if (phase == 0) -6f else 6f
                }

                start()
            }
        }
    }

    fun animateProgress(bgBar: View, progressBar: View) {
        bgBar.post {
            val max = bgBar.width
            ValueAnimator.ofInt(0, max).apply {
                duration = 2500
                interpolator = LinearInterpolator()
                addUpdateListener {
                    progressBar.layoutParams.width = it.animatedValue as Int
                    progressBar.requestLayout()
                }
                start()
            }
        }
    }
}