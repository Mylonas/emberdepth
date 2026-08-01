package com.mikmy.emberdepth.core.economy

import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.model.MaterialType

data class ForgeRequirement(val materials: Map<MaterialType, Int>)

object ForgeRecipes {

    fun basicForge(): ForgeRequirement = ForgeRequirement(
        mapOf(
            MaterialType.ORE to Tuning.FORGE_ORE_COST,
            MaterialType.ESSENCE to Tuning.FORGE_ESSENCE_COST
        )
    )

    fun canForge(requirement: ForgeRequirement, inventory: Map<MaterialType, Int>): Boolean {
        return requirement.materials.all { (type, needed) ->
            (inventory[type] ?: 0) >= needed
        }
    }

    fun consumeMaterials(
        requirement: ForgeRequirement,
        inventory: MutableMap<MaterialType, Int>
    ): Boolean {
        if (!canForge(requirement, inventory)) return false
        for ((type, needed) in requirement.materials) {
            inventory[type] = (inventory[type] ?: 0) - needed
        }
        return true
    }
}
