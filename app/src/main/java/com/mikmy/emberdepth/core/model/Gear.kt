package com.mikmy.emberdepth.core.model

enum class GearSlot { WEAPON, ARMOR, ACCESSORY }

data class Gear(
    val id: Long = 0,
    val slot: GearSlot,
    val rarity: Rarity,
    val level: Int = 1,
    val primaryStat: StatBonus,
    val secondaryStats: List<StatBonus> = emptyList(),
    val element: Element? = null,
    val equippedBy: String? = null
) {
    fun toStats(): Stats {
        var stats = bonusToStats(primaryStat)
        for (s in secondaryStats) {
            stats = stats + bonusToStats(s)
        }
        return stats
    }

    private fun bonusToStats(bonus: StatBonus): Stats = when (bonus.type) {
        StatType.HP -> Stats(hp = bonus.value)
        StatType.ATK -> Stats(atk = bonus.value)
        StatType.DEF -> Stats(def = bonus.value)
        StatType.SPD -> Stats(spd = bonus.value.toDouble())
    }
}
