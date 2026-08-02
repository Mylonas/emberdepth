package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun StatsPanel(
    stats: Map<String, Long>,
    totalPlayTimeMs: Long,
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
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lifetime Stats",
                        color = EmberColors.ember,
                        fontSize = 20.sp,
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

                StatRow("Enemies Killed", formatNumber(stats["enemies_killed"] ?: 0))
                StatRow("Bosses Killed", formatNumber(stats["bosses_killed"] ?: 0))
                StatRow("Floors Cleared", formatNumber(stats["floors_cleared"] ?: 0))
                StatRow("Rebirths", formatNumber(stats["rebirths"] ?: 0))
                StatRow("Gear Forged", formatNumber(stats["gear_forged"] ?: 0))
                StatRow("Play Time", formatPlayTime(totalPlayTimeMs))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = EmberColors.textSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = EmberColors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatNumber(n: Long): String = when {
    n >= 1_000_000 -> "${n / 1_000_000}.${(n % 1_000_000) / 100_000}M"
    n >= 1_000 -> "${n / 1_000}.${(n % 1_000) / 100}K"
    else -> "$n"
}

private fun formatPlayTime(ms: Long): String {
    val totalMin = ms / 60_000
    val hours = totalMin / 60
    val mins = totalMin % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m"
        else -> "<1m"
    }
}
