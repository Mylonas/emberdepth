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
import com.mikmy.emberdepth.core.economy.GoldEconomy
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.core.model.Gear
import com.mikmy.emberdepth.core.model.GearSlot
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun LevelUpPanel(
    def: HeroDef,
    state: HeroState,
    gold: BigNum,
    allGear: List<Gear>,
    onLevelUp: () -> Unit,
    onEquip: (Long) -> Unit,
    onUnequip: (GearSlot) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cost = GoldEconomy.heroLevelCost(state.level)
    val canAfford = gold >= cost
    val elementColor = EmberColors.element(def.element.color)

    val equippedGear = allGear.filter { g ->
        state.weaponId == g.id || state.armorId == g.id || state.accessoryId == g.id
    }
    val stats = state.effectiveStats(def, equippedGear)

    var pickingSlot by remember { mutableStateOf<GearSlot?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        EmberPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = elementColor.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = def.role.name[0].toString(),
                                color = elementColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = def.name,
                            color = EmberColors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${def.element.label} ${def.role.name}  ·  Lv ${state.level}",
                            color = EmberColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "✕",
                        color = EmberColors.textSecondary,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable { onDismiss() }.padding(8.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("HP", stats.hp.format())
                    StatItem("ATK", stats.atk.format())
                    StatItem("DEF", stats.def.format())
                    StatItem("SPD", "%.1f".format(stats.spd))
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GearSlot.entries.forEach { slot ->
                        val gearId = when (slot) {
                            GearSlot.WEAPON -> state.weaponId
                            GearSlot.ARMOR -> state.armorId
                            GearSlot.ACCESSORY -> state.accessoryId
                        }
                        val gear = gearId?.let { id -> allGear.find { it.id == id } }
                        GearSlotIndicator(
                            slot = slot,
                            gear = gear,
                            onClick = {
                                pickingSlot = if (pickingSlot == slot) null else slot
                            }
                        )
                    }
                }

                val currentPicking = pickingSlot
                if (currentPicking != null) {
                    Spacer(Modifier.height(8.dp))

                    val currentGearId = when (currentPicking) {
                        GearSlot.WEAPON -> state.weaponId
                        GearSlot.ARMOR -> state.armorId
                        GearSlot.ACCESSORY -> state.accessoryId
                    }

                    if (currentGearId != null) {
                        EmberButton(
                            text = "Unequip ${currentPicking.name}",
                            onClick = {
                                onUnequip(currentPicking)
                                pickingSlot = null
                            },
                            color = EmberColors.textSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    val available = allGear.filter { g ->
                        g.slot == currentPicking && g.id != currentGearId
                    }.sortedByDescending { it.rarity.ordinal }

                    if (available.isEmpty()) {
                        Text(
                            text = "No ${currentPicking.name.lowercase()} available",
                            color = EmberColors.textSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(available, key = { it.id }) { gear ->
                                GearPickRow(gear = gear, onClick = {
                                    onEquip(gear.id)
                                    pickingSlot = null
                                })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                EmberButton(
                    text = "Level Up  ⟨${cost.format()} gold⟩",
                    onClick = onLevelUp,
                    enabled = canAfford,
                    color = EmberColors.gold
                )
            }
        }
    }
}

@Composable
private fun GearSlotIndicator(slot: GearSlot, gear: Gear?, onClick: () -> Unit) {
    val label = when (slot) {
        GearSlot.WEAPON -> "⚔"
        GearSlot.ARMOR -> "🛡"
        GearSlot.ACCESSORY -> "💍"
    }
    val rarityColor = gear?.let { EmberColors.element(it.rarity.color) }

    Surface(
        modifier = Modifier
            .size(width = 80.dp, height = 48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = EmberColors.surfaceLight.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            rarityColor?.copy(alpha = 0.5f) ?: EmberColors.disabled.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, fontSize = 14.sp)
            if (gear != null) {
                Text(
                    text = gear.rarity.label.take(4),
                    color = rarityColor ?: EmberColors.textSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Empty",
                    color = EmberColors.disabled,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun GearPickRow(gear: Gear, onClick: () -> Unit) {
    val rarityColor = EmberColors.element(gear.rarity.color)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = EmberColors.surfaceLight.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gear.rarity.label,
                color = rarityColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${gear.primaryStat.type.name} +${gear.primaryStat.value.format()}",
                color = EmberColors.textPrimary,
                fontSize = 12.sp
            )
            if (gear.element != null) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    modifier = Modifier.size(6.dp),
                    shape = CircleShape,
                    color = EmberColors.element(gear.element.color)
                ) {}
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = EmberColors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = EmberColors.textSecondary,
            fontSize = 10.sp
        )
    }
}
