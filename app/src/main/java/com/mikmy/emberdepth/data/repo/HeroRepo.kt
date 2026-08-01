package com.mikmy.emberdepth.data.repo

import com.mikmy.emberdepth.core.content.HeroRegistry
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.data.db.HeroDao
import com.mikmy.emberdepth.data.db.HeroEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroRepo @Inject constructor(private val dao: HeroDao) {

    fun observeAll(): Flow<List<HeroState>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAll(): List<HeroState> = dao.getAll().map { it.toDomain() }

    suspend fun getById(id: String): HeroState? = dao.getById(id)?.toDomain()

    suspend fun save(state: HeroState) = dao.upsert(state.toEntity())

    suspend fun levelUp(id: String, newLevel: Int) = dao.updateLevel(id, newLevel)

    suspend fun setSlot(id: String, slot: Int?) = dao.updateSlot(id, slot)

    suspend fun resetAllLevels() = dao.resetAllLevels()

    suspend fun ensureExists() {
        val existing = dao.getAll()
        if (existing.isEmpty()) {
            val starter = HeroRegistry.starterHero()
            val entities = HeroRegistry.ALL.map { def ->
                HeroEntity(
                    id = def.id,
                    unlocked = def.id == starter.id,
                    level = 1,
                    slot = if (def.id == starter.id) 0 else null
                )
            }
            dao.upsertAll(entities)
        }
    }

    suspend fun unlockHero(id: String) {
        val existing = dao.getById(id) ?: return
        dao.upsert(existing.copy(unlocked = true))
    }

    private fun HeroEntity.toDomain() = HeroState(
        id = id,
        unlocked = unlocked,
        level = level,
        formationSlot = slot,
        weaponId = weaponId,
        armorId = armorId,
        accessoryId = accessoryId
    )

    private fun HeroState.toEntity() = HeroEntity(
        id = id,
        unlocked = unlocked,
        level = level,
        slot = formationSlot,
        weaponId = weaponId,
        armorId = armorId,
        accessoryId = accessoryId
    )
}
