package com.example.geosplit.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1300, easing = LinearEasing)
        )
        delay(100)
        onSplashComplete()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated technical geometric canvas
            Canvas(modifier = Modifier.size(140.dp)) {
                val t = progress.value // 0.0 to 1.0
                val stroke = 2.5.dp.toPx()
                val centerPt = Offset(size.width / 2f, size.height / 2f)

                if (t <= 0.35f) {
                    // Phase 1: Rectangle with dividing lines
                    val p = (t / 0.35f).coerceIn(0f, 1f)
                    val rw = size.width * 0.8f
                    val rh = size.height * 0.5f
                    val left = (size.width - rw) / 2f
                    val top = (size.height - rh) / 2f

                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, top),
                        size = Size(rw, rh),
                        style = Stroke(width = stroke)
                    )

                    // Draw 3 vertical division lines progressively
                    val sections = 4
                    val step = rw / sections
                    for (i in 1 until sections) {
                        val lineX = left + (i * step)
                        val lineProgress = (p * sections - (i - 1)).coerceIn(0f, 1f)
                        drawLine(
                            color = primaryColor.copy(alpha = 0.8f),
                            start = Offset(lineX, top),
                            end = Offset(lineX, top + rh * lineProgress),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                } else if (t <= 0.70f) {
                    // Phase 2: Circle with radial sectors
                    val p = ((t - 0.35f) / 0.35f).coerceIn(0f, 1f)
                    val r = size.width * 0.36f

                    drawCircle(
                        color = primaryColor,
                        radius = r,
                        center = centerPt,
                        style = Stroke(width = stroke)
                    )

                    val sectors = 6
                    val angleStep = (2 * Math.PI) / sectors
                    for (i in 0 until sectors) {
                        val angle = (i * angleStep) - (Math.PI / 2)
                        val endX = centerPt.x + (r * cos(angle) * p).toFloat()
                        val endY = centerPt.y + (r * sin(angle) * p).toFloat()
                        drawLine(
                            color = primaryColor.copy(alpha = 0.85f),
                            start = centerPt,
                            end = Offset(endX, endY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                } else {
                    // Phase 3: Triangle with equal-area parallel division lines
                    val p = ((t - 0.70f) / 0.30f).coerceIn(0f, 1f)
                    val apexX = size.width / 2f
                    val apexY = size.height * 0.15f
                    val leftX = size.width * 0.15f
                    val rightX = size.width * 0.85f
                    val baseY = size.height * 0.85f
                    val totalH = baseY - apexY

                    val triPath = Path().apply {
                        moveTo(apexX, apexY)
                        lineTo(rightX, baseY)
                        lineTo(leftX, baseY)
                        close()
                    }

                    drawPath(
                        path = triPath,
                        color = primaryColor,
                        style = Stroke(width = stroke)
                    )

                    // Draw parallel lines
                    val lines = 3
                    for (k in 1..lines) {
                        val ratio = kotlin.math.sqrt(k.toDouble() / (lines + 1)).toFloat() * p
                        val lineY = apexY + (totalH * ratio)
                        val lx = apexX + (leftX - apexX) * ratio
                        val rx = apexX + (rightX - apexX) * ratio
                        drawLine(
                            color = primaryColor.copy(alpha = 0.85f),
                            start = Offset(lx, lineY),
                            end = Offset(rx, lineY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title and subtitle fade in
            val textAlpha = ((progress.value - 0.4f) / 0.6f).coerceIn(0f, 1f)

            Text(
                text = "GEOSPLIT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = textAlpha),
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Divide geometric areas into equal sections",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha),
                letterSpacing = 0.5.sp
            )
        }
    }
}
