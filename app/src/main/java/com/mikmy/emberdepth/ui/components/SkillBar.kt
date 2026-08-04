package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.model.ActiveSkill
import com.mikmy.emberdepth.ui.theme.EmberColors

data class SkillState(
    val slot: Int,
    val skill: ActiveSkill,
    val cooldownFrac: Float,
    val ready: Boolean,
    val alive: Boolean
)

@Composable
fun SkillBar(
    skills: List<SkillState>,
    onSkillTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (state in skills) {
            SkillButton(
                state = state,
                onClick = { onSkillTap(state.slot) },
                modifier = Modifier.padding(horizontal = 3.dp)
            )
        }
    }
}

@Composable
private fun SkillButton(
    state: SkillState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAlpha = if (state.ready && state.alive) 0.9f else 0.4f
    val borderColor = if (state.ready && state.alive)
        EmberColors.ember.copy(alpha = 0.8f)
    else
        EmberColors.textSecondary.copy(alpha = 0.3f)

    Surface(
        modifier = modifier
            .size(width = 52.dp, height = 52.dp)
            .clickable(enabled = state.ready && state.alive) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = EmberColors.surface.copy(alpha = bgAlpha),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = state.skill.icon,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = state.skill.name.split(" ").last().take(5),
                    color = if (state.ready) EmberColors.textPrimary else EmberColors.textSecondary,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (!state.ready && state.alive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                    ) {
                        LinearProgressIndicator(
                            progress = { 1f - state.cooldownFrac },
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = EmberColors.ember,
                            trackColor = EmberColors.ember.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}
