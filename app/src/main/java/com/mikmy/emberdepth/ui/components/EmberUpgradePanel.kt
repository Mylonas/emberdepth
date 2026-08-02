package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.content.EmberUpgradeDefs
import com.mikmy.emberdepth.core.economy.EmberEconomy
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.EmberUpgradeDef
import com.mikmy.emberdepth.core.model.UpgradeEffect
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun EmberUpgradePanel(
    ember: BigNum,
    rebirthTier: Int,
    upgrades: Map<String, Int>,
    onPurchase: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = EmberColors.bg.copy(alpha = 0.7f)
        ) {}
        EmberPanel(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ember Upgrades",
                        color = EmberColors.ember,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = ember.format(),
                        color = EmberColors.ember,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "✕",
                        color = EmberColors.textSecondary,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Rebirth Tier $rebirthTier",
                    color = EmberColors.gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (def in EmberUpgradeDefs.ALL) {
                        val level = upgrades[def.id] ?: 0
                        val tierLocked = rebirthTier < def.requiredRebirthTier
                        val maxed = level >= def.maxLevel
                        val cost = if (!maxed) EmberEconomy.upgradeCost(def, level) else BigNum.ZERO
                        val canAfford = !maxed && !tierLocked && ember >= cost

                        UpgradeCard(
                            def = def,
                            level = level,
                            cost = cost,
                            tierLocked = tierLocked,
                            maxed = maxed,
                            canAfford = canAfford,
                            onPurchase = { onPurchase(def.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    def: EmberUpgradeDef,
    level: Int,
    cost: BigNum,
    tierLocked: Boolean,
    maxed: Boolean,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    val alpha = if (tierLocked) 0.4f else 1f

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmberColors.surfaceLight.copy(alpha = 0.6f * alpha),
        border = BorderStroke(
            1.dp,
            if (tierLocked) EmberColors.disabled.copy(alpha = 0.2f)
            else EmberColors.ember.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = def.name,
                        color = EmberColors.textPrimary.copy(alpha = alpha),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = def.description,
                        color = EmberColors.textSecondary.copy(alpha = alpha),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = if (maxed) "MAX" else "$level/${def.maxLevel}",
                    color = if (maxed) EmberColors.gold else EmberColors.textPrimary.copy(alpha = alpha),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = effectSummary(def, level),
                    color = EmberColors.ember.copy(alpha = alpha),
                    fontSize = 11.sp
                )
                Spacer(Modifier.weight(1f))

                if (tierLocked) {
                    Text(
                        text = "Tier ${def.requiredRebirthTier}",
                        color = EmberColors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!maxed) {
                    EmberButton(
                        text = cost.format(),
                        onClick = onPurchase,
                        enabled = canAfford,
                        color = EmberColors.ember
                    )
                }
            }
        }
    }
}

private fun effectSummary(def: EmberUpgradeDef, level: Int): String {
    if (level == 0) return effectLabel(def.effect) + " +${formatPerLevel(def.effect)}/lv"
    val current = formatTotal(def.effect, level)
    return "$current (${formatPerLevel(def.effect)}/lv)"
}

private fun effectLabel(effect: UpgradeEffect): String = when (effect) {
    is UpgradeEffect.DamageMultiplier -> "DMG"
    is UpgradeEffect.HpMultiplier -> "HP"
    is UpgradeEffect.GoldMultiplier -> "Gold"
    is UpgradeEffect.OfflineEfficiency -> "Offline"
    is UpgradeEffect.StartingFloor -> "Start Floor"
    is UpgradeEffect.ExtraHeroSlot -> "Slots"
}

private fun formatPerLevel(effect: UpgradeEffect): String = when (effect) {
    is UpgradeEffect.DamageMultiplier -> "${(effect.perLevel * 100).toInt()}%"
    is UpgradeEffect.HpMultiplier -> "${(effect.perLevel * 100).toInt()}%"
    is UpgradeEffect.GoldMultiplier -> "${(effect.perLevel * 100).toInt()}%"
    is UpgradeEffect.OfflineEfficiency -> "${(effect.perLevel * 100).toInt()}%"
    is UpgradeEffect.StartingFloor -> "+${effect.perLevel}"
    is UpgradeEffect.ExtraHeroSlot -> "+${effect.perLevel}"
}

private fun formatTotal(effect: UpgradeEffect, level: Int): String = when (effect) {
    is UpgradeEffect.DamageMultiplier -> "+${(effect.perLevel * level * 100).toInt()}% DMG"
    is UpgradeEffect.HpMultiplier -> "+${(effect.perLevel * level * 100).toInt()}% HP"
    is UpgradeEffect.GoldMultiplier -> "+${(effect.perLevel * level * 100).toInt()}% Gold"
    is UpgradeEffect.OfflineEfficiency -> "+${(effect.perLevel * level * 100).toInt()}% Offline"
    is UpgradeEffect.StartingFloor -> "Floor ${1 + effect.perLevel * level}"
    is UpgradeEffect.ExtraHeroSlot -> "+${effect.perLevel * level} Slots"
}
