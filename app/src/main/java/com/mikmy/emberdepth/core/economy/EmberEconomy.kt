package com.mikmy.emberdepth.core.economy

import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.EmberUpgradeDef
import com.mikmy.emberdepth.core.model.UpgradeEffect
import kotlin.math.pow

object EmberEconomy {

    fun emberFromRebirth(highestFloor: Int): BigNum {
        if (highestFloor < 50) return BigNum.ZERO
        val base = Tuning.EMBER_BASE * Tuning.EMBER_SCALE.pow(highestFloor)
        return BigNum.of(base)
    }

    fun rebirthTier(rebirthCount: Int): Int = when {
        rebirthCount >= 50 -> 5
        rebirthCount >= 20 -> 4
        rebirthCount >= 10 -> 3
        rebirthCount >= 5 -> 2
        rebirthCount >= 1 -> 1
        else -> 0
    }

    fun upgradeCost(def: EmberUpgradeDef, currentLevel: Int): BigNum {
        return def.baseCost * def.costScale.pow(currentLevel).toDouble()
    }

    fun canAffordUpgrade(ember: BigNum, def: EmberUpgradeDef, currentLevel: Int): Boolean {
        if (currentLevel >= def.maxLevel) return false
        return ember >= upgradeCost(def, currentLevel)
    }

    fun totalDamageMultiplier(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Double {
        var mult = 1.0
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.DamageMultiplier) {
                mult *= 1.0 + def.effect.perLevel * level
            }
        }
        return mult
    }

    fun totalGoldMultiplier(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Double {
        var mult = 1.0
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.GoldMultiplier) {
                mult *= 1.0 + def.effect.perLevel * level
            }
        }
        return mult
    }

    fun offlineEfficiency(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Double {
        var eff = Tuning.OFFLINE_BASE_EFFICIENCY
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.OfflineEfficiency) {
                eff += def.effect.perLevel * level
            }
        }
        return eff.coerceAtMost(0.95)
    }

    fun totalHpMultiplier(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Double {
        var mult = 1.0
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.HpMultiplier) {
                mult *= 1.0 + def.effect.perLevel * level
            }
        }
        return mult
    }

    fun extraHeroSlots(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Int {
        var slots = 0
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.ExtraHeroSlot) {
                slots += def.effect.perLevel * level
            }
        }
        return slots
    }

    fun startingFloor(upgrades: Map<String, Int>, defs: List<EmberUpgradeDef>): Int {
        var floor = 1
        for (def in defs) {
            val level = upgrades[def.id] ?: 0
            if (level > 0 && def.effect is UpgradeEffect.StartingFloor) {
                floor += def.effect.perLevel * level
            }
        }
        return floor
    }
}
