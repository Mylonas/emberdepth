package com.mikmy.emberdepth

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mikmy.emberdepth.core.content.HeroRegistry
import com.mikmy.emberdepth.core.model.TutorialFlag
import com.mikmy.emberdepth.monetize.AdManager
import com.mikmy.emberdepth.monetize.BillingManager
import com.mikmy.emberdepth.render.BattleView
import com.mikmy.emberdepth.ui.GameViewModel
import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.ui.components.AchievementPanel
import com.mikmy.emberdepth.ui.components.BossRewardPopup
import com.mikmy.emberdepth.ui.components.DailyRewardPopup
import com.mikmy.emberdepth.ui.components.EmberUpgradePanel
import com.mikmy.emberdepth.ui.components.ForgePanel
import com.mikmy.emberdepth.ui.components.GearInventoryPanel
import com.mikmy.emberdepth.ui.components.HeroTray
import com.mikmy.emberdepth.ui.components.LevelUpPanel
import com.mikmy.emberdepth.ui.components.MaterialBar
import com.mikmy.emberdepth.ui.components.OfflinePopup
import com.mikmy.emberdepth.ui.components.RebirthPanel
import com.mikmy.emberdepth.ui.components.SettingsButton
import com.mikmy.emberdepth.ui.components.SkillBar
import com.mikmy.emberdepth.ui.components.SkillState
import com.mikmy.emberdepth.ui.components.SpeedToggle
import com.mikmy.emberdepth.ui.components.StatsPanel
import com.mikmy.emberdepth.ui.components.TutorialTooltip
import com.mikmy.emberdepth.ui.theme.EmberColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private var battleView: BattleView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        BillingManager.init(this)
        AdManager.loadAd(this)

        viewModel.sfx.start()
        viewModel.initIfNeeded()

        val activity = this

        setContent {
            val player by viewModel.player.collectAsState()
            val heroes by viewModel.heroes.collectAsState()
            val materials by viewModel.materials.collectAsState()
            val selectedHeroId by viewModel.selectedHeroId.collectAsState()
            val offlineResult by viewModel.offlineResult.collectAsState()
            val gearList by viewModel.gear.collectAsState()
            val emberUpgrades by viewModel.emberUpgrades.collectAsState()
            val achievements by viewModel.achievements.collectAsState()
            val showForge by viewModel.showForge.collectAsState()
            val showInventory by viewModel.showInventory.collectAsState()
            val showRebirthConfirm by viewModel.showRebirthConfirm.collectAsState()
            val showEmberUpgrades by viewModel.showEmberUpgrades.collectAsState()
            val showAchievements by viewModel.showAchievements.collectAsState()
            val showStats by viewModel.showStats.collectAsState()
            val lifetimeStats by viewModel.lifetimeStats.collectAsState()
            val dailyReward by viewModel.dailyReward.collectAsState()
            val bossRewardFloor by viewModel.bossRewardFloor.collectAsState()
            val lastForged by viewModel.lastForgedGear.collectAsState()
            val adReady by AdManager.adReady.collectAsState()
            val isAdFree by BillingManager.isAdFree.collectAsState()
            val removeAdsPrice by BillingManager.removeAdsPrice.collectAsState()
            val battleSpeed by viewModel.battleSpeed.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        BattleView(
                            context = ctx,
                            engine = viewModel.engine,
                            renderer = viewModel.renderer,
                            goldProvider = { viewModel.player.value.gold },
                            emberProvider = { viewModel.player.value.ember },
                            onEvents = { events -> viewModel.handleBattleEvents(events) }
                        ).also {
                            battleView = it
                            it.onEnemyTapped = { index ->
                                viewModel.renderer.playerTarget = index
                            }
                        }
                    },
                    update = { view ->
                        view.speedMultiplier = battleSpeed
                        viewModel.renderer.playerTarget = viewModel.engine.playerTarget
                    },
                    modifier = Modifier.fillMaxSize()
                )

                MaterialBar(
                    materials = materials,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp)
                )

                SettingsButton(
                    isMuted = viewModel.sfx.muted,
                    highestFloor = player.highestFloor,
                    onToggleMute = {
                        viewModel.toggleMute()
                        viewModel.sfx.play("ui_tap")
                    },
                    onRebirth = {
                        viewModel.toggleRebirthConfirm()
                        viewModel.sfx.play("ui_tap")
                    },
                    onStats = {
                        viewModel.toggleStats()
                        viewModel.sfx.play("ui_tap")
                    },
                    onResetProgress = {
                        viewModel.resetAllProgress()
                    },
                    onRemoveAds = {
                        BillingManager.launchPurchase(activity)
                    },
                    isAdFree = isAdFree,
                    removeAdsPrice = removeAdsPrice,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 56.dp)
                )

                ActionButtons(
                    onForge = {
                        viewModel.toggleForge()
                        viewModel.sfx.play("ui_tap")
                    },
                    onInventory = {
                        viewModel.toggleInventory()
                        viewModel.sfx.play("ui_tap")
                    },
                    onEmberUpgrades = if (player.rebirthCount > 0) {
                        {
                            viewModel.toggleEmberUpgrades()
                            viewModel.sfx.play("ui_tap")
                        }
                    } else null,
                    onAchievements = {
                        viewModel.toggleAchievements()
                        viewModel.sfx.play("ui_tap")
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 56.dp)
                )

                SpeedToggle(
                    speed = battleSpeed,
                    onClick = {
                        viewModel.cycleSpeed()
                        viewModel.sfx.play("ui_tap")
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 50.dp, top = 56.dp)
                )

                val activeHeroes = heroes.filter { it.unlocked && it.formationSlot != null }

                var skillTick by remember { mutableLongStateOf(0L) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(200)
                        skillTick++
                    }
                }

                if (activeHeroes.size > 1) {
                    @Suppress("UNUSED_EXPRESSION") skillTick
                    val skillStates = viewModel.engine.heroes.map { hero ->
                        SkillState(
                            slot = hero.slot,
                            skill = hero.skill,
                            cooldownFrac = (hero.skillCooldown / hero.skill.cooldownSec).coerceIn(0f, 1f),
                            ready = hero.skillReady,
                            alive = hero.alive
                        )
                    }
                    SkillBar(
                        skills = skillStates,
                        onSkillTap = { slot ->
                            synchronized(battleView?.lock ?: return@SkillBar) {
                                viewModel.engine.useSkill(slot)
                            }
                            viewModel.useSkill(slot)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 76.dp)
                    )
                }

                if (activeHeroes.isNotEmpty()) {
                    HeroTray(
                        heroes = activeHeroes,
                        heroDefs = HeroRegistry.ALL,
                        onHeroTap = { heroId ->
                            viewModel.selectHero(heroId)
                            viewModel.sfx.play("ui_tap")
                        },
                        onSwap = if (activeHeroes.size > 1) { a, b ->
                            viewModel.swapFormationSlots(a, b)
                        } else null,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                // Tutorial tooltips
                if (!player.hasTutorialFlag(TutorialFlag.LEVELED_HERO) && activeHeroes.isNotEmpty()) {
                    TutorialTooltip(
                        text = "Tap a hero to level up!",
                        onDismiss = {},
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    )
                }
                if (!player.hasTutorialFlag(TutorialFlag.OPENED_FORGE) && player.currentFloor >= 10) {
                    TutorialTooltip(
                        text = "Open the Forge to craft gear!",
                        onDismiss = {},
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 48.dp, top = 60.dp)
                    )
                }
                if (!player.hasTutorialFlag(TutorialFlag.FIRST_REBIRTH) && player.highestFloor >= 45) {
                    TutorialTooltip(
                        text = "Rebirth at floor 50 for Ember!",
                        onDismiss = {},
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 48.dp, top = 60.dp)
                    )
                }

                val heroId = selectedHeroId
                if (heroId != null) {
                    val def = HeroRegistry.byId(heroId)
                    val state = heroes.find { it.id == heroId }
                    if (def != null && state != null) {
                        LevelUpPanel(
                            def = def,
                            state = state,
                            gold = player.gold,
                            allGear = gearList,
                            onLevelUp = {
                                viewModel.levelUpHero(heroId)
                                viewModel.sfx.play("ui_tap")
                            },
                            onEquip = { gearId ->
                                viewModel.equipGear(heroId, gearId)
                            },
                            onUnequip = { slot ->
                                viewModel.unequipGear(heroId, slot)
                            },
                            onDismiss = { viewModel.selectHero(null) }
                        )
                    }
                }

                if (showForge) {
                    ForgePanel(
                        gold = player.gold,
                        currentFloor = player.currentFloor,
                        materials = materials,
                        gearCount = gearList.size,
                        maxGear = Tuning.MAX_GEAR_INVENTORY,
                        lastForged = lastForged,
                        onForge = { viewModel.forgeGear() },
                        onEquipForged = lastForged?.let { forged ->
                            val heroId = heroes.firstOrNull { it.unlocked && it.formationSlot != null }?.id
                                ?: return@let null
                            ({ viewModel.equipGear(heroId, forged.id) })
                        },
                        onDismiss = { viewModel.dismissForge() }
                    )
                }

                if (showInventory) {
                    val heroNames = HeroRegistry.ALL.associate { it.id to it.name }
                    GearInventoryPanel(
                        gear = gearList,
                        heroes = heroes,
                        heroNames = heroNames,
                        onSalvage = { viewModel.salvageGear(it) },
                        onDismiss = { viewModel.dismissInventory() }
                    )
                }

                if (showRebirthConfirm) {
                    RebirthPanel(
                        highestFloor = player.highestFloor,
                        currentEmber = player.ember,
                        rebirthCount = player.rebirthCount,
                        rebirthTier = player.rebirthTier,
                        onRebirth = { viewModel.performRebirth() },
                        onDismiss = { viewModel.dismissRebirthConfirm() }
                    )
                }

                if (showEmberUpgrades) {
                    EmberUpgradePanel(
                        ember = player.ember,
                        rebirthTier = player.rebirthTier,
                        upgrades = emberUpgrades,
                        onPurchase = { viewModel.purchaseEmberUpgrade(it) },
                        onDismiss = { viewModel.dismissEmberUpgrades() }
                    )
                }

                if (showAchievements) {
                    AchievementPanel(
                        achievements = achievements,
                        onDismiss = { viewModel.dismissAchievements() }
                    )
                }

                if (showStats) {
                    StatsPanel(
                        stats = lifetimeStats,
                        totalPlayTimeMs = player.totalPlayTimeMs,
                        onDismiss = { viewModel.dismissStats() }
                    )
                }

                val bossFloor = bossRewardFloor
                if (bossFloor != null) {
                    BossRewardPopup(
                        floor = bossFloor,
                        adFree = isAdFree,
                        adReady = adReady,
                        onWatchAd = {
                            AdManager.showAd(activity,
                                onReward = { viewModel.collectBossReward() },
                                onDismiss = { viewModel.dismissBossReward() }
                            )
                        },
                        onSkip = { viewModel.dismissBossReward() }
                    )
                }

                val daily = dailyReward
                if (daily != null) {
                    DailyRewardPopup(
                        reward = daily,
                        adFree = isAdFree,
                        adReady = adReady,
                        onCollect = { viewModel.collectDailyReward() },
                        onCollectBonus = {
                            AdManager.showAd(activity,
                                onReward = { viewModel.collectDailyBonus() }
                            )
                        }
                    )
                }

                val offline = offlineResult
                if (offline != null) {
                    OfflinePopup(
                        result = offline,
                        adFree = isAdFree,
                        adReady = adReady,
                        onCollect = {
                            viewModel.dismissOfflineResult()
                            viewModel.sfx.play("gold", 0.6f)
                        },
                        onCollectDouble = {
                            AdManager.showAd(activity,
                                onReward = { viewModel.collectOfflineDouble() }
                            )
                        }
                    )
                }
            }
        }

        goFullscreen()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goFullscreen()
    }

    override fun onResume() {
        super.onResume()
        battleView?.onResume()
    }

    override fun onPause() {
        battleView?.onPause()
        viewModel.saveOnPause()
        super.onPause()
    }

    override fun onDestroy() {
        viewModel.sfx.stop()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun goFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
        }
    }
}

@Composable
private fun ActionButtons(
    onForge: () -> Unit,
    onInventory: () -> Unit,
    onEmberUpgrades: (() -> Unit)? = null,
    onAchievements: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable { onForge() },
            shape = CircleShape,
            color = EmberColors.surface.copy(alpha = 0.8f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "⚒", color = EmberColors.ember, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable { onInventory() },
            shape = CircleShape,
            color = EmberColors.surface.copy(alpha = 0.8f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "🎒", color = EmberColors.textSecondary, fontSize = 16.sp)
            }
        }
        if (onEmberUpgrades != null) {
            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onEmberUpgrades() },
                shape = CircleShape,
                color = EmberColors.surface.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🔥", color = EmberColors.ember, fontSize = 16.sp)
                }
            }
        }
        if (onAchievements != null) {
            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onAchievements() },
                shape = CircleShape,
                color = EmberColors.surface.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🏆", color = EmberColors.gold, fontSize = 16.sp)
                }
            }
        }
    }
}
