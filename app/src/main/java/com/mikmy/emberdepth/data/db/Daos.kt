package com.mikmy.emberdepth.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE id = 1")
    suspend fun get(): PlayerEntity?

    @Query("SELECT * FROM player WHERE id = 1")
    fun observe(): Flow<PlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerEntity)

    @Query("UPDATE player SET gold = :gold WHERE id = 1")
    suspend fun updateGold(gold: String)

    @Query("UPDATE player SET ember = :ember WHERE id = 1")
    suspend fun updateEmber(ember: String)

    @Query("UPDATE player SET floor = :floor, highest = CASE WHEN :floor > highest THEN :floor ELSE highest END WHERE id = 1")
    suspend fun updateFloor(floor: Int)

    @Query("UPDATE player SET lastOnline = :time WHERE id = 1")
    suspend fun updateLastOnline(time: Long)

    @Query("DELETE FROM player")
    suspend fun deleteAll()
}

@Dao
interface HeroDao {
    @Query("SELECT * FROM hero")
    suspend fun getAll(): List<HeroEntity>

    @Query("SELECT * FROM hero")
    fun observeAll(): Flow<List<HeroEntity>>

    @Query("SELECT * FROM hero WHERE id = :id")
    suspend fun getById(id: String): HeroEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HeroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HeroEntity>)

    @Query("UPDATE hero SET level = :level WHERE id = :id")
    suspend fun updateLevel(id: String, level: Int)

    @Query("UPDATE hero SET slot = :slot WHERE id = :id")
    suspend fun updateSlot(id: String, slot: Int?)

    @Query("UPDATE hero SET weaponId = :gearId WHERE id = :id")
    suspend fun updateWeapon(id: String, gearId: Long?)

    @Query("UPDATE hero SET armorId = :gearId WHERE id = :id")
    suspend fun updateArmor(id: String, gearId: Long?)

    @Query("UPDATE hero SET accessoryId = :gearId WHERE id = :id")
    suspend fun updateAccessory(id: String, gearId: Long?)

    @Query("UPDATE hero SET level = 1 WHERE unlocked = 1")
    suspend fun resetAllLevels()

    @Query("UPDATE hero SET weaponId = NULL, armorId = NULL, accessoryId = NULL WHERE unlocked = 1")
    suspend fun resetGearAssignments()

    @Query("DELETE FROM hero")
    suspend fun deleteAll()
}

@Dao
interface GearDao {
    @Query("SELECT * FROM gear")
    suspend fun getAll(): List<GearEntity>

    @Query("SELECT * FROM gear WHERE id = :id")
    suspend fun getById(id: Long): GearEntity?

    @Insert
    suspend fun insert(entity: GearEntity): Long

    @Query("SELECT * FROM gear")
    fun observeAll(): Flow<List<GearEntity>>

    @Query("SELECT COUNT(*) FROM gear")
    suspend fun count(): Int

    @Query("DELETE FROM gear WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM gear")
    suspend fun deleteAll()
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM material")
    suspend fun getAll(): List<MaterialEntity>

    @Query("SELECT * FROM material")
    fun observeAll(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MaterialEntity)

    @Query("UPDATE material SET count = count + :amount WHERE type = :type")
    suspend fun addAmount(type: String, amount: Int)

    @Query("UPDATE material SET count = count - :amount WHERE type = :type AND count >= :amount")
    suspend fun subtractAmount(type: String, amount: Int): Int

    @Query("UPDATE material SET count = 0")
    suspend fun resetAll()
}

@Dao
interface EmberUpgradeDao {
    @Query("SELECT * FROM ember_upgrade")
    suspend fun getAll(): List<EmberUpgradeEntity>

    @Query("SELECT * FROM ember_upgrade")
    fun observeAll(): Flow<List<EmberUpgradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EmberUpgradeEntity)

    @Query("DELETE FROM ember_upgrade")
    suspend fun deleteAll()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievement")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementEntity)

    @Query("DELETE FROM achievement")
    suspend fun deleteAll()
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM stats")
    suspend fun getAll(): List<StatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StatsEntity)

    @Query("UPDATE stats SET value = value + :amount WHERE key = :key")
    suspend fun increment(key: String, amount: Long)

    @Query("DELETE FROM stats")
    suspend fun deleteAll()
}
