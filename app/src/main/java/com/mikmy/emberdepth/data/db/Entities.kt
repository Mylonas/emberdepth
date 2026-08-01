package com.mikmy.emberdepth.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Int = 1,
    val gold: String = "0:0",
    val ember: String = "0:0",
    val floor: Int = 1,
    val highest: Int = 1,
    val rebirths: Int = 0,
    val rebirthTier: Int = 0,
    val playTime: Long = 0,
    val lastOnline: Long = System.currentTimeMillis(),
    val tutorial: Int = 0,
    val dailyLast: Long = 0
)

@Entity(tableName = "hero")
data class HeroEntity(
    @PrimaryKey val id: String,
    val unlocked: Boolean = false,
    val level: Int = 1,
    val slot: Int? = null,
    val weaponId: Long? = null,
    val armorId: Long? = null,
    val accessoryId: Long? = null
)

@Entity(tableName = "gear")
data class GearEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotType: String,
    val rarity: String,
    val level: Int,
    val primaryStat: String,
    val secondary: String,
    val element: String? = null
)

@Entity(tableName = "material")
data class MaterialEntity(
    @PrimaryKey val type: String,
    val count: Int = 0
)

@Entity(tableName = "ember_upgrade")
data class EmberUpgradeEntity(
    @PrimaryKey val id: String,
    val level: Int = 0
)

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val tier: Int = 0,
    val progress: Long = 0
)

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey val key: String,
    val value: Long = 0
)
