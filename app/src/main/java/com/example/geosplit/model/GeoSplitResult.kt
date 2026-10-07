package com.example.geosplit.model

import java.math.BigDecimal

data class GeoSplitResult(
    val totalAreaMm2: BigDecimal,
    val areaPerSectionMm2: BigDecimal,
    val sectionPercentage: BigDecimal,
    val sectionLengthMm: BigDecimal? = null,
    val sectionWidthMm: BigDecimal? = null,
    val sectionAngleDegrees: BigDecimal? = null,
    val rows: Int? = null,
    val columns: Int? = null,
    val totalSections: Int = 1,
    val perimeterMm: BigDecimal? = null,
    val circumferenceMm: BigDecimal? = null,
    val triangleBoundariesMm: List<BigDecimal> = emptyList()
)
