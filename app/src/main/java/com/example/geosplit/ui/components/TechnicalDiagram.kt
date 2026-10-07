package com.example.geosplit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.GeoSplitResult
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareSplitMode
import com.example.geosplit.ui.theme.DiagramGridDark
import com.example.geosplit.ui.theme.DiagramGridLight
import com.example.geosplit.ui.theme.DiagramLineDark
import com.example.geosplit.ui.theme.DiagramLineLight
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun TechnicalDiagram(
    shape: GeoShape,
    rectangleSplitMode: RectangleSplitMode,
    squareSplitMode: SquareSplitMode,
    result: GeoSplitResult?,
    lengthRatio: Float = 1f, // width / height ratio if available
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val blueprintGridColor = if (isDark) DiagramGridDark else DiagramGridLight
    val primaryLineColor = if (isDark) DiagramLineDark else DiagramLineLight
    val surfaceColor = MaterialTheme.colorScheme.surface
    val subtleOutline = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textPaintColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    val semanticDescription = when (shape) {
        GeoShape.RECTANGLE -> "Rectangle divided into ${result?.totalSections ?: 0} sections"
        GeoShape.SQUARE -> if (squareSplitMode == SquareSplitMode.GRID) {
            "Square divided into ${result?.rows ?: 0} by ${result?.columns ?: 0} grid"
        } else {
            "Square divided into ${result?.totalSections ?: 0} strips"
        }
        GeoShape.CIRCLE -> "Circle divided into ${result?.totalSections ?: 0} equal sectors"
        GeoShape.TRIANGLE -> "Triangle divided into ${result?.totalSections ?: 0} equal-area sections"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, subtleOutline, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .semantics { contentDescription = semanticDescription }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(214.dp)) {
            val width = size.width
            val height = size.height

            // 1. Draw subtle architectural background grid
            drawTechnicalGrid(width, height, blueprintGridColor)

            if (result == null || result.totalSections < 2) {
                return@Canvas
            }

            val padding = 28.dp.toPx()
            val availableWidth = width - (padding * 2)
            val availableHeight = height - (padding * 2)

            when (shape) {
                GeoShape.RECTANGLE -> {
                    drawRectangleDiagram(
                        availableWidth = availableWidth,
                        availableHeight = availableHeight,
                        padding = padding,
                        sections = result.totalSections,
                        splitMode = rectangleSplitMode,
                        lengthRatio = lengthRatio,
                        primaryColor = primaryLineColor,
                        textPaintColor = textPaintColor
                    )
                }
                GeoShape.SQUARE -> {
                    if (squareSplitMode == SquareSplitMode.GRID) {
                        drawSquareGridDiagram(
                            availableWidth = availableWidth,
                            availableHeight = availableHeight,
                            padding = padding,
                            rows = result.rows ?: 2,
                            columns = result.columns ?: 2,
                            primaryColor = primaryLineColor,
                            textPaintColor = textPaintColor
                        )
                    } else {
                        drawRectangleDiagram(
                            availableWidth = availableWidth,
                            availableHeight = availableHeight,
                            padding = padding,
                            sections = result.totalSections,
                            splitMode = rectangleSplitMode,
                            lengthRatio = 1f,
                            primaryColor = primaryLineColor,
                            textPaintColor = textPaintColor
                        )
                    }
                }
                GeoShape.CIRCLE -> {
                    drawCircleDiagram(
                        centerX = width / 2f,
                        centerY = height / 2f,
                        radius = minOf(availableWidth, availableHeight) / 2f - 4.dp.toPx(),
                        sections = result.totalSections,
                        primaryColor = primaryLineColor,
                        textPaintColor = textPaintColor
                    )
                }
                GeoShape.TRIANGLE -> {
                    drawTriangleDiagram(
                        availableWidth = availableWidth,
                        availableHeight = availableHeight,
                        padding = padding,
                        sections = result.totalSections,
                        primaryColor = primaryLineColor,
                        textPaintColor = textPaintColor
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTechnicalGrid(width: Float, height: Float, gridColor: Color) {
    val step = 20.dp.toPx()
    var x = 0f
    while (x < width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 0.5f
        )
        x += step
    }
    var y = 0f
    while (y < height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 0.5f
        )
        y += step
    }
}

private fun DrawScope.drawRectangleDiagram(
    availableWidth: Float,
    availableHeight: Float,
    padding: Float,
    sections: Int,
    splitMode: RectangleSplitMode,
    lengthRatio: Float,
    primaryColor: Color,
    textPaintColor: Int
) {
    // Proportional sizing
    val safeRatio = lengthRatio.coerceIn(0.35f, 2.8f)
    val rectW: Float
    val rectH: Float

    if (safeRatio >= 1f) {
        rectW = availableWidth
        rectH = (rectW / safeRatio).coerceIn(40.dp.toPx(), availableHeight)
    } else {
        rectH = availableHeight
        rectW = (rectH * safeRatio).coerceIn(40.dp.toPx(), availableWidth)
    }

    val left = padding + (availableWidth - rectW) / 2f
    val top = padding + (availableHeight - rectH) / 2f
    val right = left + rectW
    val bottom = top + rectH

    // Fill shape subtly
    drawRect(
        color = primaryColor.copy(alpha = 0.08f),
        topLeft = Offset(left, top),
        size = Size(rectW, rectH)
    )

    // Outline
    drawRect(
        color = primaryColor,
        topLeft = Offset(left, top),
        size = Size(rectW, rectH),
        style = Stroke(width = 2.dp.toPx())
    )

    // Division lines
    val showLabels = sections <= 12
    val paint = android.graphics.Paint().apply {
        color = textPaintColor
        textSize = 10.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    if (splitMode == RectangleSplitMode.ACROSS_LENGTH) {
        val step = rectW / sections
        for (i in 1 until sections) {
            val lineX = left + (i * step)
            drawLine(
                color = primaryColor.copy(alpha = 0.85f),
                start = Offset(lineX, top),
                end = Offset(lineX, bottom),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        if (showLabels) {
            for (i in 0 until sections) {
                val secCenterX = left + (i * step) + (step / 2f)
                val secCenterY = top + (rectH / 2f) + (paint.textSize / 3f)
                drawContext.canvas.nativeCanvas.drawText(
                    "${i + 1}",
                    secCenterX,
                    secCenterY,
                    paint
                )
            }
        }
    } else {
        val step = rectH / sections
        for (i in 1 until sections) {
            val lineY = top + (i * step)
            drawLine(
                color = primaryColor.copy(alpha = 0.85f),
                start = Offset(left, lineY),
                end = Offset(right, lineY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        if (showLabels) {
            for (i in 0 until sections) {
                val secCenterY = top + (i * step) + (step / 2f) + (paint.textSize / 3f)
                val secCenterX = left + (rectW / 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    "${i + 1}",
                    secCenterX,
                    secCenterY,
                    paint
                )
            }
        }
    }

    // Dimension indicators on corner
    drawDimensionTicks(left, top, right, bottom, primaryColor)
}

private fun DrawScope.drawSquareGridDiagram(
    availableWidth: Float,
    availableHeight: Float,
    padding: Float,
    rows: Int,
    columns: Int,
    primaryColor: Color,
    textPaintColor: Int
) {
    val sizePx = minOf(availableWidth, availableHeight)
    val left = padding + (availableWidth - sizePx) / 2f
    val top = padding + (availableHeight - sizePx) / 2f
    val right = left + sizePx
    val bottom = top + sizePx

    drawRect(
        color = primaryColor.copy(alpha = 0.08f),
        topLeft = Offset(left, top),
        size = Size(sizePx, sizePx)
    )

    drawRect(
        color = primaryColor,
        topLeft = Offset(left, top),
        size = Size(sizePx, sizePx),
        style = Stroke(width = 2.dp.toPx())
    )

    val colStep = sizePx / columns
    val rowStep = sizePx / rows

    for (c in 1 until columns) {
        val lineX = left + (c * colStep)
        drawLine(
            color = primaryColor.copy(alpha = 0.7f),
            start = Offset(lineX, top),
            end = Offset(lineX, bottom),
            strokeWidth = 1.dp.toPx()
        )
    }

    for (r in 1 until rows) {
        val lineY = top + (r * rowStep)
        drawLine(
            color = primaryColor.copy(alpha = 0.7f),
            start = Offset(left, lineY),
            end = Offset(right, lineY),
            strokeWidth = 1.dp.toPx()
        )
    }

    val total = rows * columns
    if (total <= 16) {
        val paint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        var count = 1
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val cx = left + (c * colStep) + (colStep / 2f)
                val cy = top + (r * rowStep) + (rowStep / 2f) + (paint.textSize / 3f)
                drawContext.canvas.nativeCanvas.drawText("$count", cx, cy, paint)
                count++
            }
        }
    }

    drawDimensionTicks(left, top, right, bottom, primaryColor)
}

private fun DrawScope.drawCircleDiagram(
    centerX: Float,
    centerY: Float,
    radius: Float,
    sections: Int,
    primaryColor: Color,
    textPaintColor: Int
) {
    drawCircle(
        color = primaryColor.copy(alpha = 0.08f),
        radius = radius,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = primaryColor,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 2.dp.toPx())
    )

    // Center point marker
    drawCircle(
        color = primaryColor,
        radius = 3.dp.toPx(),
        center = Offset(centerX, centerY)
    )

    val angleStepRad = (2 * Math.PI) / sections
    val showNumbers = sections in 2..12

    val paint = android.graphics.Paint().apply {
        color = textPaintColor
        textSize = 10.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    for (i in 0 until sections) {
        val angleRad = (i * angleStepRad) - (Math.PI / 2) // Start from top 12 o'clock
        val lineEndX = centerX + (radius * cos(angleRad)).toFloat()
        val lineEndY = centerY + (radius * sin(angleRad)).toFloat()

        drawLine(
            color = primaryColor.copy(alpha = 0.85f),
            start = Offset(centerX, centerY),
            end = Offset(lineEndX, lineEndY),
            strokeWidth = 1.5.dp.toPx()
        )

        if (showNumbers) {
            val midAngleRad = angleRad + (angleStepRad / 2.0)
            val labelRadius = radius * 0.62f
            val labelX = centerX + (labelRadius * cos(midAngleRad)).toFloat()
            val labelY = centerY + (labelRadius * sin(midAngleRad)).toFloat() + (paint.textSize / 3f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i + 1}",
                labelX,
                labelY,
                paint
            )
        }
    }

    if (!showNumbers) {
        val badgePaint = android.graphics.Paint().apply {
            color = textPaintColor
            textSize = 11.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "$sections equal sectors",
            centerX,
            centerY + radius + 18.dp.toPx(),
            badgePaint
        )
    }
}

private fun DrawScope.drawTriangleDiagram(
    availableWidth: Float,
    availableHeight: Float,
    padding: Float,
    sections: Int,
    primaryColor: Color,
    textPaintColor: Int
) {
    val apexX = padding + (availableWidth / 2f)
    val apexY = padding
    val leftBaseX = padding + (availableWidth * 0.1f)
    val rightBaseX = padding + (availableWidth * 0.9f)
    val baseY = padding + availableHeight
    val totalHeight = baseY - apexY

    val trianglePath = Path().apply {
        moveTo(apexX, apexY)
        lineTo(rightBaseX, baseY)
        lineTo(leftBaseX, baseY)
        close()
    }

    drawPath(
        path = trianglePath,
        color = primaryColor.copy(alpha = 0.08f)
    )

    drawPath(
        path = trianglePath,
        color = primaryColor,
        style = Stroke(width = 2.dp.toPx())
    )

    val showLabels = sections <= 10
    val paint = android.graphics.Paint().apply {
        color = textPaintColor
        textSize = 9.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    // Parallel equal-area boundaries: y(k) = height * sqrt(k / n) from apex
    var prevBoundaryY = apexY
    for (k in 1 until sections) {
        val ratio = sqrt(k.toDouble() / sections.toDouble()).toFloat()
        val boundaryY = apexY + (totalHeight * ratio)

        // Interpolate left and right x coordinates along the triangle edges
        val leftX = apexX + (leftBaseX - apexX) * ratio
        val rightX = apexX + (rightBaseX - apexX) * ratio

        drawLine(
            color = primaryColor.copy(alpha = 0.85f),
            start = Offset(leftX, boundaryY),
            end = Offset(rightX, boundaryY),
            strokeWidth = 1.5.dp.toPx()
        )

        if (showLabels) {
            val labelY = (prevBoundaryY + boundaryY) / 2f + (paint.textSize / 3f)
            drawContext.canvas.nativeCanvas.drawText("$k", apexX, labelY, paint)
        }
        prevBoundaryY = boundaryY
    }

    if (showLabels) {
        val lastLabelY = (prevBoundaryY + baseY) / 2f + (paint.textSize / 3f)
        drawContext.canvas.nativeCanvas.drawText("$sections", apexX, lastLabelY, paint)
    }
}

private fun DrawScope.drawDimensionTicks(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    color: Color
) {
    val tickLength = 5.dp.toPx()
    val tickColor = color.copy(alpha = 0.4f)
    val stroke = 1.dp.toPx()

    // Corners
    drawLine(tickColor, Offset(left - tickLength, top), Offset(left, top), stroke)
    drawLine(tickColor, Offset(left, top - tickLength), Offset(left, top), stroke)

    drawLine(tickColor, Offset(right + tickLength, top), Offset(right, top), stroke)
    drawLine(tickColor, Offset(right, top - tickLength), Offset(right, top), stroke)

    drawLine(tickColor, Offset(left - tickLength, bottom), Offset(left, bottom), stroke)
    drawLine(tickColor, Offset(left, bottom + tickLength), Offset(left, bottom), stroke)

    drawLine(tickColor, Offset(right + tickLength, bottom), Offset(right, bottom), stroke)
    drawLine(tickColor, Offset(right, bottom + tickLength), Offset(right, bottom), stroke)
}
