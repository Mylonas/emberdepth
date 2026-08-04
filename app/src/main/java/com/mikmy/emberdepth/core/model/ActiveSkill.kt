package com.mikmy.emberdepth.core.model

data class ActiveSkill(
    val id: String,
    val name: String,
    val icon: String,
    val cooldownSec: Float,
    val description: String
)

object Skills {
    val SHIELD_WALL = ActiveSkill("shield_wall", "Shield Wall", "🛡", 12f, "Block 80% damage for 4s")
    val FLAME_BURST = ActiveSkill("flame_burst", "Flame Burst", "💥", 8f, "Deal 3x ATK to all enemies")
    val GALE_STRIKE = ActiveSkill("gale_strike", "Gale Strike", "⚡", 6f, "Deal 5x ATK to target")
    val SHADOW_FANG = ActiveSkill("shadow_fang", "Shadow Fang", "🗡", 7f, "Deal 4x ATK, ignore DEF")
    val HEALING_WAVE = ActiveSkill("healing_wave", "Healing Wave", "💚", 10f, "Heal all allies for 40% HP")
    val RADIANT_BLESSING = ActiveSkill("radiant_blessing", "Radiant Blessing", "✨", 10f, "Heal all + boost ATK 3s")

    fun forRole(role: Role, element: Element): ActiveSkill = when (role) {
        Role.TANK -> SHIELD_WALL
        Role.DPS -> when (element) {
            Element.FIRE -> FLAME_BURST
            Element.AIR -> GALE_STRIKE
            Element.DARK -> SHADOW_FANG
            else -> FLAME_BURST
        }
        Role.SUPPORT -> when (element) {
            Element.WATER -> HEALING_WAVE
            Element.LIGHT -> RADIANT_BLESSING
            else -> HEALING_WAVE
        }
    }
}
