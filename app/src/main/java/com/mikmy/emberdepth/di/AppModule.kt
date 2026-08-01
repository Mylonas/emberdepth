package com.mikmy.emberdepth.di

import com.mikmy.emberdepth.audio.Sfx
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.render.BattleRenderer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBattleEngine(): BattleEngine = BattleEngine()

    @Provides
    @Singleton
    fun provideBattleRenderer(): BattleRenderer = BattleRenderer()

    @Provides
    @Singleton
    fun provideSfx(): Sfx = Sfx()
}
