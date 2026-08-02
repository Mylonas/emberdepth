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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.economy.EmberEconomy
import com.mikmy.emberdepth.core.model.BigNum
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun RebirthPanel(
    highestFloor: Int,
    currentEmber: BigNum,
    rebirthCount: Int,
    rebirthTier: Int,
    onRebirth: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emberToEarn = EmberEconomy.emberFromRebirth(highestFloor)
    val canRebirth = highestFloor >= 50
    val nextTier = EmberEconomy.rebirthTier(rebirthCount + 1)

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
                        text = "Rebirth",
                        color = EmberColors.ember,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
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

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmberColors.ember.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Highest Floor: $highestFloor",
                            color = EmberColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Ember to earn",
                            color = EmberColors.textSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (canRebirth) "+${emberToEarn.format()}" else "Reach floor 50",
                            color = if (canRebirth) EmberColors.ember else EmberColors.textSecondary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Rebirths", "$rebirthCount")
                    StatItem("Tier", "$rebirthTier")
                    StatItem("Ember", currentEmber.format())
                }

                if (nextTier > rebirthTier) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tier $nextTier unlock!",
                        color = EmberColors.gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmberColors.damage.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Reset on rebirth:",
                            color = EmberColors.damage.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gold, gear, materials, hero levels",
                            color = EmberColors.textSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Persists:",
                            color = EmberColors.health.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ember, ember upgrades, achievements, stats",
                            color = EmberColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                EmberButton(
                    text = if (canRebirth) "Rebirth" else "Floor 50 Required",
                    onClick = onRebirth,
                    enabled = canRebirth,
                    color = EmberColors.ember
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
            fontSize = 11.sp
        )
    }
}
