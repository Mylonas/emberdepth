package com.mikmy.emberdepth.core.content

import com.mikmy.emberdepth.core.model.AchievementDef
import com.mikmy.emberdepth.core.model.StatType

object AchievementDefs {

    val ALL: List<AchievementDef> = listOf(
        AchievementDef(
            id = "floors_cleared",
            name = "Delver",
            description = "Clear dungeon floors",
            maxTier = 5,
            targets = listOf(10, 50, 200, 1000, 5000),
            bonusType = StatType.ATK,
            bonusPerTier = 0.02
        ),
        AchievementDef(
            id = "rebirths",
            name = "Phoenix",
            description = "Complete Rebirths",
            maxTier = 5,
            targets = listOf(1, 5, 15, 50, 100),
            bonusType = StatType.HP,
            bonusPerTier = 0.03
        ),
        AchievementDef(
            id = "bosses_slain",
            name = "Bane",
            description = "Defeat boss enemies",
            maxTier = 5,
            targets = listOf(1, 10, 50, 200, 500),
            bonusType = StatType.ATK,
            bonusPerTier = 0.02
        ),
        AchievementDef(
            id = "gear_forged",
            name = "Smith",
            description = "Forge pieces of equipment",
            maxTier = 5,
            targets = listOf(5, 25, 100, 500, 2000),
            bonusType = StatType.DEF,
            bonusPerTier = 0.02
        ),
        AchievementDef(
            id = "heroes_unlocked",
            name = "Commander",
            description = "Unlock heroes",
            maxTier = 3,
            targets = listOf(2, 4, 6),
            bonusType = StatType.HP,
            bonusPerTier = 0.05
        )
    )

    fun byId(id: String): AchievementDef? = ALL.find { it.id == id }
}
