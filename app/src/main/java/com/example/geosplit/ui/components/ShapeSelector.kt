package com.example.geosplit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geosplit.model.GeoShape

@Composable
fun ShapeIcon(
    shape: GeoShape,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidth = 2.dp.toPx()
        when (shape) {
            GeoShape.RECTANGLE -> {
                drawRect(
                    color = color,
                    topLeft = Offset(1.dp.toPx(), 4.dp.toPx()),
                    size = Size(size.width - 2.dp.toPx(), size.height - 8.dp.toPx()),
                    style = Stroke(width = strokeWidth)
                )
            }
            GeoShape.SQUARE -> {
                val s = minOf(size.width, size.height) - 4.dp.toPx()
                val offset = (size.width - s) / 2
                drawRect(
                    color = color,
                    topLeft = Offset(offset, offset),
                    size = Size(s, s),
                    style = Stroke(width = strokeWidth)
                )
            }
            GeoShape.CIRCLE -> {
                drawCircle(
                    color = color,
                    radius = (size.width / 2) - 2.dp.toPx(),
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
            GeoShape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(size.width / 2, 2.dp.toPx())
                    lineTo(size.width - 2.dp.toPx(), size.height - 2.dp.toPx())
                    lineTo(2.dp.toPx(), size.height - 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}

@Composable
fun ShapeSelector(
    selectedShape: GeoShape,
    onShapeSelected: (GeoShape) -> Unit,
    modifier: Modifier = Modifier
) {
    val shapes = GeoShape.values()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            shapes.forEach { shape ->
                val isSelected = shape == selectedShape
                val itemBackgroundColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(itemBackgroundColor)
                        .clickable { onShapeSelected(shape) }
                        .padding(vertical = 10.dp)
                        .semantics {
                            role = Role.RadioButton
                            contentDescription = "${shape.displayName} shape selector"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ShapeIcon(
                            shape = shape,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shape.displayName,
                            color = contentColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
