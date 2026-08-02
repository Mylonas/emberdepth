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
import com.mikmy.emberdepth.core.engine.OfflineResult
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun OfflinePopup(
    result: OfflineResult,
    adFree: Boolean = false,
    adReady: Boolean = false,
    onCollect: () -> Unit,
    onCollectDouble: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back!",
                    color = EmberColors.ember,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatDuration(result.elapsedMs),
                    color = EmberColors.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))

                if (!result.goldEarned.isZero()) {
                    RewardRow("Gold", result.goldEarned.format(), EmberColors.gold)
                }
                if (result.floorsCleared > 0) {
                    RewardRow("Floors", "+${result.floorsCleared}", EmberColors.ember)
                }
                for ((type, amount) in result.materialsEarned) {
                    if (amount > 0) {
                        RewardRow(type.label,
                            "+$amount", EmberColors.element(type.color))
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (onCollectDouble != null) {
                    EmberButton(
                        text = if (adFree) "Collect 2x" else "Watch Ad for 2x",
                        onClick = onCollectDouble,
                        enabled = adFree || adReady,
                        color = EmberColors.gold
                    )
                    Spacer(Modifier.height(8.dp))
                }
                EmberButton(text = "Collect", onClick = onCollect)
            }
        }
    }
}

@Composable
private fun RewardRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(Modifier.width(8.dp))
        Text(text = label, color = EmberColors.textSecondary, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatDuration(ms: Long): String {
    val totalMin = ms / 60_000
    val hours = totalMin / 60
    val mins = totalMin % 60
    return when {
        hours > 0 -> "Away for ${hours}h ${mins}m"
        mins > 0 -> "Away for ${mins}m"
        else -> "Away briefly"
    }
}
