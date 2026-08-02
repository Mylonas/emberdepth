package com.mikmy.emberdepth.core.content

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.MaterialType

data class DailyReward(
    val day: Int,
    val gold: BigNum = BigNum.ZERO,
    val materials: Map<MaterialType, Int> = emptyMap(),
    val ember: BigNum = BigNum.ZERO
)

object DailyRewards {

    private val rewards = listOf(
        DailyReward(1, gold = BigNum.of(100)),
        DailyReward(2, materials = mapOf(MaterialType.ORE to 5)),
        DailyReward(3, gold = BigNum.of(250)),
        DailyReward(4, materials = mapOf(MaterialType.ORE to 3, MaterialType.ESSENCE to 2)),
        DailyReward(5, gold = BigNum.of(500)),
        DailyReward(6, materials = mapOf(MaterialType.ESSENCE to 3, MaterialType.FRAGMENT to 1)),
        DailyReward(7, gold = BigNum.of(1000), ember = BigNum.of(1))
    )

    fun forDay(dayCount: Int): DailyReward {
        val index = ((dayCount - 1) % rewards.size)
        return rewards[index].copy(day = dayCount)
    }
}
