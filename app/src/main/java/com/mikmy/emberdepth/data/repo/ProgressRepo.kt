package com.mikmy.emberdepth.data.repo

import com.mikmy.emberdepth.core.model.AchievementState
import com.mikmy.emberdepth.core.model.EmberUpgradeState
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.data.db.AchievementDao
import com.mikmy.emberdepth.data.db.AchievementEntity
import com.mikmy.emberdepth.data.db.EmberUpgradeDao
import com.mikmy.emberdepth.data.db.EmberUpgradeEntity
import com.mikmy.emberdepth.data.db.MaterialDao
import com.mikmy.emberdepth.data.db.MaterialEntity
import com.mikmy.emberdepth.data.db.StatsDao
import com.mikmy.emberdepth.data.db.StatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepo @Inject constructor(
    private val materialDao: MaterialDao,
    private val emberUpgradeDao: EmberUpgradeDao,
    private val achievementDao: AchievementDao,
    private val statsDao: StatsDao
) {
    // Materials
    fun observeMaterials(): Flow<Map<MaterialType, Int>> =
        materialDao.observeAll().map { list ->
            list.associate { MaterialType.valueOf(it.type) to it.count }
        }

    suspend fun getMaterials(): Map<MaterialType, Int> =
        materialDao.getAll().associate { MaterialType.valueOf(it.type) to it.count }

    suspend fun addMaterial(type: MaterialType, amount: Int) {
        val existing = materialDao.getAll().find { it.type == type.name }
        if (existing != null) {
            materialDao.addAmount(type.name, amount)
        } else {
            materialDao.upsert(MaterialEntity(type.name, amount))
        }
    }

    suspend fun subtractMaterial(type: MaterialType, amount: Int): Boolean {
        return materialDao.subtractAmount(type.name, amount) > 0
    }

    suspend fun resetMaterials() = materialDao.resetAll()

    suspend fun ensureMaterialsExist() {
        for (type in MaterialType.entries) {
            val existing = materialDao.getAll().find { it.type == type.name }
            if (existing == null) materialDao.upsert(MaterialEntity(type.name, 0))
        }
    }

    // Ember upgrades
    fun observeEmberUpgrades(): Flow<Map<String, Int>> =
        emberUpgradeDao.observeAll().map { list ->
            list.associate { it.id to it.level }
        }

    suspend fun getEmberUpgrades(): Map<String, Int> =
        emberUpgradeDao.getAll().associate { it.id to it.level }

    suspend fun setUpgradeLevel(id: String, level: Int) =
        emberUpgradeDao.upsert(EmberUpgradeEntity(id, level))

    // Achievements
    fun observeAchievements(): Flow<List<AchievementState>> =
        achievementDao.observeAll().map { list ->
            list.map { AchievementState(it.id, it.tier, it.progress) }
        }

    suspend fun getAchievements(): List<AchievementState> =
        achievementDao.getAll().map { AchievementState(it.id, it.tier, it.progress) }

    suspend fun saveAchievement(state: AchievementState) =
        achievementDao.upsert(AchievementEntity(state.id, state.tier, state.progress))

    // Lifetime stats
    suspend fun incrementStat(key: String, amount: Long = 1) {
        val existing = statsDao.getAll().find { it.key == key }
        if (existing != null) {
            statsDao.increment(key, amount)
        } else {
            statsDao.upsert(StatsEntity(key, amount))
        }
    }

    suspend fun getStat(key: String): Long =
        statsDao.getAll().find { it.key == key }?.value ?: 0

    suspend fun getAllStats(): Map<String, Long> =
        statsDao.getAll().associate { it.key to it.value }

    suspend fun deleteAllProgress() {
        materialDao.resetAll()
        emberUpgradeDao.deleteAll()
        achievementDao.deleteAll()
        statsDao.deleteAll()
    }
}
