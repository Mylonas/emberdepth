package com.mikmy.emberdepth.core.content

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.Role
import com.mikmy.emberdepth.core.model.Stats

object HeroRegistry {

    val ALL: List<HeroDef> = listOf(
        HeroDef(
            id = "kael",
            name = "Kael",
            element = Element.FIRE,
            role = Role.DPS,
            baseStats = Stats(hp = BigNum.of(80), atk = BigNum.of(18), def = BigNum.of(5), spd = 1.2),
            unlockFloor = 0
        ),
        HeroDef(
            id = "maren",
            name = "Maren",
            element = Element.WATER,
            role = Role.SUPPORT,
            baseStats = Stats(hp = BigNum.of(100), atk = BigNum.of(8), def = BigNum.of(8), spd = 1.0),
            unlockFloor = 10
        ),
        HeroDef(
            id = "thorne",
            name = "Thorne",
            element = Element.EARTH,
            role = Role.TANK,
            baseStats = Stats(hp = BigNum.of(160), atk = BigNum.of(10), def = BigNum.of(15), spd = 0.8),
            unlockFloor = 25
        ),
        HeroDef(
            id = "zephyr",
            name = "Zephyr",
            element = Element.AIR,
            role = Role.DPS,
            baseStats = Stats(hp = BigNum.of(70), atk = BigNum.of(15), def = BigNum.of(4), spd = 1.6),
            unlockFloor = 50
        ),
        HeroDef(
            id = "solara",
            name = "Solara",
            element = Element.LIGHT,
            role = Role.SUPPORT,
            baseStats = Stats(hp = BigNum.of(120), atk = BigNum.of(12), def = BigNum.of(10), spd = 1.1),
            unlockFloor = 80
        ),
        HeroDef(
            id = "nyx",
            name = "Nyx",
            element = Element.DARK,
            role = Role.DPS,
            baseStats = Stats(hp = BigNum.of(90), atk = BigNum.of(22), def = BigNum.of(3), spd = 1.3),
            unlockFloor = 120
        )
    )

    fun byId(id: String): HeroDef? = ALL.find { it.id == id }

    fun starterHero(): HeroDef = ALL.first()

    fun heroesUnlockedAt(floor: Int): List<HeroDef> =
        ALL.filter { it.unlockFloor <= floor }
}
