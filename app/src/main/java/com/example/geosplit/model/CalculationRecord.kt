package com.example.geosplit.model

import java.util.UUID

data class CalculationRecord(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String,
    val shape: GeoShape,
    val rectangleSplitMode: RectangleSplitMode? = null,
    val squareSplitMode: SquareSplitMode? = null,
    val squareGridPreset: SquareGridPreset? = null,
    val unit: MeasurementUnit,
    val lengthText: String = "",
    val widthText: String = "",
    val sideText: String = "",
    val radiusText: String = "",
    val triangleBaseText: String = "",
    val triangleHeightText: String = "",
    val sectionsText: String = "",
    val rowsText: String = "",
    val columnsText: String = "",
    val sectionCount: Int,
    val dimensionsSummary: String,
    val totalAreaFormatted: String,
    val areaPerSectionFormatted: String,
    val sectionDimensionsFormatted: String,
    val sectionPercentageFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
