package com.mikmy.emberdepth.render.effects

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.min

class FloatingText {

    class Entry(
        var x: Float, var y: Float,
        val text: String, val color: Int,
        var life: Float, val maxLife: Float,
        val size: Float
    )

    private val entries = ArrayList<Entry>(20)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun push(x: Float, y: Float, text: String, color: Int, life: Float = 0.8f, size: Float = 28f) {
        if (entries.size > 18) entries.removeAt(0)
        entries.add(Entry(x, y, text, color, life, life, size))
    }

    fun update(dt: Float, unit: Float) {
        var i = 0
        while (i < entries.size) {
            val e = entries[i]
            e.life -= dt
            if (e.life <= 0f) { entries.removeAt(i); continue }
            e.y -= dt * unit * 0.08f
            i++
        }
    }

    fun draw(canvas: Canvas, screenWidth: Float) {
        for (e in entries) {
            val t = (e.life / e.maxLife).coerceIn(0f, 1f)
            paint.textSize = e.size * (1f + (1f - t) * 0.25f)
            val alpha = (255 * min(1f, t * 2.5f)).toInt().coerceIn(0, 255)
            paint.color = (e.color and 0x00FFFFFF) or (alpha shl 24)
            canvas.drawText(e.text, e.x.coerceIn(screenWidth * 0.1f, screenWidth * 0.9f), e.y, paint)
        }
    }

    fun clear() = entries.clear()
}
