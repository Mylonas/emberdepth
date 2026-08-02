package com.mikmy.emberdepth.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.economy.ForgeRecipes
import com.mikmy.emberdepth.core.economy.GoldEconomy
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun ForgePanel(
    gold: BigNum,
    currentFloor: Int,
    materials: Map<MaterialType, Int>,
    gearCount: Int,
    maxGear: Int,
    lastForged: Gear?,
    onForge: () -> Unit,
    onEquipForged: ((Long) -> Unit)?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = ForgeRecipes.basicForge()
    val goldCost = GoldEconomy.forgeCost(currentFloor)
    val canAffordMaterials = ForgeRecipes.canForge(recipe, materials)
    val canAffordGold = gold >= goldCost
    val inventoryFull = gearCount >= maxGear
    val canForge = canAffordMaterials && canAffordGold && !inventoryFull

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
                .fillMaxWidth(0.88f)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forge",
                        color = EmberColors.ember,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "$gearCount/$maxGear",
                        color = if (inventoryFull) EmberColors.damage else EmberColors.textSecondary,
                        fontSize = 12.sp
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

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CostItem(
                        label = "Gold",
                        cost = goldCost.format(),
                        have = gold.format(),
                        enough = canAffordGold,
                        color = EmberColors.gold
                    )
                    for ((type, needed) in recipe.materials) {
                        val have = materials[type] ?: 0
                        CostItem(
                            label = type.label,
                            cost = "$needed",
                            have = "$have",
                            enough = have >= needed,
                            color = EmberColors.element(type.color)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (lastForged != null) {
                    ForgeResultCard(gear = lastForged)
                    Spacer(Modifier.height(12.dp))

                    if (onEquipForged != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EmberButton(
                                text = "Forge Again",
                                onClick = onForge,
                                enabled = canForge
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Combine materials to create gear",
                        color = EmberColors.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    EmberButton(
                        text = "Forge",
                        onClick = onForge,
                        enabled = canForge,
                        color = EmberColors.ember
                    )
                }
            }
        }
    }
}

@Composable
private fun CostItem(
    label: String,
    cost: String,
    have: String,
    enough: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(Modifier.height(4.dp))
        Text(
            text = cost,
            color = if (enough) EmberColors.textPrimary else EmberColors.damage,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$have $label",
            color = EmberColors.textSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ForgeResultCard(gear: Gear, modifier: Modifier = Modifier) {
    val rarityColor = EmberColors.element(gear.rarity.color)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = rarityColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = gear.slot.name,
                    color = EmberColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = gear.rarity.label,
                    color = rarityColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (gear.element != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = EmberColors.element(gear.element.color)
                    ) {}
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = gear.element.label,
                        color = EmberColors.element(gear.element.color),
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${gear.primaryStat.type.name} +${gear.primaryStat.value.format()}",
                color = EmberColors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            for (stat in gear.secondaryStats) {
                Text(
                    text = "${stat.type.name} +${stat.value.format()}",
                    color = EmberColors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
