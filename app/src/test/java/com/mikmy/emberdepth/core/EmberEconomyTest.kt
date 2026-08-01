package com.mikmy.emberdepth.core

import com.mikmy.emberdepth.core.economy.EmberEconomy
import com.mikmy.emberdepth.core.model.BigNum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmberEconomyTest {

    @Test
    fun `no ember before floor 50`() {
        assertTrue(EmberEconomy.emberFromRebirth(10).isZero())
        assertTrue(EmberEconomy.emberFromRebirth(49).isZero())
    }

    @Test
    fun `ember earned at floor 50`() {
        val ember = EmberEconomy.emberFromRebirth(50)
        assertTrue(ember > BigNum.ZERO)
    }

    @Test
    fun `deeper floors give more ember`() {
        val floor100 = EmberEconomy.emberFromRebirth(100)
        val floor200 = EmberEconomy.emberFromRebirth(200)
        assertTrue(floor200 > floor100)
    }

    @Test
    fun `ember scales exponentially`() {
        val f100 = EmberEconomy.emberFromRebirth(100).toDouble()
        val f200 = EmberEconomy.emberFromRebirth(200).toDouble()
        val f300 = EmberEconomy.emberFromRebirth(300).toDouble()
        val ratio1 = f200 / f100
        val ratio2 = f300 / f200
        assertTrue(ratio2 > ratio1 * 0.8)
    }

    @Test
    fun `rebirth tier progresses correctly`() {
        assertEquals(0, EmberEconomy.rebirthTier(0))
        assertEquals(1, EmberEconomy.rebirthTier(1))
        assertEquals(2, EmberEconomy.rebirthTier(5))
        assertEquals(3, EmberEconomy.rebirthTier(10))
        assertEquals(4, EmberEconomy.rebirthTier(20))
        assertEquals(5, EmberEconomy.rebirthTier(50))
    }
}
