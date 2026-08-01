package com.mikmy.emberdepth.core.model

data class Stats(
    val hp: BigNum = BigNum.ZERO,
    val atk: BigNum = BigNum.ZERO,
    val def: BigNum = BigNum.ZERO,
    val spd: Double = 1.0
) {
    operator fun plus(other: Stats) = Stats(
        hp = hp + other.hp,
        atk = atk + other.atk,
        def = def + other.def,
        spd = spd + other.spd
    )

    operator fun times(multiplier: Double) = Stats(
        hp = hp * multiplier,
        atk = atk * multiplier,
        def = def * multiplier,
        spd = spd * multiplier
    )
}

enum class StatType { HP, ATK, DEF, SPD }

data class StatBonus(val type: StatType, val value: BigNum)
