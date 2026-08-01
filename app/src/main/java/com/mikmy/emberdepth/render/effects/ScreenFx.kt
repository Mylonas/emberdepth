package com.mikmy.emberdepth.render.effects

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ScreenFx {

    var flash: Float = 0f
        private set
    var shake: Float = 0f
        private set

    private val paint = Paint()

    fun triggerFlash(intensity: Float = 0.5f) {
        flash = max(flash, intensity)
    }

    fun triggerShake(intensity: Float, unit: Float) {
        shake = max(shake, intensity * unit)
    }

    fun update(dt: Float) {
        flash = max(0f, flash - dt * 2.5f)
        shake *= (1f - min(1f, dt * 9f))
    }

    fun applyShake(canvas: Canvas) {
        if (shake > 0.4f) {
            canvas.translate(
                (Random.nextFloat() - 0.5f) * shake * 2f,
                (Random.nextFloat() - 0.5f) * shake * 2f
            )
        }
    }

    fun drawFlash(canvas: Canvas, w: Float, h: Float) {
        if (flash > 0.001f) {
            paint.style = Paint.Style.FILL
            paint.color = Color.argb((flash * 80).toInt().coerceIn(0, 255), 255, 255, 255)
            canvas.drawRect(0f, 0f, w, h, paint)
        }
    }
}
