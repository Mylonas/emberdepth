package com.mikmy.emberdepth.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mikmy.emberdepth.core.model.HeroDef
import com.mikmy.emberdepth.core.model.HeroState
import com.mikmy.emberdepth.ui.theme.EmberColors
import kotlin.math.roundToInt

@Composable
fun HeroTray(
    heroes: List<HeroState>,
    heroDefs: List<HeroDef>,
    onHeroTap: (String) -> Unit,
    onSwap: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var dragIndex by remember { mutableStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val itemWidth = 64f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for ((index, state) in heroes.withIndex()) {
            val def = heroDefs.find { it.id == state.id } ?: continue
            val isDragging = dragIndex == index

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .offset { IntOffset(if (isDragging) dragOffsetX.roundToInt() else 0, 0) }
                    .graphicsLayer {
                        if (isDragging) {
                            scaleX = 1.1f
                            scaleY = 1.1f
                            alpha = 0.85f
                        }
                    }
                    .padding(horizontal = 4.dp)
                    .pointerInput(heroes) {
                        if (onSwap == null) return@pointerInput
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragIndex = index
                                dragOffsetX = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount.x
                            },
                            onDragEnd = {
                                val slots = (dragOffsetX / (itemWidth * density)).roundToInt()
                                val targetIdx = (index + slots).coerceIn(0, heroes.lastIndex)
                                if (targetIdx != index) {
                                    onSwap(heroes[index].id, heroes[targetIdx].id)
                                }
                                dragIndex = -1
                                dragOffsetX = 0f
                            },
                            onDragCancel = {
                                dragIndex = -1
                                dragOffsetX = 0f
                            }
                        )
                    }
            ) {
                HeroPortrait(
                    def = def,
                    state = state,
                    onClick = { onHeroTap(state.id) }
                )
            }
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
