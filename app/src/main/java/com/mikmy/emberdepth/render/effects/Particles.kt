package com.mikmy.emberdepth.render.effects

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class Particles {

    class Particle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Float, var maxLife: Float,
        var size: Float, var color: Int
    )

    private val pool = ArrayList<Particle>(300)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun burst(x: Float, y: Float, color: Int, count: Int, power: Float) {
        if (pool.size > 350) return
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.2832f
            val speed = power * (0.3f + Random.nextFloat() * 0.8f)
            val life = 0.3f + Random.nextFloat() * 0.5f
            pool.add(Particle(
                x, y,
                cos(angle) * speed, sin(angle) * speed,
                life, life,
                2f + Random.nextFloat() * 4f, color
            ))
        }
    }

    fun ember(x: Float, y: Float, unit: Float) {
        if (pool.size > 350) return
        val life = 1.0f + Random.nextFloat() * 1.5f
        pool.add(Particle(
            x + (Random.nextFloat() - 0.5f) * unit * 0.1f,
            y,
            (Random.nextFloat() - 0.5f) * unit * 0.02f,
            -unit * (0.02f + Random.nextFloat() * 0.04f),
            life, life,
            1.5f + Random.nextFloat() * 2f,
            0xFFE8733E.toInt()
        ))
    }

    fun update(dt: Float, unit: Float) {
        var i = 0
        while (i < pool.size) {
            val p = pool[i]
            p.life -= dt
            if (p.life <= 0f) { pool.removeAt(i); continue }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += unit * 0.8f * dt
            val drag = 1f - min(1f, dt * 1.8f)
            p.vx *= drag
            i++
        }
    }

    fun draw(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        for (p in pool) {
            val t = (p.life / p.maxLife).coerceIn(0f, 1f)
            val alpha = (255 * t * t).toInt().coerceIn(0, 255)
            paint.color = (p.color and 0x00FFFFFF) or (alpha shl 24)
            canvas.drawCircle(p.x, p.y, p.size * (0.4f + t * 0.6f), paint)
        }
    }

    fun clear() = pool.clear()
}
