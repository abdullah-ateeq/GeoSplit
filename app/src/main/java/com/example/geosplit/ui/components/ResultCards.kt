package com.example.geosplit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geosplit.model.GeoShape

@Composable
fun MetricDetailCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), shape)
            .padding(14.dp)
            .semantics { contentDescription = "$title: $value" }
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SecondaryResultsSection(
    shape: GeoShape,
    totalAreaFormatted: String,
    perimeterOrCircumferenceFormatted: String,
    sectionDimensionsFormatted: String,
    anglePerSectionFormatted: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricDetailCard(
                title = "TOTAL AREA",
                value = totalAreaFormatted,
                modifier = Modifier.weight(1f)
            )

            when (shape) {
                GeoShape.RECTANGLE, GeoShape.SQUARE -> {
                    MetricDetailCard(
                        title = "SECTION SIZE",
                        value = sectionDimensionsFormatted.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }
                GeoShape.CIRCLE -> {
                    MetricDetailCard(
                        title = "SECTOR ANGLE",
                        value = anglePerSectionFormatted.ifBlank { "-" },
                        modifier = Modifier.weight(1f)
                    )
                }
                GeoShape.TRIANGLE -> {
                    MetricDetailCard(
                        title = "DIVISION TYPE",
                        value = "Equal-Area Slices",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (perimeterOrCircumferenceFormatted.isNotBlank()) {
            val title = if (shape == GeoShape.CIRCLE) "CIRCUMFERENCE" else "PERIMETER"
            MetricDetailCard(
                title = title,
                value = perimeterOrCircumferenceFormatted,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mathematical note
        val noteText = when (shape) {
            GeoShape.RECTANGLE -> "Sections have equal dimensions based on the selected split direction."
            GeoShape.SQUARE -> "Sections have equal dimensions across the chosen split or grid pattern."
            GeoShape.CIRCLE -> "All sectors have equal central angles and equal radial surface area."
            GeoShape.TRIANGLE -> "Triangle sections are equal in area, so their boundary lines are not equally spaced."
        }

        Text(
            text = noteText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
