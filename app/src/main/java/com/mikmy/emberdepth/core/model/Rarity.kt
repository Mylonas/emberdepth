package com.mikmy.emberdepth.core.model

enum class Rarity(val label: String, val color: Long, val statSlots: Int) {
    COMMON("Common", 0xFF8A8278, 0),
    UNCOMMON("Uncommon", 0xFF7CF6C0, 1),
    RARE("Rare", 0xFF4ADEFF, 2),
    EPIC("Epic", 0xFFC86BFF, 3),
    LEGENDARY("Legendary", 0xFFFFD84D, 3);
}
