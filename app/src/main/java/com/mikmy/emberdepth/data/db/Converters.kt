package com.mikmy.emberdepth.data.db

import com.mikmy.emberdepth.core.model.BigNum

object Converters {

    fun bigNumToString(value: BigNum): String =
        "${value.mantissa}:${value.exponent}"

    fun stringToBigNum(value: String): BigNum {
        val parts = value.split(":")
        if (parts.size != 2) return BigNum.ZERO
        val m = parts[0].toDoubleOrNull() ?: return BigNum.ZERO
        val e = parts[1].toIntOrNull() ?: return BigNum.ZERO
        return BigNum(m, e)
    }
}
