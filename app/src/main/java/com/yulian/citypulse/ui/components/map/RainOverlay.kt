package com.yulian.citypulse.ui.components.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.yulian.citypulse.ui.theme.RainDropColor
import kotlin.random.Random

@Composable
fun RainOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rainDrop"
    )

    val drops = remember {
        List(80) {
            Triple(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat() * 0.8f + 0.5f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drops.forEach { (x, startY, speed) ->
            val y = (startY + rainOffset * speed) % 1f
            val alpha = (0.3f + speed * 0.3f).coerceIn(0f, 0.6f)
            drawLine(
                color = RainDropColor.copy(alpha = alpha),
                start = Offset(size.width * x, size.height * y),
                end = Offset(size.width * x - 2f, size.height * y + 18f),
                strokeWidth = 2f
            )
        }
    }
}