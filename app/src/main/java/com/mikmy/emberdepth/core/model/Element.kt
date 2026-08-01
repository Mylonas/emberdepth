package com.mikmy.emberdepth.core.model

enum class Element(val label: String, val color: Long) {
    FIRE("Fire", 0xFFFF6B4A),
    WATER("Water", 0xFF4ADEFF),
    EARTH("Earth", 0xFFC6F24E),
    AIR("Air", 0xFFE0E0FF),
    LIGHT("Light", 0xFFFFD84D),
    DARK("Dark", 0xFFC86BFF);

    fun strongAgainst(): Element = when (this) {
        FIRE -> EARTH
        WATER -> FIRE
        EARTH -> AIR
        AIR -> WATER
        LIGHT -> DARK
        DARK -> LIGHT
    }

    fun weakAgainst(): Element = when (this) {
        FIRE -> WATER
        WATER -> EARTH
        EARTH -> FIRE
        AIR -> EARTH
        LIGHT -> DARK
        DARK -> LIGHT
    }
}
