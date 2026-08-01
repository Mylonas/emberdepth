package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.GearSlot
import com.mikmy.emberdepth.core.model.LootDrop
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.core.model.Rarity
import com.mikmy.emberdepth.core.model.StatBonus
import com.mikmy.emberdepth.core.model.StatType
import kotlin.random.Random

object LootGenerator {

    data class MaterialReward(val type: MaterialType, val amount: Int)

    fun rollMaterials(lootTable: List<LootDrop>, rng: Random = Random): List<MaterialReward> {
        return lootTable.mapNotNull { drop ->
            if (rng.nextDouble() < drop.chance) {
                MaterialReward(drop.materialType, drop.amount)
            } else null
        }
    }

    fun rollGoldReward(floor: Int, isBoss: Boolean, goldMultiplier: Double = 1.0): BigNum {
        val base = Tuning.GOLD_BASE * Math.pow(Tuning.GOLD_SCALE, floor.toDouble())
        val mult = if (isBoss) Tuning.BOSS_GOLD_MULT else 1.0
        return BigNum.of(base * mult * goldMultiplier)
    }

    fun forgeGear(
        level: Int,
        guaranteedRarityUpgrade: Boolean = false,
        rng: Random = Random
    ): Gear {
        val slot = GearSlot.entries[rng.nextInt(GearSlot.entries.size)]
        val rarity = rollRarity(level, guaranteedRarityUpgrade, rng)
        val element = if (rng.nextDouble() < 0.3) Element.entries[rng.nextInt(Element.entries.size)] else null

        val primaryType = primaryStatForSlot(slot)
        val baseValue = (5 + level * 3) * rarityMultiplier(rarity)
        val primaryStat = StatBonus(primaryType, BigNum.of(baseValue))

        val secondaryStats = (0 until rarity.statSlots).map {
            val type = StatType.entries[rng.nextInt(StatType.entries.size)]
            val value = baseValue * (0.2 + rng.nextDouble() * 0.3)
            StatBonus(type, BigNum.of(value))
        }

        return Gear(
            slot = slot,
            rarity = rarity,
            level = level,
            primaryStat = primaryStat,
            secondaryStats = secondaryStats,
            element = element
        )
    }

    private fun rollRarity(level: Int, upgraded: Boolean, rng: Random): Rarity {
        val roll = rng.nextDouble()
        val bonus = if (upgraded) 0.15 else 0.0
        val levelBonus = (level * 0.002).coerceAtMost(0.15)
        val adjusted = roll + bonus + levelBonus

        return when {
            adjusted > 0.98 -> Rarity.LEGENDARY
            adjusted > 0.90 -> Rarity.EPIC
            adjusted > 0.70 -> Rarity.RARE
            adjusted > 0.40 -> Rarity.UNCOMMON
            else -> Rarity.COMMON
        }
    }

    private fun primaryStatForSlot(slot: GearSlot): StatType = when (slot) {
        GearSlot.WEAPON -> StatType.ATK
        GearSlot.ARMOR -> StatType.DEF
        GearSlot.ACCESSORY -> StatType.HP
    }

    private fun rarityMultiplier(rarity: Rarity): Double = when (rarity) {
        Rarity.COMMON -> 1.0
        Rarity.UNCOMMON -> 1.3
        Rarity.RARE -> 1.7
        Rarity.EPIC -> 2.2
        Rarity.LEGENDARY -> 3.0
    }
}
