package com.mikmy.emberdepth.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerEntity::class,
        HeroEntity::class,
        GearEntity::class,
        MaterialEntity::class,
        EmberUpgradeEntity::class,
        AchievementEntity::class,
        StatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun heroDao(): HeroDao
    abstract fun gearDao(): GearDao
    abstract fun materialDao(): MaterialDao
    abstract fun emberUpgradeDao(): EmberUpgradeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun statsDao(): StatsDao
}
