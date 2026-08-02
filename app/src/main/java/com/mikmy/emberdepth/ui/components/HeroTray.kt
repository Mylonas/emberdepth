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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.ui.theme.EmberColors

@Composable
fun HeroTray(
    heroes: List<HeroState>,
    heroDefs: List<HeroDef>,
    onHeroTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (state in heroes) {
            val def = heroDefs.find { it.id == state.id } ?: continue
            HeroPortrait(
                def = def,
                state = state,
                onClick = { onHeroTap(state.id) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun HeroPortrait(
    def: HeroDef,
    state: HeroState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elementColor = EmberColors.element(def.element.color)

    Surface(
        modifier = modifier
            .size(width = 56.dp, height = 68.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = EmberColors.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.5.dp, elementColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = def.role.name[0].toString(),
                color = elementColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = def.name.take(4),
                color = EmberColors.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Lv${state.level}",
                color = EmberColors.textSecondary,
                fontSize = 9.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
            ) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = EmberColors.health,
                    trackColor = EmberColors.health.copy(alpha = 0.2f)
                )
            }
        }
    }
}
