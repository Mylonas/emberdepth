package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Stats

object DamageCalc {

    fun calculate(
        attackerStats: Stats,
        attackerElement: Element,
        defenderStats: Stats,
        defenderElement: Element,
        damageMultiplier: Double = 1.0
    ): BigNum {
        val raw = attackerStats.atk - defenderStats.def * 0.5
        val clamped = if (raw < BigNum.ONE) BigNum.ONE else raw

        val elementMult = elementMultiplier(attackerElement, defenderElement)

        return clamped * elementMult * damageMultiplier
    }

    fun elementMultiplier(attacker: Element, defender: Element): Double {
        return when {
            attacker.strongAgainst() == defender -> Tuning.ELEMENT_STRONG_MULT
            attacker.weakAgainst() == defender -> Tuning.ELEMENT_WEAK_MULT
            else -> 1.0
        }
    }
}
