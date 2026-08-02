package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.mikmy.emberdepth.core.model.MaterialType
import com.mikmy.emberdepth.ui.theme.EmberColors

@Composable
fun MaterialBar(
    materials: Map<MaterialType, Int>,
    modifier: Modifier = Modifier
) {
    val visible = MaterialType.entries.filter { (materials[it] ?: 0) > 0 }
    if (visible.isEmpty()) return

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (type in visible) {
            val count = materials[type] ?: 0

            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = EmberColors.element(type.color)
            ) {}
            Spacer(Modifier.width(3.dp))
            Text(
                text = "$count",
                color = EmberColors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}
