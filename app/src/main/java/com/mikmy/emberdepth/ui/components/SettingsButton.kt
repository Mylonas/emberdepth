package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.mikmy.emberdepth.BuildConfig
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun SettingsButton(
    isMuted: Boolean,
    highestFloor: Int,
    onToggleMute: () -> Unit,
    onRebirth: (() -> Unit)? = null,
    onStats: (() -> Unit)? = null,
    onResetProgress: (() -> Unit)? = null,
    onRemoveAds: (() -> Unit)? = null,
    isAdFree: Boolean = false,
    removeAdsPrice: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable { expanded = !expanded },
            shape = CircleShape,
            color = EmberColors.surface.copy(alpha = 0.8f)
        ) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (expanded) "✕" else "⚙",
                    color = EmberColors.textSecondary,
                    fontSize = 16.sp
                )
            }
        }

        if (expanded) {
            Spacer(Modifier.height(4.dp))
            EmberPanel {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sound",
                            color = EmberColors.textPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = !isMuted,
                            onCheckedChange = { onToggleMute() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmberColors.ember,
                                checkedTrackColor = EmberColors.ember.copy(alpha = 0.3f),
                                uncheckedThumbColor = EmberColors.textSecondary,
                                uncheckedTrackColor = EmberColors.surface
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Highest Floor: $highestFloor",
                        color = EmberColors.textSecondary,
                        fontSize = 11.sp
                    )
                    if (onRebirth != null && highestFloor >= 50) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "🔥 Rebirth",
                            color = EmberColors.ember,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onRebirth() }
                                .padding(vertical = 4.dp)
                        )
                    }
                    if (onStats != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "📊 Stats",
                            color = EmberColors.textPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { onStats() }
                                .padding(vertical = 4.dp)
                        )
                    }
                    if (onRemoveAds != null && !isAdFree) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Remove Ads${removeAdsPrice?.let { " — $it" } ?: ""}",
                            color = EmberColors.gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onRemoveAds() }
                                .padding(vertical = 4.dp)
                        )
                    }
                    if (isAdFree) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "✓ Ad-Free",
                            color = EmberColors.health,
                            fontSize = 11.sp
                        )
                    }
                    if (onResetProgress != null) {
                        Spacer(Modifier.height(8.dp))
                        if (!confirmReset) {
                            Text(
                                text = "Reset Progress",
                                color = EmberColors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable { confirmReset = true }
                                    .padding(vertical = 4.dp)
                            )
                        } else {
                            Text(
                                text = "⚠ Tap again to ERASE ALL DATA",
                                color = EmberColors.damage,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        confirmReset = false
                                        expanded = false
                                        onResetProgress()
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "EmberDepth v${BuildConfig.VERSION_NAME}",
                        color = EmberColors.textSecondary.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
