package com.mikmy.emberdepth.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object EmberColors {
    val bg = Color(0xFF080A14)
    val surface = Color(0xFF10131F)
    val surfaceLight = Color(0xFF1A1D2A)
    val ember = Color(0xFFE8733E)
    val gold = Color(0xFFFFD84D)
    val health = Color(0xFF4AE06A)
    val damage = Color(0xFFFF3B5C)
    val textPrimary = Color(0xFFE8E2D8)
    val textSecondary = Color(0xFF8A8278)
    val disabled = Color(0xFF4A4640)

    fun element(color: Long): Color = Color(color.toInt())
}

@Composable
fun EmberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = EmberColors.ember
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) color.copy(alpha = 0.2f) else EmberColors.disabled.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, if (enabled) color.copy(alpha = 0.6f) else EmberColors.disabled.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) EmberColors.textPrimary else EmberColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmberPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = EmberColors.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, EmberColors.ember.copy(alpha = 0.2f))
    ) {
        content()
    }
}
