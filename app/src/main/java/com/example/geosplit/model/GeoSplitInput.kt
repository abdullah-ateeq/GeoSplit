package com.example.geosplit.model

import java.math.BigDecimal

data class GeoSplitInput(
    val projectName: String = "",
    val shape: GeoShape = GeoShape.RECTANGLE,
    val rectangleSplitMode: RectangleSplitMode? = RectangleSplitMode.ACROSS_LENGTH,
    val squareSplitMode: SquareSplitMode? = SquareSplitMode.STRIPS,
    val lengthMm: BigDecimal? = null,
    val widthMm: BigDecimal? = null,
    val sideMm: BigDecimal? = null,
    val radiusMm: BigDecimal? = null,
    val triangleBaseMm: BigDecimal? = null,
    val triangleHeightMm: BigDecimal? = null,
    val sections: Int? = 4,
    val rows: Int? = null,
    val columns: Int? = null,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER
)
