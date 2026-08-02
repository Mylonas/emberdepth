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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.content.AchievementDefs
import com.mikmy.emberdepth.core.model.AchievementDef
import com.mikmy.emberdepth.core.model.AchievementState
import com.mikmy.emberdepth.ui.theme.EmberColors
import com.mikmy.emberdepth.ui.theme.EmberPanel

@Composable
fun AchievementPanel(
    achievements: List<AchievementState>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateMap = achievements.associateBy { it.id }

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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievements",
                        color = EmberColors.gold,
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

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (def in AchievementDefs.ALL) {
                        val state = stateMap[def.id] ?: AchievementState(def.id)
                        AchievementCard(def = def, state = state)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    def: AchievementDef,
    state: AchievementState
) {
    val active = state.tier > 0
    val maxed = state.tier >= def.maxTier
    val alpha = if (active) 1f else 0.5f

    val nextTarget = if (!maxed) def.targets[state.tier] else def.targets.last()
    val progress = if (!maxed) {
        (state.progress.toFloat() / nextTarget.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmberColors.surfaceLight.copy(alpha = 0.6f * alpha),
        border = BorderStroke(
            1.dp,
            if (active) EmberColors.gold.copy(alpha = 0.2f)
            else EmberColors.disabled.copy(alpha = 0.2f)
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
                        color = if (active) EmberColors.gold else EmberColors.textSecondary,
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
                    text = if (maxed) "MAX" else "Tier ${state.tier}/${def.maxTier}",
                    color = if (maxed) EmberColors.gold else EmberColors.textPrimary.copy(alpha = alpha),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (maxed) EmberColors.gold else EmberColors.ember,
                trackColor = EmberColors.disabled.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (maxed) "Complete!" else "${state.progress}/${nextTarget}",
                    color = EmberColors.textSecondary.copy(alpha = alpha),
                    fontSize = 10.sp
                )
                Spacer(Modifier.weight(1f))
                if (state.tier > 0) {
                    Text(
                        text = "+${(def.bonusPerTier * state.tier * 100).toInt()}% ${def.bonusType.name}",
                        color = EmberColors.health.copy(alpha = alpha),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
