package com.mikmy.emberdepth.data.repo

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.PlayerState
import com.mikmy.emberdepth.data.db.Converters
import com.mikmy.emberdepth.data.db.PlayerDao
import com.mikmy.emberdepth.data.db.PlayerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepo @Inject constructor(private val dao: PlayerDao) {

    fun observe(): Flow<PlayerState> = dao.observe().map { it?.toDomain() ?: PlayerState() }

    suspend fun get(): PlayerState = dao.get()?.toDomain() ?: PlayerState()

    suspend fun save(state: PlayerState) = dao.upsert(state.toEntity())

    suspend fun updateGold(gold: BigNum) = dao.updateGold(Converters.bigNumToString(gold))

    suspend fun updateEmber(ember: BigNum) = dao.updateEmber(Converters.bigNumToString(ember))

    suspend fun updateFloor(floor: Int) = dao.updateFloor(floor)

    suspend fun updateLastOnline(time: Long) = dao.updateLastOnline(time)

    suspend fun ensureExists() {
        if (dao.get() == null) dao.upsert(PlayerEntity())
    }

    private fun PlayerEntity.toDomain() = PlayerState(
        gold = Converters.stringToBigNum(gold),
        ember = Converters.stringToBigNum(ember),
        currentFloor = floor,
        highestFloor = highest,
        rebirthCount = rebirths,
        rebirthTier = rebirthTier,
        totalPlayTimeMs = playTime,
        lastOnlineMs = lastOnline,
        tutorialFlags = tutorial,
        dailyLastMs = dailyLast
    )

    private fun PlayerState.toEntity() = PlayerEntity(
        gold = Converters.bigNumToString(gold),
        ember = Converters.bigNumToString(ember),
        floor = currentFloor,
        highest = highestFloor,
        rebirths = rebirthCount,
        rebirthTier = rebirthTier,
        playTime = totalPlayTimeMs,
        lastOnline = lastOnlineMs,
        tutorial = tutorialFlags,
        dailyLast = dailyLastMs
    )
}
