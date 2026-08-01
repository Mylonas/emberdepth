package com.mikmy.emberdepth.core

import com.mikmy.emberdepth.core.engine.LootGenerator
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Rarity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LootGeneratorTest {

    @Test
    fun `gold reward increases with floor`() {
        val floor10 = LootGenerator.rollGoldReward(10, false)
        val floor50 = LootGenerator.rollGoldReward(50, false)
        assertTrue(floor50 > floor10)
    }

    @Test
    fun `boss gold is higher than normal`() {
        val normal = LootGenerator.rollGoldReward(50, false)
        val boss = LootGenerator.rollGoldReward(50, true)
        assertTrue(boss > normal)
    }

    @Test
    fun `forged gear has valid properties`() {
        val gear = LootGenerator.forgeGear(10, rng = Random(42))
        assertNotNull(gear.slot)
        assertNotNull(gear.rarity)
        assertNotNull(gear.primaryStat)
        assertTrue(gear.primaryStat.value > BigNum.ZERO)
    }

    @Test
    fun `rarity upgrade increases chance of better gear`() {
        var upgradedRareCount = 0
        var normalRareCount = 0
        val runs = 1000
        for (i in 0 until runs) {
            val upgraded = LootGenerator.forgeGear(50, guaranteedRarityUpgrade = true, rng = Random(i.toLong()))
            val normal = LootGenerator.forgeGear(50, guaranteedRarityUpgrade = false, rng = Random(i.toLong()))
            if (upgraded.rarity.ordinal >= Rarity.RARE.ordinal) upgradedRareCount++
            if (normal.rarity.ordinal >= Rarity.RARE.ordinal) normalRareCount++
        }
        assertTrue(upgradedRareCount > normalRareCount)
    }
}
