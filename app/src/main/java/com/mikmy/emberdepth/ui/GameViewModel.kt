package com.mikmy.emberdepth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikmy.emberdepth.audio.Sfx
import com.mikmy.emberdepth.core.content.HeroRegistry
import com.mikmy.emberdepth.core.economy.GoldEconomy
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.core.engine.LootGenerator
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.core.model.PlayerState
import com.mikmy.emberdepth.data.repo.GearRepo
import com.mikmy.emberdepth.data.repo.HeroRepo
import com.mikmy.emberdepth.data.repo.PlayerRepo
import com.mikmy.emberdepth.data.repo.ProgressRepo
import com.mikmy.emberdepth.render.BattleRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val playerRepo: PlayerRepo,
    private val heroRepo: HeroRepo,
    private val gearRepo: GearRepo,
    private val progressRepo: ProgressRepo,
    val engine: BattleEngine,
    val renderer: BattleRenderer,
    val sfx: Sfx
) : ViewModel() {

    private val _player = MutableStateFlow(PlayerState())
    val player: StateFlow<PlayerState> = _player

    private val _heroes = MutableStateFlow<List<HeroState>>(emptyList())
    val heroes: StateFlow<List<HeroState>> = _heroes

    private val _materials = MutableStateFlow<Map<MaterialType, Int>>(emptyMap())
    val materials: StateFlow<Map<MaterialType, Int>> = _materials

    private var initialized = false
    private var allGear = emptyList<Gear>()

    fun initIfNeeded() {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            playerRepo.ensureExists()
            heroRepo.ensureExists()
            progressRepo.ensureMaterialsExist()

            _player.value = playerRepo.get()
            _heroes.value = heroRepo.getAll()
            _materials.value = progressRepo.getMaterials()
            allGear = gearRepo.getAll()

            startBattle()
        }

        viewModelScope.launch {
            playerRepo.observe().collect { _player.value = it }
        }
        viewModelScope.launch {
            heroRepo.observeAll().collect { _heroes.value = it }
        }
        viewModelScope.launch {
            progressRepo.observeMaterials().collect { _materials.value = it }
        }
    }

    private fun startBattle() {
        val heroStates = _heroes.value.filter { it.unlocked && it.formationSlot != null }
        val heroPairs = heroStates.mapNotNull { state ->
            val def = HeroRegistry.byId(state.id) ?: return@mapNotNull null
            def to state
        }

        if (heroPairs.isEmpty()) return

        engine.init(
            heroDefs = heroPairs,
            gearLookup = { state -> gearForHero(state) },
            startFloor = _player.value.currentFloor
        )
    }

    private fun gearForHero(state: HeroState): List<Gear> {
        return listOfNotNull(
            state.weaponId?.let { id -> allGear.find { it.id == id } },
            state.armorId?.let { id -> allGear.find { it.id == id } },
            state.accessoryId?.let { id -> allGear.find { it.id == id } }
        )
    }

    fun handleBattleEvents(events: List<BattleEngine.BattleEvent>) {
        viewModelScope.launch {
            for (event in events) {
                when (event.type) {
                    BattleEngine.EventType.HERO_ATTACK -> {
                        sfx.play("hit", 0.5f)
                    }
                    BattleEngine.EventType.ENEMY_ATTACK -> {
                        sfx.play("thud", 0.4f)
                    }
                    BattleEngine.EventType.ENEMY_KILLED -> {
                        val newGold = _player.value.gold + event.goldEarned
                        _player.value = _player.value.copy(gold = newGold)
                        playerRepo.updateGold(newGold)

                        for (mat in event.materials) {
                            progressRepo.addMaterial(mat.type, mat.amount)
                        }

                        sfx.play(if (event.isBoss) "boss_kill" else "kill", 0.7f)
                        progressRepo.incrementStat("enemies_killed")
                        if (event.isBoss) progressRepo.incrementStat("bosses_killed")
                    }
                    BattleEngine.EventType.FLOOR_CLEARED -> {
                        val newFloor = engine.currentFloor
                        _player.value = _player.value.copy(
                            currentFloor = newFloor,
                            highestFloor = maxOf(_player.value.highestFloor, newFloor)
                        )
                        playerRepo.updateFloor(newFloor)
                        sfx.play("floor", 0.6f)
                        renderer.onFloorCleared(event.floor)
                        progressRepo.incrementStat("floors_cleared")

                        checkHeroUnlocks(newFloor)
                    }
                    BattleEngine.EventType.HERO_DIED -> {
                        sfx.play("hero_died", 0.7f)
                    }
                    BattleEngine.EventType.PARTY_WIPED -> {
                        sfx.play("wipe", 0.8f)
                        engine.healParty(0.5)
                    }
                }
            }
        }
    }

    fun levelUpHero(heroId: String) {
        viewModelScope.launch {
            val state = heroRepo.getById(heroId) ?: return@launch
            val cost = GoldEconomy.heroLevelCost(state.level)
            if (_player.value.gold < cost) return@launch

            val newGold = _player.value.gold - cost
            _player.value = _player.value.copy(gold = newGold)
            playerRepo.updateGold(newGold)
            heroRepo.levelUp(heroId, state.level + 1)
            sfx.play("levelup", 0.6f)
        }
    }

    private suspend fun checkHeroUnlocks(floor: Int) {
        for (def in HeroRegistry.ALL) {
            if (def.unlockFloor <= floor) {
                val state = heroRepo.getById(def.id) ?: continue
                if (!state.unlocked) {
                    heroRepo.unlockHero(def.id)
                    sfx.play("levelup", 0.8f)
                }
            }
        }
    }

    fun saveOnPause() {
        viewModelScope.launch {
            playerRepo.save(_player.value.copy(lastOnlineMs = System.currentTimeMillis()))
        }
    }

    override fun onCleared() {
        sfx.stop()
        super.onCleared()
    }
}
