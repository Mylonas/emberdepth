package com.mikmy.emberdepth.render.effects

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.sin

class AttackFx {

    enum class Style { SLASH, BOLT, HEAL_PULSE }

    private class Anim(
        val startX: Float, val startY: Float,
        val endX: Float, val endY: Float,
        var life: Float, val maxLife: Float,
        val color: Int, val style: Style
    )

    private val anims = ArrayList<Anim>(16)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    fun trigger(sx: Float, sy: Float, ex: Float, ey: Float, color: Int, style: Style) {
        if (anims.size > 16) anims.removeAt(0)
        anims.add(Anim(sx, sy, ex, ey, 0f, 0.15f, color, style))
    }

    fun update(dt: Float) {
        var i = 0
        while (i < anims.size) {
            anims[i].life += dt
            if (anims[i].life >= anims[i].maxLife) anims.removeAt(i) else i++
        }
    }

    fun draw(canvas: Canvas) {
        for (a in anims) {
            val t = (a.life / a.maxLife).coerceIn(0f, 1f)
            val alpha = ((1f - t) * 200).toInt().coerceIn(0, 255)
            p.color = (a.color and 0x00FFFFFF) or (alpha shl 24)

            when (a.style) {
                Style.SLASH -> drawSlash(canvas, a, t)
                Style.BOLT -> drawBolt(canvas, a, t)
                Style.HEAL_PULSE -> drawHealPulse(canvas, a, t)
            }
        }
    }

    private fun drawSlash(canvas: Canvas, a: Anim, t: Float) {
        val progress = t.coerceAtMost(0.8f) / 0.8f
        val cx = a.startX + (a.endX - a.startX) * progress
        val cy = a.startY + (a.endY - a.startY) * progress
        val tailT = (progress - 0.3f).coerceAtLeast(0f) / 0.7f
        val tx = a.startX + (a.endX - a.startX) * tailT
        val ty = a.startY + (a.endY - a.startY) * tailT
        p.strokeWidth = 4f * (1f - t)
        p.style = Paint.Style.STROKE
        canvas.drawLine(tx, ty, cx, cy, p)
    }

    private fun drawBolt(canvas: Canvas, a: Anim, t: Float) {
        p.style = Paint.Style.FILL
        val progress = t.coerceAtMost(0.8f) / 0.8f
        for (i in 0..2) {
            val bt = (progress - i * 0.1f).coerceIn(0f, 1f)
            val bx = a.startX + (a.endX - a.startX) * bt
            val by = a.startY + (a.endY - a.startY) * bt
            val r = 3f * (1f - t)
            canvas.drawCircle(bx, by, r, p)
        }
    }

    private fun drawHealPulse(canvas: Canvas, a: Anim, t: Float) {
        p.style = Paint.Style.FILL
        val progress = t.coerceAtMost(0.9f) / 0.9f
        val cx = a.startX + (a.endX - a.startX) * progress
        val arcY = a.startY + (a.endY - a.startY) * progress - sin(progress * Math.PI.toFloat()) * 40f
        val r = 5f * (1f - t * 0.5f)
        canvas.drawCircle(cx, arcY, r, p)
    }
}
