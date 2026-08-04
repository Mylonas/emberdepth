package com.mikmy.emberdepth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikmy.emberdepth.audio.Sfx
import com.mikmy.emberdepth.core.content.AchievementDefs
import com.mikmy.emberdepth.core.content.DailyReward
import com.mikmy.emberdepth.core.content.DailyRewards
import com.mikmy.emberdepth.core.content.EmberUpgradeDefs
import com.mikmy.emberdepth.core.content.HeroRegistry
import com.mikmy.emberdepth.core.economy.EmberEconomy
import com.mikmy.emberdepth.core.economy.GoldEconomy
import com.mikmy.emberdepth.core.engine.AchievementChecker
import com.mikmy.emberdepth.core.engine.BattleEngine
import com.mikmy.emberdepth.core.engine.LootGenerator
import com.mikmy.emberdepth.core.engine.Tuning
import com.mikmy.emberdepth.core.engine.OfflineResult
import com.mikmy.emberdepth.core.engine.OfflineSimulator
import com.mikmy.emberdepth.core.economy.ForgeRecipes
import com.mikmy.emberdepth.core.model.AchievementState
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.GearSlot
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.core.model.PlayerState
import com.mikmy.emberdepth.core.model.Rarity
import com.mikmy.emberdepth.core.model.StatType
import com.mikmy.emberdepth.core.model.TutorialFlag
import com.mikmy.emberdepth.data.repo.GearRepo
import com.mikmy.emberdepth.data.repo.HeroRepo
import com.mikmy.emberdepth.data.repo.PlayerRepo
import com.mikmy.emberdepth.data.repo.ProgressRepo
import com.mikmy.emberdepth.render.BattleRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
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

    private val _emberUpgrades = MutableStateFlow<Map<String, Int>>(emptyMap())
    val emberUpgrades: StateFlow<Map<String, Int>> = _emberUpgrades

    private val _achievements = MutableStateFlow<List<AchievementState>>(emptyList())
    val achievements: StateFlow<List<AchievementState>> = _achievements

    private val _offlineResult = MutableStateFlow<OfflineResult?>(null)
    val offlineResult: StateFlow<OfflineResult?> = _offlineResult

    private val _selectedHeroId = MutableStateFlow<String?>(null)
    val selectedHeroId: StateFlow<String?> = _selectedHeroId

    private val _gear = MutableStateFlow<List<Gear>>(emptyList())
    val gear: StateFlow<List<Gear>> = _gear

    private val _lastForgedGear = MutableStateFlow<Gear?>(null)
    val lastForgedGear: StateFlow<Gear?> = _lastForgedGear

    private val _showForge = MutableStateFlow(false)
    val showForge: StateFlow<Boolean> = _showForge

    private val _showInventory = MutableStateFlow(false)
    val showInventory: StateFlow<Boolean> = _showInventory

    private val _showRebirthConfirm = MutableStateFlow(false)
    val showRebirthConfirm: StateFlow<Boolean> = _showRebirthConfirm

    private val _showEmberUpgrades = MutableStateFlow(false)
    val showEmberUpgrades: StateFlow<Boolean> = _showEmberUpgrades

    private val _showAchievements = MutableStateFlow(false)
    val showAchievements: StateFlow<Boolean> = _showAchievements

    private val _showStats = MutableStateFlow(false)
    val showStats: StateFlow<Boolean> = _showStats

    private val _lifetimeStats = MutableStateFlow<Map<String, Long>>(emptyMap())
    val lifetimeStats: StateFlow<Map<String, Long>> = _lifetimeStats

    private val _dailyReward = MutableStateFlow<DailyReward?>(null)
    val dailyReward: StateFlow<DailyReward?> = _dailyReward

    private val _bossRewardFloor = MutableStateFlow<Int?>(null)
    val bossRewardFloor: StateFlow<Int?> = _bossRewardFloor

    private val _battleSpeed = MutableStateFlow(1f)
    val battleSpeed: StateFlow<Float> = _battleSpeed

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
            _emberUpgrades.value = progressRepo.getEmberUpgrades()
            _achievements.value = progressRepo.getAchievements()
            allGear = gearRepo.getAll()

            checkDailyReward()
            processOfflineEarnings()
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
        viewModelScope.launch {
            gearRepo.observeAll().collect {
                _gear.value = it
                allGear = it
            }
        }
        viewModelScope.launch {
            progressRepo.observeEmberUpgrades().collect { _emberUpgrades.value = it }
        }
        viewModelScope.launch {
            progressRepo.observeAchievements().collect { _achievements.value = it }
        }
    }

    private fun startBattle() {
        val heroStates = _heroes.value.filter { it.unlocked && it.formationSlot != null }
        val heroPairs = heroStates.mapNotNull { state ->
            val def = HeroRegistry.byId(state.id) ?: return@mapNotNull null
            def to state
        }

        if (heroPairs.isEmpty()) return

        val upgrades = _emberUpgrades.value
        val defs = EmberUpgradeDefs.ALL
        val achBonuses = AchievementChecker.totalBonus(AchievementDefs.ALL, _achievements.value)

        val baseDmgMult = EmberEconomy.totalDamageMultiplier(upgrades, defs)
        val baseHpMult = EmberEconomy.totalHpMultiplier(upgrades, defs)

        engine.init(
            heroDefs = heroPairs,
            gearLookup = { state -> gearForHero(state) },
            startFloor = _player.value.currentFloor,
            goldMult = EmberEconomy.totalGoldMultiplier(upgrades, defs),
            damageMult = baseDmgMult * (1.0 + (achBonuses[StatType.ATK] ?: 0.0)),
            hpMult = baseHpMult * (1.0 + (achBonuses[StatType.HP] ?: 0.0))
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
        for (event in events) {
            when (event.type) {
                BattleEngine.EventType.HERO_ATTACK -> {
                    sfx.play("hit", 0.5f)
                    val (tx, ty) = renderer.enemyScreenPos(event.targetIndex)
                    renderer.onHeroAttack(event.sourceSlot, event.damage, tx, ty)
                }
                BattleEngine.EventType.ENEMY_ATTACK -> {
                    sfx.play("thud", 0.4f)
                }
                BattleEngine.EventType.ENEMY_KILLED -> {
                    val idx = event.targetIndex.coerceAtLeast(0)
                    val (ex, ey) = renderer.enemyScreenPos(idx)
                    val enemyColor = (event.element?.color ?: 0xFFAAAAAA).toInt()
                    renderer.onEnemyKilled(ex, ey, enemyColor, event.isBoss)
                    renderer.onGoldEarned(event.goldEarned, ex, ey)
                    sfx.play("gold", 0.4f)
                    sfx.play(if (event.isBoss) "boss_kill" else "kill", 0.7f)
                }
                BattleEngine.EventType.FLOOR_CLEARED -> {
                    sfx.play("floor", 0.6f)
                    renderer.onFloorCleared(event.floor)
                    val newFloor = engine.currentFloor
                    if (newFloor % Tuning.BOSS_INTERVAL == 0) {
                        renderer.onBossFloor()
                    }
                }
                BattleEngine.EventType.HERO_DIED -> {
                    sfx.play("hero_died", 0.7f)
                }
                BattleEngine.EventType.PARTY_WIPED -> {
                    sfx.play("wipe", 0.8f)
                    engine.dropFloor()
                    engine.healParty(0.5)
                }
                BattleEngine.EventType.HERO_HEAL -> {
                    sfx.play("levelup", 0.2f)
                    renderer.onHeroHealed(event.targetIndex, event.damage)
                }
                BattleEngine.EventType.SKILL_USED -> {
                    sfx.play("forge", 0.6f)
                    val hero = engine.heroes.find { it.slot == event.sourceSlot }
                    if (hero != null) {
                        val (hx, hy) = renderer.heroScreenPos(hero.slot)
                        renderer.floatingText.push(
                            hx, hy - renderer.unit * 0.06f,
                            hero.skill.name, renderer.colSkill, 0.8f, renderer.unit * 0.03f
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            for (event in events) {
                when (event.type) {
                    BattleEngine.EventType.ENEMY_KILLED -> {
                        val newGold = _player.value.gold + event.goldEarned
                        _player.value = _player.value.copy(gold = newGold)
                        playerRepo.updateGold(newGold)

                        for (mat in event.materials) {
                            progressRepo.addMaterial(mat.type, mat.amount)
                        }

                        progressRepo.incrementStat("enemies_killed")
                        if (event.isBoss) progressRepo.incrementStat("bosses_killed")
                        checkAchievements()
                    }
                    BattleEngine.EventType.FLOOR_CLEARED -> {
                        val newFloor = engine.currentFloor
                        _player.value = _player.value.copy(
                            currentFloor = newFloor,
                            highestFloor = maxOf(_player.value.highestFloor, newFloor)
                        )
                        playerRepo.updateFloor(newFloor)
                        progressRepo.incrementStat("floors_cleared")

                        if (event.isBoss && !_player.value.hasTutorialFlag(TutorialFlag.FIRST_BOSS)) {
                            setTutorialFlag(TutorialFlag.FIRST_BOSS)
                        }

                        if (event.isBoss && event.floor % 100 == 0) {
                            _bossRewardFloor.value = event.floor
                        }

                        checkHeroUnlocks(newFloor)
                        checkAchievements()
                    }
                    BattleEngine.EventType.PARTY_WIPED -> {
                        val newFloor = engine.currentFloor
                        _player.value = _player.value.copy(currentFloor = newFloor)
                        playerRepo.updateFloor(newFloor)
                    }
                    else -> {}
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

            if (!_player.value.hasTutorialFlag(TutorialFlag.LEVELED_HERO)) {
                setTutorialFlag(TutorialFlag.LEVELED_HERO)
            }

            val slot = state.formationSlot
            if (slot != null) {
                val def = HeroRegistry.byId(heroId) ?: return@launch
                val newState = state.copy(level = state.level + 1)
                val gear = gearForHero(newState)
                engine.refreshHeroStats(slot, newState.effectiveStats(def, gear))
            }
        }
    }

    private suspend fun checkHeroUnlocks(floor: Int) {
        for (def in HeroRegistry.ALL) {
            if (def.unlockFloor <= floor) {
                val state = heroRepo.getById(def.id) ?: continue
                if (!state.unlocked) {
                    heroRepo.unlockHero(def.id)
                    assignFormationSlot(def.id)
                    progressRepo.incrementStat("heroes_unlocked")
                    sfx.play("levelup", 0.8f)
                    checkAchievements()
                }
            }
        }
    }

    private suspend fun assignFormationSlot(heroId: String) {
        val occupied = _heroes.value
            .filter { it.unlocked && it.formationSlot != null }
            .mapNotNull { it.formationSlot }
            .toSet()
        val nextSlot = (0 until effectiveFormationSize()).firstOrNull { it !in occupied }
            ?: return
        heroRepo.setSlot(heroId, nextSlot)
    }

    private fun effectiveFormationSize(): Int {
        return Tuning.MAX_FORMATION_SIZE + EmberEconomy.extraHeroSlots(
            _emberUpgrades.value, EmberUpgradeDefs.ALL
        )
    }

    private suspend fun checkAchievements() {
        val stats = progressRepo.getAllStats()
        val result = AchievementChecker.evaluate(
            AchievementDefs.ALL, _achievements.value, stats
        )
        for ((def, newTier) in result.advanced) {
            progressRepo.saveAchievement(
                result.updated.first { it.id == def.id }
            )
            sfx.play("levelup", 0.8f)
            renderer.onAchievementUnlock(def.name, newTier)
        }
        if (result.advanced.isNotEmpty()) {
            for (state in result.updated) {
                if (result.advanced.none { it.first.id == state.id }) continue.let {}
                progressRepo.saveAchievement(state)
            }
        }
        // Always save progress updates even without tier advances
        for (state in result.updated) {
            val existing = _achievements.value.find { it.id == state.id }
            if (existing == null || existing.progress != state.progress) {
                progressRepo.saveAchievement(state)
            }
        }
        _achievements.value = result.updated

        checkTutorialComplete()
    }

    private suspend fun checkTutorialComplete() {
        val flags = _player.value.tutorialFlags
        val allSet = (flags and TutorialFlag.LEVELED_HERO) != 0 &&
            (flags and TutorialFlag.OPENED_FORGE) != 0 &&
            (flags and TutorialFlag.FIRST_BOSS) != 0 &&
            (flags and TutorialFlag.FIRST_REBIRTH) != 0
        if (allSet && !_player.value.hasTutorialFlag(TutorialFlag.TUTORIAL_COMPLETE)) {
            setTutorialFlag(TutorialFlag.TUTORIAL_COMPLETE)
        }
    }

    private suspend fun checkDailyReward() {
        val player = _player.value
        val now = System.currentTimeMillis()
        if (isNewDay(player.dailyLastMs, now)) {
            val dayCount = (progressRepo.getStat("daily_logins") + 1).toInt()
            val reward = DailyRewards.forDay(dayCount)
            _dailyReward.value = reward
        }
    }

    fun collectDailyReward() {
        viewModelScope.launch {
            val reward = _dailyReward.value ?: return@launch
            _dailyReward.value = null

            if (!reward.gold.isZero()) {
                val newGold = _player.value.gold + reward.gold
                _player.value = _player.value.copy(gold = newGold)
                playerRepo.updateGold(newGold)
            }
            for ((type, amount) in reward.materials) {
                progressRepo.addMaterial(type, amount)
            }
            if (!reward.ember.isZero() && _player.value.rebirthCount > 0) {
                val newEmber = _player.value.ember + reward.ember
                _player.value = _player.value.copy(ember = newEmber)
                playerRepo.updateEmber(newEmber)
            }

            progressRepo.incrementStat("daily_logins")
            val updated = _player.value.copy(dailyLastMs = System.currentTimeMillis())
            _player.value = updated
            playerRepo.save(updated)
            sfx.play("gold", 0.6f)
        }
    }

    private fun isNewDay(lastMs: Long, nowMs: Long): Boolean {
        if (lastMs <= 0L) return true
        val cal1 = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = lastMs }
        val cal2 = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = nowMs }
        return cal1.get(Calendar.DAY_OF_YEAR) != cal2.get(Calendar.DAY_OF_YEAR) ||
            cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)
    }

    fun selectHero(id: String?) {
        _selectedHeroId.value = id
    }

    fun dismissOfflineResult() {
        _offlineResult.value = null
    }

    fun collectOfflineDouble() {
        viewModelScope.launch {
            val result = _offlineResult.value ?: return@launch
            _offlineResult.value = null

            val bonusGold = result.goldEarned
            val newGold = _player.value.gold + bonusGold
            _player.value = _player.value.copy(gold = newGold)
            playerRepo.updateGold(newGold)

            for ((type, amount) in result.materialsEarned) {
                progressRepo.addMaterial(type, amount)
            }
            sfx.play("gold", 0.8f)
        }
    }

    fun collectDailyBonus() {
        viewModelScope.launch {
            val reward = _dailyReward.value ?: return@launch
            _dailyReward.value = null

            if (!reward.gold.isZero()) {
                val totalGold = _player.value.gold + reward.gold * 1.5
                _player.value = _player.value.copy(gold = totalGold)
                playerRepo.updateGold(totalGold)
            }
            for ((type, amount) in reward.materials) {
                progressRepo.addMaterial(type, amount + 1)
            }
            if (!reward.ember.isZero() && _player.value.rebirthCount > 0) {
                val newEmber = _player.value.ember + reward.ember
                _player.value = _player.value.copy(ember = newEmber)
                playerRepo.updateEmber(newEmber)
            }

            progressRepo.incrementStat("daily_logins")
            val updated = _player.value.copy(dailyLastMs = System.currentTimeMillis())
            _player.value = updated
            playerRepo.save(updated)
            sfx.play("gold", 0.8f)
        }
    }

    fun collectBossReward() {
        viewModelScope.launch {
            val floor = _bossRewardFloor.value ?: return@launch
            _bossRewardFloor.value = null

            val bonusGold = GoldEconomy.forgeCost(floor) * 2.0
            val newGold = _player.value.gold + bonusGold
            _player.value = _player.value.copy(gold = newGold)
            playerRepo.updateGold(newGold)

            progressRepo.addMaterial(MaterialType.ORE, 3)
            progressRepo.addMaterial(MaterialType.ESSENCE, 1)
            sfx.play("gold", 0.8f)
            renderer.onForgeComplete("Boss Bonus!")
        }
    }

    fun dismissBossReward() {
        _bossRewardFloor.value = null
    }

    fun toggleMute() {
        sfx.muted = !sfx.muted
    }

    fun toggleForge() {
        _showForge.value = !_showForge.value
        if (_showForge.value) {
            _showInventory.value = false
            viewModelScope.launch { setTutorialFlag(TutorialFlag.OPENED_FORGE) }
        }
    }

    fun toggleInventory() {
        _showInventory.value = !_showInventory.value
        if (_showInventory.value) _showForge.value = false
    }

    fun dismissForge() {
        _showForge.value = false
        _lastForgedGear.value = null
    }

    fun dismissInventory() {
        _showInventory.value = false
    }

    fun toggleRebirthConfirm() {
        _showRebirthConfirm.value = !_showRebirthConfirm.value
    }

    fun dismissRebirthConfirm() {
        _showRebirthConfirm.value = false
    }

    fun toggleEmberUpgrades() {
        _showEmberUpgrades.value = !_showEmberUpgrades.value
    }

    fun dismissEmberUpgrades() {
        _showEmberUpgrades.value = false
    }

    fun toggleAchievements() {
        _showAchievements.value = !_showAchievements.value
    }

    fun dismissAchievements() {
        _showAchievements.value = false
    }

    fun toggleStats() {
        _showStats.value = !_showStats.value
        if (_showStats.value) {
            viewModelScope.launch {
                _lifetimeStats.value = progressRepo.getAllStats()
            }
        }
    }

    fun dismissStats() {
        _showStats.value = false
    }

    fun performRebirth() {
        viewModelScope.launch {
            val player = _player.value
            if (player.highestFloor < 50) return@launch

            val emberEarned = EmberEconomy.emberFromRebirth(player.highestFloor)
            val newRebirthCount = player.rebirthCount + 1
            val newTier = EmberEconomy.rebirthTier(newRebirthCount)
            val upgrades = _emberUpgrades.value
            val startFloor = EmberEconomy.startingFloor(upgrades, EmberUpgradeDefs.ALL)

            val updated = player.copy(
                gold = BigNum.ZERO,
                ember = player.ember + emberEarned,
                currentFloor = startFloor,
                highestFloor = startFloor,
                rebirthCount = newRebirthCount,
                rebirthTier = newTier
            )
            _player.value = updated
            playerRepo.save(updated)

            heroRepo.resetAllLevels()
            heroRepo.resetGearAssignments()
            gearRepo.deleteAll()
            progressRepo.resetMaterials()
            progressRepo.incrementStat("rebirths")

            if (!player.hasTutorialFlag(TutorialFlag.FIRST_REBIRTH)) {
                setTutorialFlag(TutorialFlag.FIRST_REBIRTH)
            }

            _showRebirthConfirm.value = false
            sfx.play("rebirth", 0.9f)
            renderer.onRebirth()

            _heroes.value = heroRepo.getAll()
            allGear = emptyList()
            checkAchievements()
            startBattle()
        }
    }

    fun purchaseEmberUpgrade(upgradeId: String) {
        viewModelScope.launch {
            val def = EmberUpgradeDefs.byId(upgradeId) ?: return@launch
            val player = _player.value
            if (player.rebirthTier < def.requiredRebirthTier) return@launch

            val currentLevel = _emberUpgrades.value[upgradeId] ?: 0
            if (!EmberEconomy.canAffordUpgrade(player.ember, def, currentLevel)) return@launch

            val cost = EmberEconomy.upgradeCost(def, currentLevel)
            val newEmber = player.ember - cost
            _player.value = player.copy(ember = newEmber)
            playerRepo.updateEmber(newEmber)
            progressRepo.setUpgradeLevel(upgradeId, currentLevel + 1)
            sfx.play("ui_tap")
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            playerRepo.deleteAll()
            heroRepo.deleteAll()
            gearRepo.deleteAll()
            progressRepo.deleteAllProgress()

            playerRepo.ensureExists()
            heroRepo.ensureExists()
            progressRepo.ensureMaterialsExist()

            _player.value = playerRepo.get()
            _heroes.value = heroRepo.getAll()
            _materials.value = progressRepo.getMaterials()
            _emberUpgrades.value = emptyMap()
            _achievements.value = emptyList()
            allGear = emptyList()

            sfx.play("ui_tap")
            startBattle()
        }
    }

    fun forgeGear() {
        viewModelScope.launch {
            val recipe = ForgeRecipes.basicForge()
            if (!ForgeRecipes.canForge(recipe, _materials.value)) return@launch
            val cost = GoldEconomy.forgeCost(_player.value.currentFloor)
            if (_player.value.gold < cost) return@launch
            if (gearRepo.count() >= Tuning.MAX_GEAR_INVENTORY) return@launch

            val newGold = _player.value.gold - cost
            _player.value = _player.value.copy(gold = newGold)
            playerRepo.updateGold(newGold)

            for ((type, amount) in recipe.materials) {
                progressRepo.subtractMaterial(type, amount)
            }

            val gear = LootGenerator.forgeGear(_player.value.currentFloor)
            val id = gearRepo.insert(gear)
            val savedGear = gear.copy(id = id)

            _lastForgedGear.value = savedGear
            sfx.play("forge", 0.7f)
            progressRepo.incrementStat("gear_forged")
            renderer.onForgeComplete(gear.rarity.label)
            checkAchievements()
        }
    }

    private suspend fun setTutorialFlag(flag: Int) {
        if (_player.value.hasTutorialFlag(flag)) return
        val updated = _player.value.withTutorialFlag(flag)
        _player.value = updated
        playerRepo.save(updated)
    }

    fun equipGear(heroId: String, gearId: Long) {
        viewModelScope.launch {
            val gear = allGear.find { it.id == gearId } ?: return@launch
            val heroState = heroRepo.getById(heroId) ?: return@launch

            val currentGearId = when (gear.slot) {
                GearSlot.WEAPON -> heroState.weaponId
                GearSlot.ARMOR -> heroState.armorId
                GearSlot.ACCESSORY -> heroState.accessoryId
            }
            if (currentGearId == gearId) return@launch

            val otherHero = _heroes.value.find { hero ->
                hero.id != heroId && when (gear.slot) {
                    GearSlot.WEAPON -> hero.weaponId == gearId
                    GearSlot.ARMOR -> hero.armorId == gearId
                    GearSlot.ACCESSORY -> hero.accessoryId == gearId
                }
            }
            if (otherHero != null) {
                heroRepo.unequipGear(otherHero.id, gear.slot)
                refreshEngineStatsForHero(otherHero.id)
            }

            heroRepo.equipGear(heroId, gear.slot, gearId)
            refreshEngineStatsForHero(heroId)
            sfx.play("ui_tap")
        }
    }

    fun unequipGear(heroId: String, slot: GearSlot) {
        viewModelScope.launch {
            heroRepo.unequipGear(heroId, slot)
            refreshEngineStatsForHero(heroId)
            sfx.play("ui_tap")
        }
    }

    fun salvageGear(gearId: Long) {
        viewModelScope.launch {
            val gear = allGear.find { it.id == gearId } ?: return@launch

            val equippedBy = _heroes.value.find { hero ->
                hero.weaponId == gearId || hero.armorId == gearId || hero.accessoryId == gearId
            }
            if (equippedBy != null) {
                heroRepo.unequipGear(equippedBy.id, gear.slot)
                refreshEngineStatsForHero(equippedBy.id)
            }

            gearRepo.delete(gearId)

            val returns = salvageReturns(gear.rarity)
            for ((type, amount) in returns) {
                progressRepo.addMaterial(type, amount)
            }
            sfx.play("ui_tap")
        }
    }

    private fun salvageReturns(rarity: Rarity): Map<MaterialType, Int> = when (rarity) {
        Rarity.COMMON -> mapOf(MaterialType.ORE to 1)
        Rarity.UNCOMMON -> mapOf(MaterialType.ORE to 2)
        Rarity.RARE -> mapOf(MaterialType.ORE to 2, MaterialType.ESSENCE to 1)
        Rarity.EPIC -> mapOf(MaterialType.ESSENCE to 2, MaterialType.FRAGMENT to 1)
        Rarity.LEGENDARY -> mapOf(MaterialType.ESSENCE to 3, MaterialType.CRYSTAL to 1)
    }

    private suspend fun refreshEngineStatsForHero(heroId: String) {
        val state = heroRepo.getById(heroId) ?: return
        val slot = state.formationSlot ?: return
        val def = HeroRegistry.byId(heroId) ?: return
        val gear = gearForHero(state)
        engine.refreshHeroStats(slot, state.effectiveStats(def, gear))
    }

    private suspend fun processOfflineEarnings() {
        val player = _player.value
        if (player.lastOnlineMs <= 0L) return
        val elapsed = System.currentTimeMillis() - player.lastOnlineMs
        if (elapsed < 60_000L) return

        val upgrades = _emberUpgrades.value
        val upgradesDefs = EmberUpgradeDefs.ALL

        val power = partyPower()
        val result = OfflineSimulator.simulate(
            startFloor = player.currentFloor,
            partyPower = power,
            elapsedMs = elapsed,
            offlineEfficiency = EmberEconomy.offlineEfficiency(upgrades, upgradesDefs),
            goldMultiplier = EmberEconomy.totalGoldMultiplier(upgrades, upgradesDefs)
        )
        if (result.floorsCleared <= 0 && result.goldEarned.isZero()) return

        val newGold = player.gold + result.goldEarned
        val newFloor = result.newFloor
        _player.value = player.copy(
            gold = newGold,
            currentFloor = newFloor,
            highestFloor = maxOf(player.highestFloor, newFloor)
        )
        playerRepo.updateGold(newGold)
        playerRepo.updateFloor(newFloor)
        for ((type, amount) in result.materialsEarned) {
            progressRepo.addMaterial(type, amount)
        }
        _offlineResult.value = result
    }

    private fun partyPower(): BigNum {
        val heroStates = _heroes.value.filter { it.unlocked && it.formationSlot != null }
        var power = BigNum.ZERO
        for (state in heroStates) {
            val def = HeroRegistry.byId(state.id) ?: continue
            val gear = gearForHero(state)
            val stats = state.effectiveStats(def, gear)
            power = power + stats.atk * stats.hp
        }
        return power
    }

    fun cycleSpeed() {
        _battleSpeed.value = when (_battleSpeed.value) {
            1f -> 2f
            2f -> 3f
            else -> 1f
        }
    }

    fun useSkill(slot: Int) {
        sfx.play("ui_tap")
    }

    fun swapFormationSlots(heroIdA: String, heroIdB: String) {
        viewModelScope.launch {
            val stateA = heroRepo.getById(heroIdA) ?: return@launch
            val stateB = heroRepo.getById(heroIdB) ?: return@launch
            val slotA = stateA.formationSlot ?: return@launch
            val slotB = stateB.formationSlot ?: return@launch
            heroRepo.setSlot(heroIdA, slotB)
            heroRepo.setSlot(heroIdB, slotA)

            val defA = HeroRegistry.byId(heroIdA)
            val defB = HeroRegistry.byId(heroIdB)
            if (defA != null) {
                val newState = stateA.copy(formationSlot = slotB)
                engine.refreshHeroStats(slotB, newState.effectiveStats(defA, gearForHero(newState)))
            }
            if (defB != null) {
                val newState = stateB.copy(formationSlot = slotA)
                engine.refreshHeroStats(slotA, newState.effectiveStats(defB, gearForHero(newState)))
            }

            startBattle()
            sfx.play("ui_tap")
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
