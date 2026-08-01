package com.mikmy.emberdepth.di

import android.content.Context
import androidx.room.Room
import com.mikmy.emberdepth.data.db.AchievementDao
import com.mikmy.emberdepth.data.db.AppDatabase
import com.mikmy.emberdepth.data.db.EmberUpgradeDao
import com.mikmy.emberdepth.data.db.GearDao
import com.mikmy.emberdepth.data.db.HeroDao
import com.mikmy.emberdepth.data.db.MaterialDao
import com.mikmy.emberdepth.data.db.PlayerDao
import com.mikmy.emberdepth.data.db.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "emberdepth.db")
            .build()

    @Provides fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()
    @Provides fun provideHeroDao(db: AppDatabase): HeroDao = db.heroDao()
    @Provides fun provideGearDao(db: AppDatabase): GearDao = db.gearDao()
    @Provides fun provideMaterialDao(db: AppDatabase): MaterialDao = db.materialDao()
    @Provides fun provideEmberUpgradeDao(db: AppDatabase): EmberUpgradeDao = db.emberUpgradeDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
}
