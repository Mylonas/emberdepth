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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mikmy.emberdepth.core.content.HeroRegistry
import com.mikmy.emberdepth.render.BattleView
import com.mikmy.emberdepth.ui.GameViewModel
import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.ui.components.ForgePanel
import com.mikmy.emberdepth.ui.components.GearInventoryPanel
import com.mikmy.emberdepth.ui.components.HeroTray
import com.mikmy.emberdepth.ui.components.LevelUpPanel
import com.mikmy.emberdepth.ui.components.MaterialBar
import com.mikmy.emberdepth.ui.components.OfflinePopup
import com.mikmy.emberdepth.ui.components.SettingsButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private var battleView: BattleView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        goFullscreen()

        viewModel.sfx.start()
        viewModel.initIfNeeded()

        setContent {
            val player by viewModel.player.collectAsState()
            val heroes by viewModel.heroes.collectAsState()
            val materials by viewModel.materials.collectAsState()
            val selectedHeroId by viewModel.selectedHeroId.collectAsState()
            val offlineResult by viewModel.offlineResult.collectAsState()
            val gearList by viewModel.gear.collectAsState()
            val showForge by viewModel.showForge.collectAsState()
            val showInventory by viewModel.showInventory.collectAsState()
            val lastForged by viewModel.lastForgedGear.collectAsState()

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
                        ).also { battleView = it }
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
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 56.dp)
                )

                val activeHeroes = heroes.filter { it.unlocked && it.formationSlot != null }
                if (activeHeroes.isNotEmpty()) {
                    HeroTray(
                        heroes = activeHeroes,
                        heroDefs = HeroRegistry.ALL,
                        onHeroTap = { heroId ->
                            viewModel.selectHero(heroId)
                            viewModel.sfx.play("ui_tap")
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
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
                            { viewModel.equipGear(heroes.firstOrNull { it.unlocked && it.formationSlot != null }?.id ?: return@let null, forged.id) }
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

                val offline = offlineResult
                if (offline != null) {
                    OfflinePopup(
                        result = offline,
                        onCollect = {
                            viewModel.dismissOfflineResult()
                            viewModel.sfx.play("gold", 0.6f)
                        }
                    )
                }
            }
        }
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
    }
}
