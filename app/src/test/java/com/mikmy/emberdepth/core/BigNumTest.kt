package com.mikmy.emberdepth.core

import com.mikmy.emberdepth.core.model.BigNum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BigNumTest {

    @Test
    fun `zero is zero`() {
        assertEquals("0", BigNum.ZERO.format())
        assertTrue(BigNum.ZERO.isZero())
    }

    @Test
    fun `small numbers format as integers`() {
        assertEquals("42", BigNum.of(42).format())
        assertEquals("999", BigNum.of(999).format())
    }

    @Test
    fun `thousands use K suffix`() {
        val n = BigNum.of(12345)
        assertTrue(n.format().contains("K"))
    }

    @Test
    fun `millions use M suffix`() {
        val n = BigNum.of(5_000_000)
        assertTrue(n.format().contains("M"))
    }

    @Test
    fun `addition works`() {
        val a = BigNum.of(100)
        val b = BigNum.of(50)
        val result = a + b
        assertEquals(150.0, result.toDouble(), 0.1)
    }

    @Test
    fun `subtraction works`() {
        val a = BigNum.of(100)
        val b = BigNum.of(30)
        val result = a - b
        assertEquals(70.0, result.toDouble(), 0.1)
    }

    @Test
    fun `multiplication works`() {
        val a = BigNum.of(100)
        val b = BigNum.of(50)
        val result = a * b
        assertEquals(5000.0, result.toDouble(), 1.0)
    }

    @Test
    fun `comparison works`() {
        val a = BigNum.of(100)
        val b = BigNum.of(200)
        assertTrue(a < b)
        assertTrue(b > a)
    }

    @Test
    fun `large numbers compare correctly`() {
        val a = BigNum(1.5, 100)
        val b = BigNum(2.0, 99)
        assertTrue(a > b)
    }

    @Test
    fun `scalar multiplication works`() {
        val a = BigNum.of(100)
        val result = a * 1.5
        assertEquals(150.0, result.toDouble(), 0.1)
    }

    @Test
    fun `division works`() {
        val a = BigNum.of(100)
        val b = BigNum.of(4)
        val result = a / b
        assertEquals(25.0, result.toDouble(), 0.1)
    }
}
