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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.GearSlot
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun GearInventoryPanel(
    gear: List<Gear>,
    heroes: List<HeroState>,
    heroNames: Map<String, String>,
    onSalvage: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterSlot by remember { mutableStateOf<GearSlot?>(null) }
    var expandedGearId by remember { mutableStateOf<Long?>(null) }
    var confirmSalvageId by remember { mutableStateOf<Long?>(null) }

    val filtered = gear
        .filter { filterSlot == null || it.slot == filterSlot }
        .sortedByDescending { it.rarity.ordinal }

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
                .padding(vertical = 48.dp)
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gear",
                        color = EmberColors.ember,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${gear.size} items",
                        color = EmberColors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "✕",
                        color = EmberColors.textSecondary,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip("All", filterSlot == null) { filterSlot = null }
                    GearSlot.entries.forEach { slot ->
                        FilterChip(slot.name, filterSlot == slot) { filterSlot = slot }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No gear yet — forge some!",
                            color = EmberColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            val equippedBy = findEquippedBy(item.id, heroes)
                            val equippedName = equippedBy?.let { heroNames[it.id] }
                            val isExpanded = expandedGearId == item.id
                            val isConfirmingSalvage = confirmSalvageId == item.id

                            GearCard(
                                gear = item,
                                equippedByName = equippedName,
                                expanded = isExpanded,
                                confirmingSalvage = isConfirmingSalvage,
                                onClick = {
                                    expandedGearId = if (isExpanded) null else item.id
                                    confirmSalvageId = null
                                },
                                onSalvageRequest = { confirmSalvageId = item.id },
                                onSalvageConfirm = {
                                    onSalvage(item.id)
                                    confirmSalvageId = null
                                    expandedGearId = null
                                },
                                onSalvageCancel = { confirmSalvageId = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) EmberColors.ember.copy(alpha = 0.2f) else EmberColors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) EmberColors.ember.copy(alpha = 0.5f) else EmberColors.surfaceLight
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = if (selected) EmberColors.ember else EmberColors.textSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun GearCard(
    gear: Gear,
    equippedByName: String?,
    expanded: Boolean,
    confirmingSalvage: Boolean,
    onClick: () -> Unit,
    onSalvageRequest: () -> Unit,
    onSalvageConfirm: () -> Unit,
    onSalvageCancel: () -> Unit
) {
    val rarityColor = EmberColors.element(gear.rarity.color)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = EmberColors.surfaceLight.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = gear.slot.name,
                    color = EmberColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = gear.rarity.label,
                    color = rarityColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (gear.element != null) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        modifier = Modifier.size(6.dp),
                        shape = CircleShape,
                        color = EmberColors.element(gear.element.color)
                    ) {}
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${gear.primaryStat.type.name} +${gear.primaryStat.value.format()}",
                    color = EmberColors.textSecondary,
                    fontSize = 12.sp
                )
            }

            if (equippedByName != null) {
                Text(
                    text = "Equipped by $equippedByName",
                    color = EmberColors.ember.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            if (expanded) {
                Spacer(Modifier.height(6.dp))
                for (stat in gear.secondaryStats) {
                    Text(
                        text = "${stat.type.name} +${stat.value.format()}",
                        color = EmberColors.textSecondary,
                        fontSize = 11.sp
                    )
                }
                if (gear.secondaryStats.isEmpty()) {
                    Text(
                        text = "No secondary stats",
                        color = EmberColors.disabled,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (confirmingSalvage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EmberButton(
                            text = "Confirm Salvage",
                            onClick = onSalvageConfirm,
                            color = EmberColors.damage
                        )
                        EmberButton(
                            text = "Cancel",
                            onClick = onSalvageCancel
                        )
                    }
                } else {
                    EmberButton(
                        text = "Salvage",
                        onClick = onSalvageRequest,
                        color = EmberColors.damage
                    )
                }
            }
        }
    }
}

private fun findEquippedBy(gearId: Long, heroes: List<HeroState>): HeroState? {
    return heroes.find { hero ->
        hero.weaponId == gearId || hero.armorId == gearId || hero.accessoryId == gearId
    }
}
