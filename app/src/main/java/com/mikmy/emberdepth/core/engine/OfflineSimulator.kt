package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.MaterialType
import kotlin.math.min
import kotlin.math.pow

data class OfflineResult(
    val elapsedMs: Long,
    val floorsCleared: Int,
    val goldEarned: BigNum,
    val materialsEarned: Map<MaterialType, Int>,
    val newFloor: Int
)

object OfflineSimulator {

    fun simulate(
        startFloor: Int,
        partyPower: BigNum,
        elapsedMs: Long,
        offlineEfficiency: Double = Tuning.OFFLINE_BASE_EFFICIENCY,
        goldMultiplier: Double = 1.0
    ): OfflineResult {
        val maxMs = (Tuning.OFFLINE_MAX_HOURS * 3600 * 1000).toLong()
        val cappedMs = min(elapsedMs, maxMs)
        val hours = cappedMs / 3600000.0

        val wallFloor = estimateWallFloor(partyPower)
        val clearableFloors = (wallFloor - startFloor).coerceAtLeast(0)
        val floorsPerHour = (clearableFloors * 0.4).coerceAtLeast(1.0)
        val rawFloors = (floorsPerHour * hours * offlineEfficiency).toInt()
        val floorsCleared = min(rawFloors, clearableFloors)

        var totalGold = BigNum.ZERO
        val materials = mutableMapOf<MaterialType, Int>()
        for (f in startFloor until startFloor + floorsCleared) {
            totalGold = totalGold + LootGenerator.rollGoldReward(f, f % Tuning.BOSS_INTERVAL == 0, goldMultiplier)
            val matChance = Tuning.MATERIAL_DROP_BASE_CHANCE * offlineEfficiency
            if (f >= 0) addMaterial(materials, MaterialType.ORE, 1, matChance)
            if (f >= 20) addMaterial(materials, MaterialType.ESSENCE, 1, matChance * 0.6)
            if (f >= 50) addMaterial(materials, MaterialType.FRAGMENT, 1, matChance * 0.4)
            if (f >= 100) addMaterial(materials, MaterialType.CRYSTAL, 1, matChance * 0.2)
            if (f >= 200) addMaterial(materials, MaterialType.RELIC, 1, matChance * 0.1)
        }

        return OfflineResult(
            elapsedMs = cappedMs,
            floorsCleared = floorsCleared,
            goldEarned = totalGold,
            materialsEarned = materials,
            newFloor = startFloor + floorsCleared
        )
    }

    private fun estimateWallFloor(partyPower: BigNum): Int {
        if (partyPower.isZero()) return 1
        val powerLog = partyPower.exponent + kotlin.math.log10(partyPower.mantissa)
        return (powerLog / kotlin.math.log10(Tuning.ENEMY_HP_SCALE)).toInt()
            .coerceAtLeast(1)
    }

    private fun addMaterial(map: MutableMap<MaterialType, Int>, type: MaterialType, amount: Int, chance: Double) {
        val count = (amount * chance).toInt().coerceAtLeast(if (chance >= 0.5) 1 else 0)
        if (count > 0) map[type] = (map[type] ?: 0) + count
    }
}
