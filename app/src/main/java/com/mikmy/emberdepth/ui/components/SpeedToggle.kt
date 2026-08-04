package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikmy.emberdepth.ui.theme.EmberColors

@Composable
fun SpeedToggle(
    speed: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (speed) {
        2f -> "2x"
        3f -> "3x"
        else -> "1x"
    }
    val color = when (speed) {
        2f -> EmberColors.gold
        3f -> EmberColors.ember
        else -> EmberColors.textSecondary
    }

    Surface(
        modifier = modifier
            .size(36.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = EmberColors.surface.copy(alpha = 0.8f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
