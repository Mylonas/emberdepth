package com.mikmy.emberdepth.core.content

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.EmberUpgradeDef
import com.mikmy.emberdepth.core.model.UpgradeEffect

object EmberUpgradeDefs {

    val ALL: List<EmberUpgradeDef> = listOf(
        EmberUpgradeDef(
            id = "ember_atk",
            name = "Ember Strike",
            description = "Increase all hero damage",
            maxLevel = 100,
            baseCost = BigNum.of(5),
            costScale = 1.18,
            effect = UpgradeEffect.DamageMultiplier(perLevel = 0.10),
            requiredRebirthTier = 0
        ),
        EmberUpgradeDef(
            id = "ember_hp",
            name = "Ember Ward",
            description = "Increase all hero HP",
            maxLevel = 100,
            baseCost = BigNum.of(5),
            costScale = 1.18,
            effect = UpgradeEffect.HpMultiplier(perLevel = 0.10),
            requiredRebirthTier = 0
        ),
        EmberUpgradeDef(
            id = "ember_gold",
            name = "Ember Fortune",
            description = "Increase gold earned from enemies",
            maxLevel = 50,
            baseCost = BigNum.of(10),
            costScale = 1.22,
            effect = UpgradeEffect.GoldMultiplier(perLevel = 0.15),
            requiredRebirthTier = 0
        ),
        EmberUpgradeDef(
            id = "ember_offline",
            name = "Smoldering Embers",
            description = "Increase offline farming efficiency",
            maxLevel = 20,
            baseCost = BigNum.of(25),
            costScale = 1.30,
            effect = UpgradeEffect.OfflineEfficiency(perLevel = 0.02),
            requiredRebirthTier = 1
        ),
        EmberUpgradeDef(
            id = "ember_start",
            name = "Ember Surge",
            description = "Start each run on a higher floor",
            maxLevel = 50,
            baseCost = BigNum.of(50),
            costScale = 1.25,
            effect = UpgradeEffect.StartingFloor(perLevel = 5),
            requiredRebirthTier = 2
        ),
        EmberUpgradeDef(
            id = "ember_slot",
            name = "Ember Bond",
            description = "Unlock additional hero formation slots",
            maxLevel = 2,
            baseCost = BigNum.of(200),
            costScale = 3.0,
            effect = UpgradeEffect.ExtraHeroSlot(perLevel = 1),
            requiredRebirthTier = 3
        )
    )

    fun byId(id: String): EmberUpgradeDef? = ALL.find { it.id == id }
}
