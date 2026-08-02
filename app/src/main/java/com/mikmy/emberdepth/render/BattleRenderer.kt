package com.mikmy.emberdepth.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.render.effects.FloatingText
import com.mikmy.emberdepth.render.effects.Particles
import com.mikmy.emberdepth.render.effects.ScreenFx
import kotlin.math.min
import kotlin.math.sin

class BattleRenderer {

    private val colBg = 0xFF080A14.toInt()
    private val colSurface = 0xFF10131F.toInt()
    private val colEmber = 0xFFE8733E.toInt()
    private val colGold = 0xFFFFD84D.toInt()
    private val colHealth = 0xFF4AE06A.toInt()
    private val colDamage = 0xFFFF3B5C.toInt()
    private val colTextPrimary = 0xFFE8E2D8.toInt()
    private val colTextSecondary = 0xFF8A8278.toInt()

    val particles = Particles()
    val floatingText = FloatingText()
    val screenFx = ScreenFx()

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fontBold = Typeface.create("sans-serif-black", Typeface.BOLD)
    private val fontCond = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    private val fontMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    private var w = 1f
    private var h = 1f
    private var unit = 1f
    private var hudH = 0f
    private var battleY = 0f
    private var clock = 0f

    fun resize(width: Int, height: Int) {
        w = width.toFloat()
        h = height.toFloat()
        unit = min(w, h)
        hudH = h * 0.12f
        battleY = hudH
    }

    fun update(dt: Float) {
        clock += dt
        particles.update(dt, unit)
        floatingText.update(dt, unit)
        screenFx.update(dt)
    }

    fun draw(
        canvas: Canvas,
        heroes: List<BattleEngine.BattleHero>,
        enemies: List<com.mikmy.emberdepth.core.model.Enemy>,
        enemyIndex: Int,
        floor: Int,
        gold: BigNum,
        ember: BigNum
    ) {
        canvas.drawColor(colBg)
        val saved = canvas.save()
        screenFx.applyShake(canvas)

        drawBattleArea(canvas)
        drawHeroes(canvas, heroes)
        drawEnemies(canvas, enemies, enemyIndex)
        particles.draw(canvas)
        floatingText.draw(canvas, w)

        canvas.restoreToCount(saved)

        drawHud(canvas, floor, gold, ember)
        screenFx.drawFlash(canvas, w, h)

        spawnAmbientEmbers()
    }

    private fun drawBattleArea(canvas: Canvas) {
        p.style = Paint.Style.FILL
        p.color = colSurface
        canvas.drawRect(0f, battleY, w, h, p)

        p.style = Paint.Style.STROKE
        p.strokeWidth = unit * 0.004f
        p.color = withAlpha(colEmber, 40)
        canvas.drawLine(0f, battleY, w, battleY, p)
    }

    private fun drawHeroes(canvas: Canvas, heroes: List<BattleEngine.BattleHero>) {
        val baseX = w * 0.18f
        val baseY = battleY + (h - battleY) * 0.5f

        for (hero in heroes) {
            val slot = hero.slot
            val x = baseX + (slot % 2) * w * 0.12f
            val y = baseY + (slot / 2 - 0.5f) * (h - battleY) * 0.28f
            val r = unit * 0.045f
            val col = elementColor(hero.element)

            // glow
            p.style = Paint.Style.FILL
            p.color = withAlpha(col, if (hero.alive) 35 else 10)
            canvas.drawCircle(x, y, r * 1.4f, p)

            // body
            p.color = if (hero.alive) col else withAlpha(col, 80)
            canvas.drawCircle(x, y, r, p)

            // highlight
            p.color = withAlpha(Color.WHITE, if (hero.alive) 120 else 40)
            canvas.drawCircle(x - r * 0.25f, y - r * 0.28f, r * 0.22f, p)

            // health bar
            if (hero.alive) {
                val hpFrac = (hero.currentHp.toDouble() / hero.stats.hp.toDouble()).toFloat().coerceIn(0f, 1f)
                val barW = r * 2f
                val barH = unit * 0.008f
                val barY = y + r + unit * 0.015f
                val barX = x - r
                p.color = withAlpha(colHealth, 40)
                canvas.drawRect(barX, barY, barX + barW, barY + barH, p)
                p.color = if (hpFrac > 0.3f) colHealth else colDamage
                canvas.drawRect(barX, barY, barX + barW * hpFrac, barY + barH, p)
            }

            // role letter
            p.textSize = unit * 0.022f
            p.typeface = fontCond
            p.textAlign = Paint.Align.CENTER
            p.color = withAlpha(Color.WHITE, if (hero.alive) 200 else 60)
            canvas.drawText(hero.def.role.name[0].toString(), x, y + unit * 0.008f, p)
        }
    }

    private fun drawEnemies(canvas: Canvas, enemies: List<com.mikmy.emberdepth.core.model.Enemy>, activeIndex: Int) {
        val baseX = w * 0.75f
        val baseY = battleY + (h - battleY) * 0.45f

        for ((i, enemy) in enemies.withIndex()) {
            if (enemy.hp <= BigNum.ZERO) continue
            val x = baseX + (i % 2) * w * 0.10f
            val y = baseY + (i / 2 - 0.5f) * (h - battleY) * 0.22f
            val r = unit * (if (enemy.isBoss) 0.065f else 0.04f)
            val col = elementColor(enemy.element)
            val active = i == activeIndex

            // glow for active target
            if (active) {
                val pulse = 0.6f + 0.4f * sin(clock * 4f)
                p.style = Paint.Style.FILL
                p.color = withAlpha(col, (25 * pulse).toInt())
                canvas.drawCircle(x, y, r * 1.6f, p)
            }

            // body
            p.style = Paint.Style.FILL
            p.color = col
            canvas.drawCircle(x, y, r, p)

            // shadow
            p.color = withAlpha(0xFF000000.toInt(), 50)
            canvas.drawCircle(x, y + r * 0.15f, r * 0.85f, p)
            p.color = col
            canvas.drawCircle(x, y, r * 0.82f, p)

            // boss ring
            if (enemy.isBoss) {
                p.style = Paint.Style.STROKE
                p.strokeWidth = unit * 0.005f
                p.color = withAlpha(colGold, (160 + 80 * sin(clock * 3f)).toInt())
                canvas.drawCircle(x, y, r * 1.15f, p)
            }

            // health bar
            val hpFrac = (enemy.hp.toDouble() / enemy.maxHp.toDouble()).toFloat().coerceIn(0f, 1f)
            val barW = r * 2f
            val barH = unit * 0.007f
            val barY2 = y + r + unit * 0.012f
            val barX = x - r
            p.style = Paint.Style.FILL
            p.color = withAlpha(colDamage, 35)
            canvas.drawRect(barX, barY2, barX + barW, barY2 + barH, p)
            p.color = colDamage
            canvas.drawRect(barX, barY2, barX + barW * hpFrac, barY2 + barH, p)
        }
    }

    private fun drawHud(canvas: Canvas, floor: Int, gold: BigNum, ember: BigNum) {
        p.style = Paint.Style.FILL
        p.color = colBg
        canvas.drawRect(0f, 0f, w, hudH, p)

        // floor counter
        p.typeface = fontBold
        p.textAlign = Paint.Align.LEFT
        p.textSize = unit * 0.04f
        p.color = colTextPrimary
        canvas.drawText("FLOOR $floor", w * 0.05f, hudH * 0.55f, p)

        // gold
        p.typeface = fontCond
        p.textSize = unit * 0.028f
        p.color = colGold
        p.textAlign = Paint.Align.RIGHT
        canvas.drawText(gold.format(), w * 0.95f, hudH * 0.42f, p)

        // ember
        p.color = colEmber
        canvas.drawText(ember.format(), w * 0.95f, hudH * 0.75f, p)

        // labels
        p.textSize = unit * 0.018f
        p.color = colTextSecondary
        canvas.drawText("GOLD", w * 0.95f - goldLabelOffset(gold), hudH * 0.42f, p)
        canvas.drawText("EMBER", w * 0.95f - emberLabelOffset(ember), hudH * 0.75f, p)
    }

    private fun goldLabelOffset(gold: BigNum): Float {
        p.textSize = unit * 0.028f
        return p.measureText(gold.format()) + unit * 0.015f
    }

    private fun emberLabelOffset(ember: BigNum): Float {
        p.textSize = unit * 0.028f
        return p.measureText(ember.format()) + unit * 0.015f
    }

    private fun spawnAmbientEmbers() {
        if (clock % 0.15f < 0.02f) {
            particles.ember(w * (0.1f + kotlin.random.Random.nextFloat() * 0.8f), h * 0.95f, unit)
        }
    }

    fun heroScreenPos(slot: Int): Pair<Float, Float> {
        val baseX = w * 0.18f
        val baseY = battleY + (h - battleY) * 0.5f
        val x = baseX + (slot % 2) * w * 0.12f
        val y = baseY + (slot / 2 - 0.5f) * (h - battleY) * 0.28f
        return x to y
    }

    fun enemyScreenPos(index: Int): Pair<Float, Float> {
        val baseX = w * 0.75f
        val baseY = battleY + (h - battleY) * 0.45f
        val x = baseX + (index % 2) * w * 0.10f
        val y = baseY + (index / 2 - 0.5f) * (h - battleY) * 0.22f
        return x to y
    }

    fun onHeroAttack(slot: Int, damage: BigNum, targetX: Float, targetY: Float) {
        val col = colTextPrimary
        floatingText.push(targetX, targetY - unit * 0.03f, damage.format(), col, 0.6f, unit * 0.03f)
    }

    fun onEnemyKilled(x: Float, y: Float, color: Int, isBoss: Boolean) {
        val count = if (isBoss) 25 else 12
        particles.burst(x, y, color, count, unit * 0.3f)
        if (isBoss) {
            screenFx.triggerFlash(0.4f)
            screenFx.triggerShake(0.012f, unit)
        }
    }

    fun onFloorCleared(floor: Int) {
        floatingText.push(w * 0.5f, battleY + (h - battleY) * 0.3f,
            "FLOOR $floor", colEmber, 1.0f, unit * 0.05f)
    }

    fun onHeroHealed(slot: Int, amount: BigNum) {
        val (hx, hy) = heroScreenPos(slot)
        floatingText.push(hx, hy - unit * 0.04f, "+${amount.format()}", colHealth, 0.5f, unit * 0.025f)
    }

    fun onGoldEarned(amount: BigNum, x: Float, y: Float) {
        floatingText.push(x, y - unit * 0.05f, "+${amount.format()}", colGold, 0.5f, unit * 0.025f)
    }

    private fun elementColor(element: Element): Int = element.color.toInt()

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
}
