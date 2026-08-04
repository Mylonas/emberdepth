package com.mikmy.emberdepth.core.model

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Compact large-number representation: mantissa × 10^exponent.
 * Avoids BigInteger overhead in the hot combat loop while supporting
 * numbers up to ~1e308. Display formatting ("1.23M", "4.56T") is built in.
 */
data class BigNum(val mantissa: Double = 0.0, val exponent: Int = 0) : Comparable<BigNum> {

    companion object {
        val ZERO = BigNum(0.0, 0)
        val ONE = BigNum(1.0, 0)

        fun of(value: Double): BigNum {
            if (value == 0.0) return ZERO
            if (value.isNaN()) return ZERO
            if (value.isInfinite()) return BigNum(if (value > 0) 9.999 else -9.999, 308)
            val e = floor(log10(abs(value))).toInt()
            return BigNum(value / 10.0.pow(e), e).normalize()
        }

        fun of(value: Long): BigNum = of(value.toDouble())
        fun of(value: Int): BigNum = of(value.toDouble())

        private val SUFFIXES = arrayOf(
            "", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc",
            "No", "Dc", "Ud", "Dd", "Td", "Qad", "Qid", "Sxd", "Spd", "Ocd",
            "Nod", "Vg"
        )
    }

    fun normalize(): BigNum {
        if (mantissa == 0.0) return ZERO
        var m = mantissa
        var e = exponent
        while (abs(m) >= 10.0) { m /= 10.0; e++ }
        while (abs(m) < 1.0 && m != 0.0) { m *= 10.0; e-- }
        return BigNum(m, e)
    }

    operator fun plus(other: BigNum): BigNum {
        if (mantissa == 0.0) return other
        if (other.mantissa == 0.0) return this
        val diff = exponent - other.exponent
        return if (diff >= 0) {
            BigNum(mantissa + other.mantissa / 10.0.pow(diff), exponent).normalize()
        } else {
            BigNum(mantissa / 10.0.pow(-diff) + other.mantissa, other.exponent).normalize()
        }
    }

    operator fun minus(other: BigNum): BigNum {
        return this + BigNum(-other.mantissa, other.exponent)
    }

    operator fun times(other: BigNum): BigNum {
        return BigNum(mantissa * other.mantissa, exponent + other.exponent).normalize()
    }

    operator fun times(scalar: Double): BigNum {
        return BigNum(mantissa * scalar, exponent).normalize()
    }

    operator fun div(other: BigNum): BigNum {
        if (other.mantissa == 0.0) return ZERO
        return BigNum(mantissa / other.mantissa, exponent - other.exponent).normalize()
    }

    override fun compareTo(other: BigNum): Int {
        if (mantissa == 0.0 && other.mantissa == 0.0) return 0
        val signA = if (mantissa >= 0) 1 else -1
        val signB = if (other.mantissa >= 0) 1 else -1
        if (signA != signB) return signA - signB
        val expCmp = exponent.compareTo(other.exponent)
        if (expCmp != 0) return expCmp * signA
        return mantissa.compareTo(other.mantissa)
    }

    fun toDouble(): Double = mantissa * 10.0.pow(exponent)

    fun toLong(): Long = if (exponent > 18) Long.MAX_VALUE else toDouble().toLong()

    fun isZero(): Boolean = mantissa == 0.0

    fun format(): String {
        if (mantissa == 0.0) return "0"
        val n = normalize()
        if (n.exponent < 4) {
            val raw = n.toDouble()
            return if (raw == floor(raw) && raw < 1_000_000) {
                raw.toLong().toString()
            } else {
                "%.1f".format(raw)
            }
        }
        val suffixIndex = n.exponent / 3
        if (suffixIndex >= SUFFIXES.size) {
            return "%.2fe%d".format(n.mantissa, n.exponent)
        }
        val displayMantissa = n.mantissa * 10.0.pow(n.exponent % 3)
        return "%.2f%s".format(displayMantissa, SUFFIXES[suffixIndex])
    }

    override fun toString(): String = format()
}
