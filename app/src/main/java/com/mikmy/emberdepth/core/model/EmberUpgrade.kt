package com.mikmy.emberdepth.core.model

data class EmberUpgradeDef(
    val id: String,
    val name: String,
    val description: String,
    val maxLevel: Int,
    val baseCost: BigNum,
    val costScale: Double,
    val effect: UpgradeEffect,
    val requiredRebirthTier: Int = 0
)

data class EmberUpgradeState(
    val id: String,
    val level: Int = 0
)

sealed class UpgradeEffect {
    data class DamageMultiplier(val perLevel: Double) : UpgradeEffect()
    data class HpMultiplier(val perLevel: Double) : UpgradeEffect()
    data class GoldMultiplier(val perLevel: Double) : UpgradeEffect()
    data class OfflineEfficiency(val perLevel: Double) : UpgradeEffect()
    data class StartingFloor(val perLevel: Int) : UpgradeEffect()
    data class ExtraHeroSlot(val perLevel: Int) : UpgradeEffect()
}
