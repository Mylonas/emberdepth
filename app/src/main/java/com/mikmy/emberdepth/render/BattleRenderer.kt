package com.mikmy.emberdepth.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.EnemyType
import com.mikmy.emberdepth.core.model.Role
import com.mikmy.emberdepth.render.effects.AttackFx
import com.mikmy.emberdepth.render.effects.FloatingText
import com.mikmy.emberdepth.render.effects.Particles
import com.mikmy.emberdepth.render.effects.ScreenFx
import kotlin.math.cos
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
    val attackFx = AttackFx()

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val fontBold = Typeface.create("sans-serif-black", Typeface.BOLD)
    private val fontCond = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    private val fontMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    private var w = 1f
    private var h = 1f
    private var unit = 1f
    private var hudH = 0f
    private var battleY = 0f
    private var clock = 0f
    private var currentFloor = 1
    private var bossEntrance = 0f

    private val bgTiers = arrayOf(
        intArrayOf(0xFF10131F.toInt(), 0xFF080A14.toInt()),
        intArrayOf(0xFF141822.toInt(), 0xFF0A0E18.toInt()),
        intArrayOf(0xFF1A1510.toInt(), 0xFF120E08.toInt()),
        intArrayOf(0xFF1E100C.toInt(), 0xFF160A06.toInt()),
        intArrayOf(0xFF180A18.toInt(), 0xFF100610.toInt())
    )

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
        attackFx.update(dt)
        if (bossEntrance > 0f) bossEntrance -= dt
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
        currentFloor = floor
        canvas.drawColor(colBg)
        val saved = canvas.save()
        screenFx.applyShake(canvas)

        drawBattleArea(canvas, floor)
        drawHeroes(canvas, heroes)
        drawEnemies(canvas, enemies, enemyIndex)
        attackFx.draw(canvas)
        particles.draw(canvas)
        floatingText.draw(canvas, w)

        if (bossEntrance > 0f) drawBossEntrance(canvas)

        canvas.restoreToCount(saved)

        drawHud(canvas, floor, gold, ember)
        screenFx.drawFlash(canvas, w, h)

        spawnAmbientEmbers()
    }

    private fun drawBattleArea(canvas: Canvas, floor: Int) {
        val tier = (floor / 25).coerceAtMost(bgTiers.size - 1)
        val nextTier = (tier + 1).coerceAtMost(bgTiers.size - 1)
        val t = (floor % 25) / 25f

        val surfaceCol = lerpColor(bgTiers[tier][0], bgTiers[nextTier][0], t)
        val bgCol = lerpColor(bgTiers[tier][1], bgTiers[nextTier][1], t)

        p.style = Paint.Style.FILL
        p.color = surfaceCol
        canvas.drawRect(0f, battleY, w, h, p)

        p.style = Paint.Style.STROKE
        p.strokeWidth = unit * 0.004f
        p.color = withAlpha(colEmber, 40)
        canvas.drawLine(0f, battleY, w, battleY, p)
    }

    private fun drawHeroes(canvas: Canvas, heroes: List<BattleEngine.BattleHero>) {
        for (hero in heroes) {
            val (x, y) = heroScreenPos(hero.slot)
            val r = unit * 0.045f
            val col = elementColor(hero.element)

            p.style = Paint.Style.FILL
            p.color = withAlpha(col, if (hero.alive) 35 else 10)
            canvas.drawCircle(x, y, r * 1.4f, p)

            p.color = if (hero.alive) col else withAlpha(col, 80)
            drawHeroShape(canvas, hero.def.role, x, y, r)

            p.color = withAlpha(Color.WHITE, if (hero.alive) 100 else 30)
            canvas.drawCircle(x - r * 0.2f, y - r * 0.25f, r * 0.18f, p)

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

            p.textSize = unit * 0.022f
            p.typeface = fontCond
            p.textAlign = Paint.Align.CENTER
            p.color = withAlpha(Color.WHITE, if (hero.alive) 200 else 60)
            canvas.drawText(hero.def.role.name[0].toString(), x, y + unit * 0.008f, p)
        }
    }

    private fun drawHeroShape(canvas: Canvas, role: Role, x: Float, y: Float, r: Float) {
        p.style = Paint.Style.FILL
        when (role) {
            Role.TANK -> {
                val rect = RectF(x - r, y - r * 0.9f, x + r, y + r * 0.9f)
                canvas.drawRoundRect(rect, r * 0.25f, r * 0.25f, p)
                val saved = p.color
                p.color = withAlpha(0xFF000000.toInt(), 40)
                p.strokeWidth = r * 0.08f
                p.style = Paint.Style.STROKE
                canvas.drawLine(x - r * 0.6f, y, x + r * 0.6f, y, p)
                p.color = saved
                p.style = Paint.Style.FILL
            }
            Role.DPS -> {
                path.reset()
                path.moveTo(x, y - r * 1.1f)
                path.lineTo(x + r, y)
                path.lineTo(x, y + r * 1.1f)
                path.lineTo(x - r, y)
                path.close()
                canvas.drawPath(path, p)
            }
            Role.SUPPORT -> {
                val arm = r * 0.35f
                path.reset()
                path.addRect(x - arm, y - r, x + arm, y + r, Path.Direction.CW)
                path.addRect(x - r, y - arm, x + r, y + arm, Path.Direction.CW)
                canvas.drawPath(path, p)
            }
        }
    }

    private fun drawEnemies(canvas: Canvas, enemies: List<com.mikmy.emberdepth.core.model.Enemy>, activeIndex: Int) {
        for ((i, enemy) in enemies.withIndex()) {
            if (enemy.hp <= BigNum.ZERO) continue
            val (x, y) = enemyScreenPos(i)
            val r = unit * (if (enemy.isBoss) 0.065f else 0.04f)
            val col = elementColor(enemy.element)
            val active = i == activeIndex

            if (active) {
                val pulse = 0.6f + 0.4f * sin(clock * 4f)
                p.style = Paint.Style.FILL
                p.color = withAlpha(col, (25 * pulse).toInt())
                canvas.drawCircle(x, y, r * 1.6f, p)
            }

            p.style = Paint.Style.FILL
            p.color = col
            drawEnemyShape(canvas, enemy.type, x, y, r)

            if (enemy.isBoss) {
                p.style = Paint.Style.STROKE
                p.strokeWidth = unit * 0.005f
                p.color = withAlpha(colGold, (160 + 80 * sin(clock * 3f)).toInt())
                canvas.drawCircle(x, y, r * 1.15f, p)
            }

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

            p.textSize = unit * 0.016f
            p.typeface = fontMedium
            p.textAlign = Paint.Align.CENTER
            p.color = withAlpha(colTextSecondary, 150)
            canvas.drawText(enemy.type.name, x, barY2 + barH + unit * 0.018f, p)
        }
    }

    private fun drawEnemyShape(canvas: Canvas, type: EnemyType, x: Float, y: Float, r: Float) {
        when (type) {
            EnemyType.SLIME -> {
                path.reset()
                path.moveTo(x - r, y)
                path.quadTo(x - r, y - r * 1.2f, x, y - r)
                path.quadTo(x + r, y - r * 1.2f, x + r, y)
                path.lineTo(x + r * 0.8f, y + r * 0.6f)
                path.lineTo(x - r * 0.8f, y + r * 0.6f)
                path.close()
                canvas.drawPath(path, p)
            }
            EnemyType.SKULL -> {
                canvas.drawCircle(x, y, r, p)
                val saved = p.color
                p.color = withAlpha(0xFF000000.toInt(), 180)
                canvas.drawCircle(x - r * 0.3f, y - r * 0.15f, r * 0.18f, p)
                canvas.drawCircle(x + r * 0.3f, y - r * 0.15f, r * 0.18f, p)
                canvas.drawCircle(x, y + r * 0.25f, r * 0.12f, p)
                p.color = saved
            }
            EnemyType.SPIKE -> {
                path.reset()
                for (i in 0..5) {
                    val angle = (Math.PI / 3.0 * i - Math.PI / 2).toFloat()
                    val px = x + r * cos(angle)
                    val py = y + r * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, p)
            }
            EnemyType.ORB -> {
                canvas.drawCircle(x, y, r, p)
                val saved = canvas.save()
                canvas.rotate(clock * 60f, x, y)
                val ringColor = p.color
                p.style = Paint.Style.STROKE
                p.strokeWidth = r * 0.1f
                p.color = withAlpha(ringColor, 120)
                canvas.drawCircle(x, y, r * 1.1f, p)
                p.style = Paint.Style.FILL
                p.color = ringColor
                canvas.restoreToCount(saved)
            }
            EnemyType.WYRM -> {
                val rect = RectF(x - r * 1.2f, y - r * 0.7f, x + r * 0.8f, y + r * 0.7f)
                canvas.drawOval(rect, p)
                path.reset()
                path.moveTo(x + r * 0.8f, y - r * 0.2f)
                path.lineTo(x + r * 1.4f, y)
                path.lineTo(x + r * 0.8f, y + r * 0.2f)
                path.close()
                canvas.drawPath(path, p)
            }
        }
    }

    private fun drawBossEntrance(canvas: Canvas) {
        val t = bossEntrance / 1.5f
        val scale = when {
            t > 0.8f -> (1f - t) / 0.2f
            t < 0.3f -> t / 0.3f
            else -> 1f
        }
        val alpha = (scale * 255).toInt().coerceIn(0, 255)

        p.typeface = fontBold
        p.textAlign = Paint.Align.CENTER
        p.textSize = unit * 0.08f * (0.5f + scale * 0.5f)
        p.color = withAlpha(colGold, alpha)
        canvas.drawText("BOSS", w * 0.5f, battleY + (h - battleY) * 0.4f, p)

        p.style = Paint.Style.STROKE
        p.strokeWidth = unit * 0.003f
        p.color = withAlpha(colGold, (alpha * 0.5f).toInt())
        val lineProgress = ((1f - t) * 2f).coerceIn(0f, 1f)
        canvas.drawLine(w * 0.1f, battleY + (h - battleY) * 0.5f,
            w * 0.1f + w * 0.8f * lineProgress, battleY + (h - battleY) * 0.5f, p)
    }

    private fun drawHud(canvas: Canvas, floor: Int, gold: BigNum, ember: BigNum) {
        p.style = Paint.Style.FILL
        p.color = colBg
        canvas.drawRect(0f, 0f, w, hudH, p)

        p.typeface = fontBold
        p.textAlign = Paint.Align.LEFT
        p.textSize = unit * 0.04f
        p.color = colTextPrimary
        canvas.drawText("FLOOR $floor", w * 0.05f, hudH * 0.55f, p)

        p.typeface = fontCond
        p.textSize = unit * 0.028f
        p.color = colGold
        p.textAlign = Paint.Align.RIGHT
        canvas.drawText(gold.format(), w * 0.95f, hudH * 0.42f, p)

        p.color = colEmber
        canvas.drawText(ember.format(), w * 0.95f, hudH * 0.75f, p)

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
        floatingText.push(targetX, targetY - unit * 0.03f, damage.format(), colTextPrimary, 0.6f, unit * 0.03f)
        val (hx, hy) = heroScreenPos(slot)
        attackFx.trigger(hx, hy, targetX, targetY, colTextPrimary, AttackFx.Style.SLASH)
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

    fun onBossFloor() {
        bossEntrance = 1.5f
        screenFx.triggerShake(0.006f, unit)
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

    private fun lerpColor(a: Int, b: Int, t: Float): Int {
        val aA = (a shr 24) and 0xFF; val aR = (a shr 16) and 0xFF
        val aG = (a shr 8) and 0xFF; val aB = a and 0xFF
        val bA = (b shr 24) and 0xFF; val bR = (b shr 16) and 0xFF
        val bG = (b shr 8) and 0xFF; val bB = b and 0xFF
        return ((aA + (bA - aA) * t).toInt() shl 24) or
            ((aR + (bR - aR) * t).toInt() shl 16) or
            ((aG + (bG - aG) * t).toInt() shl 8) or
            (aB + (bB - aB) * t).toInt()
    }
}
