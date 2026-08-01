package com.mikmy.emberdepth.core.economy

import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.model.BigNum
import kotlin.math.pow

object GoldEconomy {

    fun heroLevelCost(heroLevel: Int): BigNum {
        val cost = Tuning.HERO_LEVEL_COST_BASE * Tuning.HERO_LEVEL_COST_SCALE.pow(heroLevel)
        return BigNum.of(cost)
    }

    fun canAffordLevel(gold: BigNum, heroLevel: Int): Boolean {
        return gold >= heroLevelCost(heroLevel)
    }

    fun forgeCost(floor: Int): BigNum {
        return BigNum.of(Tuning.GOLD_BASE * 5.0 * Tuning.GOLD_SCALE.pow(floor * 0.5))
    }
}
