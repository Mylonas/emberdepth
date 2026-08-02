package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.ui.theme.EmberButton
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun BossRewardPopup(
    floor: Int,
    adFree: Boolean,
    adReady: Boolean,
    onWatchAd: () -> Unit,
    onSkip: () -> Unit,
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
                    text = "Boss Defeated!",
                    color = EmberColors.ember,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Floor $floor cleared",
                    color = EmberColors.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Bonus loot: extra gold and materials!",
                    color = EmberColors.gold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                EmberButton(
                    text = if (adFree) "Claim Bonus" else "Watch Ad for Bonus",
                    onClick = onWatchAd,
                    enabled = adFree || adReady
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Skip",
                    color = EmberColors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
