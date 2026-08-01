package com.mikmy.emberdepth.core.model

data class Enemy(
    val floor: Int,
    val element: Element,
    val hp: BigNum,
    val maxHp: BigNum,
    val atk: BigNum,
    val def: BigNum,
    val isBoss: Boolean,
    val lootTable: List<LootDrop>
)

data class LootDrop(
    val materialType: MaterialType,
    val amount: Int,
    val chance: Double
)
