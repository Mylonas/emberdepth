package com.mikmy.emberdepth.core.model

data class HeroDef(
    val id: String,
    val name: String,
    val element: Element,
    val role: Role,
    val baseStats: Stats,
    val unlockFloor: Int
)

data class HeroState(
    val id: String,
    val unlocked: Boolean = false,
    val level: Int = 1,
    val formationSlot: Int? = null,
    val weaponId: Long? = null,
    val armorId: Long? = null,
    val accessoryId: Long? = null
) {
    fun effectiveStats(def: HeroDef, gear: List<Gear>): Stats {
        val levelMultiplier = 1.0 + (level - 1) * 0.12
        var stats = def.baseStats * levelMultiplier
        for (g in gear) {
            stats = stats + g.toStats()
        }
        return stats
    }
}
