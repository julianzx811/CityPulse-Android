package com.yulian.citypulse.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.yulian.citypulse.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherAnimation(weatherMain: String, modifier: Modifier = Modifier) {
    when (weatherMain.lowercase()) {
        "clear" -> SunAnimation(modifier)
        "clouds" -> CloudAnimation(modifier)
        "rain", "drizzle" -> RainAnimation(modifier)
        "thunderstorm" -> ThunderstormAnimation(modifier)
        "snow" -> SnowAnimation(modifier)
        else -> SunAnimation(modifier)
    }
}

@Composable
fun SunAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val radius = size.minDimension * 0.28f * pulse

        // Halo exterior
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SunMid.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(cx, cy),
                radius = radius * 1.8f
            ),
            radius = radius * 1.8f,
            center = Offset(cx, cy)
        )

        // Rayos
        for (i in 0..7) {
            val angle = Math.toRadians((rotation + i * 45.0))
            val startR = radius * 1.2f
            val endR = radius * 1.6f
            drawLine(
                color = SunMid.copy(alpha = 0.8f),
                start = Offset(cx + startR * cos(angle).toFloat(), cy + startR * sin(angle).toFloat()),
                end = Offset(cx + endR * cos(angle).toFloat(), cy + endR * sin(angle).toFloat()),
                strokeWidth = 6f
            )
        }

        // Sombra 3D
        drawCircle(
            color = SunShadow.copy(alpha = 0.4f),
            radius = radius,
            center = Offset(cx + radius * 0.15f, cy + radius * 0.15f)
        )

        // Sol principal
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SunCenter, SunMid, SunOuter),
                center = Offset(cx - radius * 0.2f, cy - radius * 0.2f),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Brillo superior izquierdo (efecto 3D)
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = radius * 0.35f,
            center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
        )
    }
}

@Composable
fun CloudAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "offsetX"
    )

    Canvas(modifier = modifier) {
        drawCloud(offsetX, Grey300, Grey200)
    }
}

fun DrawScope.drawCloud(offsetX: Float, shadowColor: Color, mainColor: Color) {
    val cx = size.width / 2 + offsetX
    val cy = size.height / 2
    val r = size.minDimension * 0.18f

    // Sombra
    drawCircle(shadowColor.copy(alpha = 0.3f), r * 2.5f, Offset(cx, cy + r * 0.3f))

    // Cuerpo nube
    drawCircle(mainColor, r * 1.1f, Offset(cx - r, cy))
    drawCircle(mainColor, r * 1.4f, Offset(cx, cy - r * 0.3f))
    drawCircle(mainColor, r * 1.1f, Offset(cx + r, cy))
    drawCircle(mainColor, r * 0.9f, Offset(cx - r * 1.8f, cy + r * 0.3f))
    drawCircle(mainColor, r * 0.9f, Offset(cx + r * 1.8f, cy + r * 0.3f))

    // Base plana
    drawRect(mainColor, Offset(cx - r * 2.5f, cy), androidx.compose.ui.geometry.Size(r * 5f, r * 1.2f))

    // Brillo 3D
    drawCircle(Color.White.copy(alpha = 0.4f), r * 0.5f, Offset(cx - r * 0.5f, cy - r * 0.8f))
}

@Composable
fun RainAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "rain"
    )

    Canvas(modifier = modifier) {
        drawCloud(0f, Grey400, RainCloudMain)

        // Gotas
        val dropPositions = listOf(0.2f, 0.4f, 0.6f, 0.8f, 0.3f, 0.5f, 0.7f)
        dropPositions.forEachIndexed { i, x ->
            val y = (rainOffset + i * 0.15f) % 1f
            val dropY = size.height * 0.55f + y * size.height * 0.4f
            drawLine(
                color = RainDropColor.copy(alpha = 1f - y),
                start = Offset(size.width * x, dropY),
                end = Offset(size.width * x - 4f, dropY + 20f),
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun ThunderstormAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "thunder")
    val flash by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "flash"
    )

    Canvas(modifier = modifier) {
        drawCloud(0f, StormCloudShadow, StormCloudMain)

        // Rayo
        val cx = size.width / 2
        val cy = size.height / 2
        val alpha = if (flash > 0.8f) 1f else 0.3f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx + 10f, cy + 20f)
            lineTo(cx - 10f, cy + 55f)
            lineTo(cx + 5f, cy + 55f)
            lineTo(cx - 15f, cy + 100f)
            lineTo(cx + 20f, cy + 58f)
            lineTo(cx + 5f, cy + 58f)
            close()
        }
        drawPath(path, LightningColor.copy(alpha = alpha))
    }
}

@Composable
fun SnowAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val snowOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "snow"
    )

    Canvas(modifier = modifier) {
        drawCloud(0f, Grey300, Grey200)

        val flakePositions = listOf(0.2f, 0.35f, 0.5f, 0.65f, 0.8f)
        flakePositions.forEachIndexed { i, x ->
            val y = (snowOffset + i * 0.2f) % 1f
            val flakeY = size.height * 0.55f + y * size.height * 0.4f
            drawCircle(
                Color.White.copy(alpha = 1f - y),
                radius = 5f,
                center = Offset(size.width * x, flakeY)
            )
        }
    }
}