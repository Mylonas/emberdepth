package com.mikmy.emberdepth.core.model

enum class MaterialType(val label: String, val color: Long) {
    ORE("Ore", 0xFF8A8278),
    ESSENCE("Essence", 0xFF4ADEFF),
    FRAGMENT("Fragment", 0xFFC6F24E),
    CRYSTAL("Crystal", 0xFFC86BFF),
    RELIC("Relic", 0xFFFFD84D);
}
