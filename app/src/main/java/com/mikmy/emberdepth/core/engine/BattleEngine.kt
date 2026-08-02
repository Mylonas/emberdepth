package com.mikmy.emberdepth.core.engine

import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Element
import com.mikmy.emberdepth.core.model.Enemy
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.core.model.Stats
import kotlin.random.Random

class BattleEngine {

    data class BattleHero(
        val def: HeroDef,
        val state: HeroState,
        val stats: Stats,
        var currentHp: BigNum,
        var attackTimer: Float = 0f,
        val slot: Int
    ) {
        val alive get() = currentHp > BigNum.ZERO
        val element get() = def.element
    }

    data class BattleEvent(
        val type: EventType,
        val sourceSlot: Int = -1,
        val targetIndex: Int = -1,
        val damage: BigNum = BigNum.ZERO,
        val goldEarned: BigNum = BigNum.ZERO,
        val floor: Int = 0,
        val isBoss: Boolean = false,
        val materials: List<LootGenerator.MaterialReward> = emptyList(),
        val element: Element? = null
    )

    enum class EventType {
        HERO_ATTACK, ENEMY_ATTACK, ENEMY_KILLED, FLOOR_CLEARED,
        HERO_DIED, PARTY_WIPED
    }

    private val events = mutableListOf<BattleEvent>()
    private val rng = Random(System.nanoTime())

    var heroes = emptyList<BattleHero>()
        private set
    var enemies = mutableListOf<Enemy>()
        private set
    var currentFloor = 1
        private set
    var enemyIndex = 0
        private set

    private var enemyAttackTimer = 0f
    private var goldMultiplier = 1.0
    private var damageMultiplier = 1.0

    fun init(
        heroDefs: List<Pair<HeroDef, HeroState>>,
        gearLookup: (HeroState) -> List<com.mikmy.emberdepth.core.model.Gear>,
        startFloor: Int,
        goldMult: Double = 1.0,
        damageMult: Double = 1.0
    ) {
        currentFloor = startFloor
        goldMultiplier = goldMult
        damageMultiplier = damageMult

        heroes = heroDefs
            .filter { it.second.formationSlot != null }
            .map { (def, state) ->
                val gear = gearLookup(state)
                val stats = state.effectiveStats(def, gear)
                BattleHero(
                    def = def,
                    state = state,
                    stats = stats,
                    currentHp = stats.hp,
                    slot = state.formationSlot!!
                )
            }

        spawnFloor()
    }

    fun consumeEvents(): List<BattleEvent> {
        val copy = events.toList()
        events.clear()
        return copy
    }

    fun update(dt: Float) {
        if (heroes.none { it.alive }) return
        val currentEnemy = enemies.getOrNull(enemyIndex) ?: return

        for (hero in heroes) {
            if (!hero.alive) continue
            hero.attackTimer += dt * hero.stats.spd.toFloat()
            val interval = Tuning.ATTACK_INTERVAL_BASE.toFloat()
            if (hero.attackTimer >= interval) {
                hero.attackTimer -= interval
                attackEnemy(hero, currentEnemy)
            }
        }

        enemyAttackTimer += dt
        val enemyInterval = Tuning.ATTACK_INTERVAL_BASE.toFloat() * 1.2f
        if (enemyAttackTimer >= enemyInterval) {
            enemyAttackTimer -= enemyInterval
            enemyAttacksParty(currentEnemy)
        }
    }

    private fun attackEnemy(hero: BattleHero, enemy: Enemy) {
        val damage = DamageCalc.calculate(
            hero.stats, hero.element,
            Stats(def = enemy.def), enemy.element,
            damageMultiplier
        )
        val newHp = enemy.hp - damage
        val updatedEnemy = enemy.copy(hp = if (newHp < BigNum.ZERO) BigNum.ZERO else newHp)
        enemies[enemyIndex] = updatedEnemy

        events.add(BattleEvent(
            type = EventType.HERO_ATTACK,
            sourceSlot = hero.slot,
            targetIndex = enemyIndex,
            damage = damage
        ))

        if (updatedEnemy.hp <= BigNum.ZERO) {
            onEnemyKilled(updatedEnemy)
        }
    }

    private fun onEnemyKilled(enemy: Enemy) {
        val gold = LootGenerator.rollGoldReward(currentFloor, enemy.isBoss, goldMultiplier)
        val materials = LootGenerator.rollMaterials(enemy.lootTable, rng)

        events.add(BattleEvent(
            type = EventType.ENEMY_KILLED,
            goldEarned = gold,
            materials = materials,
            isBoss = enemy.isBoss,
            floor = currentFloor,
            element = enemy.element
        ))

        enemyIndex++
        if (enemyIndex >= enemies.size) {
            events.add(BattleEvent(
                type = EventType.FLOOR_CLEARED,
                floor = currentFloor,
                isBoss = currentFloor % Tuning.BOSS_INTERVAL == 0
            ))
            currentFloor++
            spawnFloor()
        }
    }

    private fun enemyAttacksParty(enemy: Enemy) {
        val target = pickTarget() ?: return
        val damage = DamageCalc.calculate(
            Stats(atk = enemy.atk), enemy.element,
            target.stats, target.element
        )
        target.currentHp = target.currentHp - damage
        if (target.currentHp < BigNum.ZERO) target.currentHp = BigNum.ZERO

        events.add(BattleEvent(
            type = EventType.ENEMY_ATTACK,
            targetIndex = target.slot,
            damage = damage
        ))

        if (!target.alive) {
            events.add(BattleEvent(type = EventType.HERO_DIED, sourceSlot = target.slot))
            if (heroes.none { it.alive }) {
                events.add(BattleEvent(type = EventType.PARTY_WIPED, floor = currentFloor))
            }
        }
    }

    private fun pickTarget(): BattleHero? {
        val frontRow = heroes.filter { it.alive && it.slot < 2 }
        if (frontRow.isNotEmpty()) return frontRow[rng.nextInt(frontRow.size)]
        val alive = heroes.filter { it.alive }
        return if (alive.isNotEmpty()) alive[rng.nextInt(alive.size)] else null
    }

    private fun spawnFloor() {
        enemyIndex = 0
        enemyAttackTimer = 0f
        val count = if (currentFloor % Tuning.BOSS_INTERVAL == 0) 1 else Tuning.ENEMIES_PER_FLOOR
        enemies = (0 until count).map { i ->
            if (count == 1) {
                EnemyFactory.create(currentFloor, rng.nextLong())
            } else {
                EnemyFactory.create(currentFloor, rng.nextLong()).copy(isBoss = false)
            }
        }.toMutableList()

        for (hero in heroes) {
            if (!hero.alive) {
                hero.currentHp = hero.stats.hp * 0.3
            }
        }
    }

    fun healParty(fraction: Double = 1.0) {
        for (hero in heroes) {
            hero.currentHp = hero.stats.hp * fraction
        }
    }
}
