package com.mikmy.emberdepth.core

import com.mikmy.emberdepth.core.engine.DamageCalc
import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Stats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DamageCalcTest {

    @Test
    fun `basic damage is atk minus half def`() {
        val atk = Stats(atk = BigNum.of(100))
        val def = Stats(def = BigNum.of(20))
        val result = DamageCalc.calculate(atk, Element.FIRE, def, Element.FIRE)
        assertEquals(90.0, result.toDouble(), 1.0)
    }

    @Test
    fun `minimum damage is 1`() {
        val atk = Stats(atk = BigNum.of(1))
        val def = Stats(def = BigNum.of(1000))
        val result = DamageCalc.calculate(atk, Element.FIRE, def, Element.FIRE)
        assertTrue(result >= BigNum.ONE)
    }

    @Test
    fun `element advantage increases damage`() {
        val atk = Stats(atk = BigNum.of(100))
        val def = Stats(def = BigNum.ZERO)
        val neutral = DamageCalc.calculate(atk, Element.FIRE, def, Element.FIRE)
        val strong = DamageCalc.calculate(atk, Element.FIRE, def, Element.EARTH)
        assertTrue(strong > neutral)
    }

    @Test
    fun `element disadvantage decreases damage`() {
        val atk = Stats(atk = BigNum.of(100))
        val def = Stats(def = BigNum.ZERO)
        val neutral = DamageCalc.calculate(atk, Element.FIRE, def, Element.FIRE)
        val weak = DamageCalc.calculate(atk, Element.FIRE, def, Element.WATER)
        assertTrue(weak < neutral)
    }

    @Test
    fun `element multiplier values are correct`() {
        assertEquals(Tuning.ELEMENT_STRONG_MULT, DamageCalc.elementMultiplier(Element.FIRE, Element.EARTH), 0.01)
        assertEquals(Tuning.ELEMENT_WEAK_MULT, DamageCalc.elementMultiplier(Element.FIRE, Element.WATER), 0.01)
        assertEquals(1.0, DamageCalc.elementMultiplier(Element.FIRE, Element.FIRE), 0.01)
    }
}
