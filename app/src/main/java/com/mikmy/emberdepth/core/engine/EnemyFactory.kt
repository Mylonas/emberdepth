package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Enemy
import com.mikmy.emberdepth.core.model.EnemyType
import com.mikmy.emberdepth.core.model.LootDrop
import com.mikmy.emberdepth.core.model.MaterialType
import kotlin.math.pow
import kotlin.random.Random

object EnemyFactory {

    fun create(floor: Int, seed: Long = System.nanoTime()): Enemy {
        val rng = Random(seed xor floor.toLong())
        val isBoss = floor % Tuning.BOSS_INTERVAL == 0 && floor > 0
        val element = Element.entries[rng.nextInt(Element.entries.size)]

        val hpBase = Tuning.ENEMY_HP_BASE * Tuning.ENEMY_HP_SCALE.pow(floor)
        val atkBase = Tuning.ENEMY_ATK_BASE * Tuning.ENEMY_ATK_SCALE.pow(floor)
        val defBase = Tuning.ENEMY_DEF_BASE * Tuning.ENEMY_DEF_SCALE.pow(floor)

        val hp = BigNum.of(if (isBoss) hpBase * Tuning.BOSS_HP_MULT else hpBase)
        val atk = BigNum.of(if (isBoss) atkBase * Tuning.BOSS_ATK_MULT else atkBase)
        val def = BigNum.of(if (isBoss) defBase * Tuning.BOSS_DEF_MULT else defBase)

        val type = if (isBoss) {
            if (rng.nextBoolean()) EnemyType.WYRM else EnemyType.SKULL
        } else {
            EnemyType.entries[rng.nextInt(EnemyType.entries.size)]
        }

        return Enemy(
            floor = floor,
            element = element,
            hp = hp,
            maxHp = hp,
            atk = atk,
            def = def,
            isBoss = isBoss,
            lootTable = buildLootTable(floor, isBoss, rng),
            type = type
        )
    }

    private fun buildLootTable(floor: Int, isBoss: Boolean, rng: Random): List<LootDrop> {
        val drops = mutableListOf<LootDrop>()
        val baseChance = if (isBoss) 1.0 else Tuning.MATERIAL_DROP_BASE_CHANCE
        val amount = if (isBoss) 3 + floor / 50 else 1 + floor / 100

        drops.add(LootDrop(MaterialType.ORE, amount, baseChance))
        if (floor >= 20) drops.add(LootDrop(MaterialType.ESSENCE, amount, baseChance * 0.6))
        if (floor >= 50) drops.add(LootDrop(MaterialType.FRAGMENT, amount, baseChance * 0.4))
        if (floor >= 100) drops.add(LootDrop(MaterialType.CRYSTAL, amount, baseChance * 0.2))
        if (floor >= 200) drops.add(LootDrop(MaterialType.RELIC, amount, baseChance * 0.1))

        return drops
    }
}
