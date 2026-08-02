package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.AchievementDef
import com.mikmy.emberdepth.core.model.AchievementState

object AchievementChecker {

    data class Result(
        val updated: List<AchievementState>,
        val advanced: List<Pair<AchievementDef, Int>>
    )

    fun evaluate(
        defs: List<AchievementDef>,
        states: List<AchievementState>,
        stats: Map<String, Long>
    ): Result {
        val stateMap = states.associateBy { it.id }.toMutableMap()
        val advanced = mutableListOf<Pair<AchievementDef, Int>>()

        for (def in defs) {
            val current = stateMap[def.id] ?: AchievementState(def.id)
            val statValue = stats[def.id] ?: 0L
            var tier = current.tier

            while (tier < def.maxTier && statValue >= def.targets[tier]) {
                tier++
            }

            val updatedState = current.copy(tier = tier, progress = statValue)
            stateMap[def.id] = updatedState

            if (tier > current.tier) {
                advanced.add(def to tier)
            }
        }

        return Result(
            updated = stateMap.values.toList(),
            advanced = advanced
        )
    }

    fun totalBonus(
        defs: List<AchievementDef>,
        states: List<AchievementState>
    ): Map<com.mikmy.emberdepth.core.model.StatType, Double> {
        val bonuses = mutableMapOf<com.mikmy.emberdepth.core.model.StatType, Double>()
        val stateMap = states.associateBy { it.id }
        for (def in defs) {
            val tier = stateMap[def.id]?.tier ?: 0
            if (tier > 0) {
                val current = bonuses[def.bonusType] ?: 0.0
                bonuses[def.bonusType] = current + def.bonusPerTier * tier
            }
        }
        return bonuses
    }
}
