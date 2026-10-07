package com.example.geosplit.calculator

import com.example.geosplit.model.GeoSplitResult
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.util.MathUtils
import java.math.BigDecimal
import java.math.MathContext

object GeoSplitCalculator {

    private val mc: MathContext = MathUtils.DEFAULT_MATH_CONTEXT

    /**
     * Calculates the area of each equal section: totalArea ÷ sections.
     * Uses full internal precision.
     */
    fun calculateSectionArea(totalAreaMm2: BigDecimal, sections: Int): BigDecimal {
        require(sections > 0) { "Sections must be greater than zero" }
        return MathUtils.divide(totalAreaMm2, BigDecimal(sections), mc)
    }

    /**
     * Calculates the percentage of total area each section occupies: 100 ÷ sections.
     */
    fun calculateSectionPercentage(sections: Int): BigDecimal {
        require(sections > 0) { "Sections must be greater than zero" }
        return MathUtils.divide(MathUtils.HUNDRED, BigDecimal(sections), mc)
    }

    /**
     * Calculates rectangle properties and split dimensions.
     */
    fun calculateRectangle(
        lengthMm: BigDecimal,
        widthMm: BigDecimal,
        sections: Int,
        splitMode: RectangleSplitMode
    ): GeoSplitResult {
        require(lengthMm.signum() > 0) { "Length must be positive" }
        require(widthMm.signum() > 0) { "Width must be positive" }
        require(sections >= 2) { "Sections must be at least 2" }

        val totalAreaMm2 = lengthMm.multiply(widthMm, mc)
        val areaPerSectionMm2 = calculateSectionArea(totalAreaMm2, sections)
        val sectionPercentage = calculateSectionPercentage(sections)
        val perimeterMm = MathUtils.TWO.multiply(lengthMm.add(widthMm, mc), mc)

        val (sectionLengthMm, sectionWidthMm) = when (splitMode) {
            RectangleSplitMode.ACROSS_LENGTH -> {
                val secLen = MathUtils.divide(lengthMm, BigDecimal(sections), mc)
                val secWid = widthMm
                Pair(secLen, secWid)
            }
            RectangleSplitMode.ACROSS_WIDTH -> {
                val secLen = lengthMm
                val secWid = MathUtils.divide(widthMm, BigDecimal(sections), mc)
                Pair(secLen, secWid)
            }
        }

        return GeoSplitResult(
            totalAreaMm2 = totalAreaMm2,
            areaPerSectionMm2 = areaPerSectionMm2,
            sectionPercentage = sectionPercentage,
            sectionLengthMm = sectionLengthMm,
            sectionWidthMm = sectionWidthMm,
            perimeterMm = perimeterMm,
            totalSections = sections
        )
    }

    /**
     * Calculates square equal strips split.
     */
    fun calculateSquareStrips(
        sideMm: BigDecimal,
        sections: Int,
        splitMode: RectangleSplitMode = RectangleSplitMode.ACROSS_LENGTH
    ): GeoSplitResult {
        require(sideMm.signum() > 0) { "Side must be positive" }
        require(sections >= 2) { "Sections must be at least 2" }

        val totalAreaMm2 = sideMm.multiply(sideMm, mc)
        val areaPerSectionMm2 = calculateSectionArea(totalAreaMm2, sections)
        val sectionPercentage = calculateSectionPercentage(sections)
        val perimeterMm = BigDecimal("4").multiply(sideMm, mc)

        val stripDimension = MathUtils.divide(sideMm, BigDecimal(sections), mc)
        val (sectionLengthMm, sectionWidthMm) = when (splitMode) {
            RectangleSplitMode.ACROSS_LENGTH -> Pair(stripDimension, sideMm)
            RectangleSplitMode.ACROSS_WIDTH -> Pair(sideMm, stripDimension)
        }

        return GeoSplitResult(
            totalAreaMm2 = totalAreaMm2,
            areaPerSectionMm2 = areaPerSectionMm2,
            sectionPercentage = sectionPercentage,
            sectionLengthMm = sectionLengthMm,
            sectionWidthMm = sectionWidthMm,
            perimeterMm = perimeterMm,
            totalSections = sections
        )
    }

    /**
     * Calculates square grid split.
     */
    fun calculateSquareGrid(
        sideMm: BigDecimal,
        rows: Int,
        columns: Int
    ): GeoSplitResult {
        require(sideMm.signum() > 0) { "Side must be positive" }
        require(rows in 1..20) { "Rows must be between 1 and 20" }
        require(columns in 1..20) { "Columns must be between 1 and 20" }
        val totalSections = rows * columns
        require(totalSections >= 2) { "Grid must contain at least 2 sections" }

        val totalAreaMm2 = sideMm.multiply(sideMm, mc)
        val areaPerSectionMm2 = calculateSectionArea(totalAreaMm2, totalSections)
        val sectionPercentage = calculateSectionPercentage(totalSections)
        val perimeterMm = BigDecimal("4").multiply(sideMm, mc)

        val sectionLengthMm = MathUtils.divide(sideMm, BigDecimal(rows), mc)
        val sectionWidthMm = MathUtils.divide(sideMm, BigDecimal(columns), mc)

        return GeoSplitResult(
            totalAreaMm2 = totalAreaMm2,
            areaPerSectionMm2 = areaPerSectionMm2,
            sectionPercentage = sectionPercentage,
            sectionLengthMm = sectionLengthMm,
            sectionWidthMm = sectionWidthMm,
            rows = rows,
            columns = columns,
            perimeterMm = perimeterMm,
            totalSections = totalSections
        )
    }

    /**
     * Calculates circle equal sectors split.
     */
    fun calculateCircle(
        radiusMm: BigDecimal,
        sections: Int
    ): GeoSplitResult {
        require(radiusMm.signum() > 0) { "Radius must be positive" }
        require(sections >= 2) { "Sections must be at least 2" }

        val radiusSquared = radiusMm.multiply(radiusMm, mc)
        val totalAreaMm2 = MathUtils.PI_BIG_DECIMAL.multiply(radiusSquared, mc)
        val areaPerSectionMm2 = calculateSectionArea(totalAreaMm2, sections)
        val sectionPercentage = calculateSectionPercentage(sections)
        val circumferenceMm = MathUtils.TWO.multiply(MathUtils.PI_BIG_DECIMAL, mc).multiply(radiusMm, mc)
        val sectionAngleDegrees = MathUtils.divide(MathUtils.THREE_SIXTY, BigDecimal(sections), mc)

        return GeoSplitResult(
            totalAreaMm2 = totalAreaMm2,
            areaPerSectionMm2 = areaPerSectionMm2,
            sectionPercentage = sectionPercentage,
            sectionAngleDegrees = sectionAngleDegrees,
            circumferenceMm = circumferenceMm,
            totalSections = sections
        )
    }

    /**
     * Calculates triangle equal-area sections parallel to base.
     */
    fun calculateTriangle(
        baseMm: BigDecimal,
        heightMm: BigDecimal,
        sections: Int
    ): GeoSplitResult {
        require(baseMm.signum() > 0) { "Base must be positive" }
        require(heightMm.signum() > 0) { "Height must be positive" }
        require(sections >= 2) { "Sections must be at least 2" }

        val totalAreaMm2 = MathUtils.divide(baseMm.multiply(heightMm, mc), MathUtils.TWO, mc)
        val areaPerSectionMm2 = calculateSectionArea(totalAreaMm2, sections)
        val sectionPercentage = calculateSectionPercentage(sections)
        val boundaries = calculateTriangleBoundaries(heightMm, sections)

        return GeoSplitResult(
            totalAreaMm2 = totalAreaMm2,
            areaPerSectionMm2 = areaPerSectionMm2,
            sectionPercentage = sectionPercentage,
            triangleBoundariesMm = boundaries,
            totalSections = sections
        )
    }

    /**
     * Calculates cumulative height boundaries from apex for n equal-area sections:
     * y(k) = height × √(k / n) for k = 1..sections.
     */
    fun calculateTriangleBoundaries(
        heightMm: BigDecimal,
        sections: Int
    ): List<BigDecimal> {
        val totalSectionsBd = BigDecimal(sections)
        return (1..sections).map { k ->
            val ratio = MathUtils.divide(BigDecimal(k), totalSectionsBd, mc)
            val sqrtRatio = MathUtils.sqrtBigDecimal(ratio, mc)
            heightMm.multiply(sqrtRatio, mc)
        }
    }
}
