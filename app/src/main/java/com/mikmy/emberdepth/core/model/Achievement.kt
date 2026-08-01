package com.mikmy.emberdepth.core.model

data class AchievementDef(
    val id: String,
    val name: String,
    val description: String,
    val maxTier: Int,
    val targets: List<Long>,
    val bonusType: StatType,
    val bonusPerTier: Double
)

data class AchievementState(
    val id: String,
    val tier: Int = 0,
    val progress: Long = 0
)
