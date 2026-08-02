package com.mikmy.emberdepth.core.model

enum class EnemyType { SLIME, SKULL, SPIKE, ORB, WYRM }

data class Enemy(
    val floor: Int,
    val element: Element,
    val hp: BigNum,
    val maxHp: BigNum,
    val atk: BigNum,
    val def: BigNum,
    val isBoss: Boolean,
    val lootTable: List<LootDrop>,
    val type: EnemyType = EnemyType.SLIME
)

data class LootDrop(
    val materialType: MaterialType,
    val amount: Int,
    val chance: Double
)
