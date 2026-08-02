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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.economy.GoldEconomy
import com.mikmy.emberdepth.core.model.BigNum
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
    onLevelUp: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cost = GoldEconomy.heroLevelCost(state.level)
    val canAfford = gold >= cost
    val elementColor = EmberColors.element(def.element.color)
    val stats = state.effectiveStats(def, emptyList())

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

                Spacer(Modifier.height(16.dp))

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
