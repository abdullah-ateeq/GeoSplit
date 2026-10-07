package com.example.geosplit

import com.example.geosplit.calculator.GeoSplitCalculator
import com.example.geosplit.data.AppSettings
import com.example.geosplit.data.AppThemeMode
import com.example.geosplit.data.HistoryRepository
import com.example.geosplit.model.CalculationRecord
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareGridPreset
import com.example.geosplit.model.SquareSplitMode
import com.example.geosplit.util.MathUtils
import com.example.geosplit.util.UnitConverter
import com.example.geosplit.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class GeoSplitComprehensiveTest {

    private val mc = MathUtils.DEFAULT_MATH_CONTEXT

    // 1. Rectangle area
    @Test
    fun test01_rectangleArea() {
        // 120 cm x 50 cm -> 1200 mm x 500 mm
        val lengthMm = UnitConverter.toMillimeters(BigDecimal("120"), MeasurementUnit.CENTIMETER)
        val widthMm = UnitConverter.toMillimeters(BigDecimal("50"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateRectangle(lengthMm, widthMm, 4, RectangleSplitMode.ACROSS_LENGTH)

        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        assertEquals(0, BigDecimal("6000").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 2. Rectangle equal split
    @Test
    fun test02_rectangleEqualSplit() {
        val lengthMm = UnitConverter.toMillimeters(BigDecimal("120"), MeasurementUnit.CENTIMETER)
        val widthMm = UnitConverter.toMillimeters(BigDecimal("50"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateRectangle(lengthMm, widthMm, 4, RectangleSplitMode.ACROSS_LENGTH)

        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        assertEquals(0, BigDecimal("1500").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 3. Rectangle split across length
    @Test
    fun test03_rectangleSplitAcrossLength() {
        val lengthMm = UnitConverter.toMillimeters(BigDecimal("120"), MeasurementUnit.CENTIMETER)
        val widthMm = UnitConverter.toMillimeters(BigDecimal("50"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateRectangle(lengthMm, widthMm, 4, RectangleSplitMode.ACROSS_LENGTH)

        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("30").compareTo(secLenCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("50").compareTo(secWidCm.setScale(2, RoundingMode.HALF_UP)))
    }

    // 4. Rectangle split across width
    @Test
    fun test04_rectangleSplitAcrossWidth() {
        val lengthMm = UnitConverter.toMillimeters(BigDecimal("120"), MeasurementUnit.CENTIMETER)
        val widthMm = UnitConverter.toMillimeters(BigDecimal("50"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateRectangle(lengthMm, widthMm, 5, RectangleSplitMode.ACROSS_WIDTH)

        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("6000").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("1200").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("120").compareTo(secLenCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("10").compareTo(secWidCm.setScale(2, RoundingMode.HALF_UP)))
    }

    // 5. Square strips
    @Test
    fun test05_squareStrips() {
        val sideMm = UnitConverter.toMillimeters(BigDecimal("100"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateSquareStrips(sideMm, 4, RectangleSplitMode.ACROSS_LENGTH)

        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("10000").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("2500").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("25").compareTo(secLenCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("100").compareTo(secWidCm.setScale(2, RoundingMode.HALF_UP)))
    }

    // 6. Square 2x2 grid
    @Test
    fun test06_square2x2Grid() {
        val sideMm = UnitConverter.toMillimeters(BigDecimal("100"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateSquareGrid(sideMm, 2, 2)

        assertEquals(4, result.totalSections)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("2500").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("50").compareTo(secLenCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("50").compareTo(secWidCm.setScale(2, RoundingMode.HALF_UP)))
    }

    // 7. Square 4x4 grid
    @Test
    fun test07_square4x4Grid() {
        val sideMm = UnitConverter.toMillimeters(BigDecimal("100"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateSquareGrid(sideMm, 4, 4)

        assertEquals(16, result.totalSections)
        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("10000").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("625").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("25").compareTo(secLenCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("25").compareTo(secWidCm.setScale(2, RoundingMode.HALF_UP)))
    }

    // 8. Square custom grid
    @Test
    fun test08_squareCustomGrid() {
        val sideMm = UnitConverter.toMillimeters(BigDecimal("120"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateSquareGrid(sideMm, 3, 4)

        assertEquals(12, result.totalSections)
        val secHeightCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val secWidthCm = UnitConverter.fromMillimeters(result.sectionWidthMm!!, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("40").compareTo(secHeightCm.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("30").compareTo(secWidthCm.setScale(2, RoundingMode.HALF_UP)))

        val secAreaCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)
        assertEquals(0, BigDecimal("1200").compareTo(secAreaCm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 9. Circle equal sectors
    @Test
    fun test09_circleEqualSectors() {
        val radiusMm = UnitConverter.toMillimeters(BigDecimal("10"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateCircle(radiusMm, 4)

        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)

        // Expected total area: ~314.1593 cm²
        assertEquals(0, BigDecimal("314.16").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
        // Expected per section: ~78.5398 cm²
        assertEquals(0, BigDecimal("78.54").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 10. Circle section angle
    @Test
    fun test10_circleSectionAngle() {
        val radiusMm = UnitConverter.toMillimeters(BigDecimal("10"), MeasurementUnit.CENTIMETER)

        val res4 = GeoSplitCalculator.calculateCircle(radiusMm, 4)
        assertEquals(0, BigDecimal("90").compareTo(res4.sectionAngleDegrees!!.setScale(2, RoundingMode.HALF_UP)))

        val res6 = GeoSplitCalculator.calculateCircle(radiusMm, 6)
        assertEquals(0, BigDecimal("60").compareTo(res6.sectionAngleDegrees!!.setScale(2, RoundingMode.HALF_UP)))

        val res8 = GeoSplitCalculator.calculateCircle(radiusMm, 8)
        assertEquals(0, BigDecimal("45").compareTo(res8.sectionAngleDegrees!!.setScale(2, RoundingMode.HALF_UP)))

        val res12 = GeoSplitCalculator.calculateCircle(radiusMm, 12)
        assertEquals(0, BigDecimal("30").compareTo(res12.sectionAngleDegrees!!.setScale(2, RoundingMode.HALF_UP)))
    }

    // 11. Triangle area
    @Test
    fun test11_triangleArea() {
        val baseMm = UnitConverter.toMillimeters(BigDecimal("100"), MeasurementUnit.CENTIMETER)
        val heightMm = UnitConverter.toMillimeters(BigDecimal("60"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateTriangle(baseMm, heightMm, 4)

        val totalAreaCm2 = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, MeasurementUnit.CENTIMETER)
        val areaPerSectionCm2 = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, MeasurementUnit.CENTIMETER)

        assertEquals(0, BigDecimal("3000").compareTo(totalAreaCm2.setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("750").compareTo(areaPerSectionCm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 12. Triangle equal-area boundaries
    @Test
    fun test12_triangleEqualAreaBoundaries() {
        val heightMm = BigDecimal("600") // 60 cm in mm
        val boundaries = GeoSplitCalculator.calculateTriangleBoundaries(heightMm, 4)

        assertEquals(4, boundaries.size)
        // For k = 1: 600 * sqrt(1/4) = 600 * 0.5 = 300 mm = 30 cm
        assertEquals(0, BigDecimal("300").compareTo(boundaries[0].setScale(1, RoundingMode.HALF_UP)))
        // For k = 4: 600 * sqrt(4/4) = 600 mm = 60 cm (apex to base)
        assertEquals(0, BigDecimal("600").compareTo(boundaries[3].setScale(1, RoundingMode.HALF_UP)))
    }

    // 13. Section percentage
    @Test
    fun test13_sectionPercentage() {
        assertEquals(0, BigDecimal("50.00").compareTo(GeoSplitCalculator.calculateSectionPercentage(2).setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("25.00").compareTo(GeoSplitCalculator.calculateSectionPercentage(4).setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("20.00").compareTo(GeoSplitCalculator.calculateSectionPercentage(5).setScale(2, RoundingMode.HALF_UP)))
        assertEquals(0, BigDecimal("10.00").compareTo(GeoSplitCalculator.calculateSectionPercentage(10).setScale(2, RoundingMode.HALF_UP)))
    }

    // 14. Section count validation
    @Test
    fun test14_sectionCountValidation() {
        assertTrue(ValidationUtils.validateSections(2) is ValidationUtils.ValidationResult.Valid)
        assertTrue(ValidationUtils.validateSections(100) is ValidationUtils.ValidationResult.Valid)
        assertTrue(ValidationUtils.validateSections(50) is ValidationUtils.ValidationResult.Valid)

        assertTrue(ValidationUtils.validateSections(1) is ValidationUtils.ValidationResult.Invalid)
        assertTrue(ValidationUtils.validateSections(0) is ValidationUtils.ValidationResult.Invalid)
        assertTrue(ValidationUtils.validateSections(-5) is ValidationUtils.ValidationResult.Invalid)
        assertTrue(ValidationUtils.validateSections(101) is ValidationUtils.ValidationResult.Invalid)
    }

    // 15. Grid validation
    @Test
    fun test15_gridValidation() {
        assertTrue(ValidationUtils.validateGrid(2, 2) is ValidationUtils.ValidationResult.Valid)
        assertTrue(ValidationUtils.validateGrid(20, 20) is ValidationUtils.ValidationResult.Valid)

        // 1x1 total is 1 section -> invalid
        assertTrue(ValidationUtils.validateGrid(1, 1) is ValidationUtils.ValidationResult.Invalid)
        // Rows or cols > 20 -> invalid
        assertTrue(ValidationUtils.validateGrid(21, 2) is ValidationUtils.ValidationResult.Invalid)
        assertTrue(ValidationUtils.validateGrid(2, 0) is ValidationUtils.ValidationResult.Invalid)
    }

    // 16. Unit conversion (linear)
    @Test
    fun test16_unitConversionLinear() {
        // 120 cm -> 1.2 m
        val cmValue = BigDecimal("120")
        val mValue = UnitConverter.convertLinear(cmValue, MeasurementUnit.CENTIMETER, MeasurementUnit.METER)
        assertEquals(0, BigDecimal("1.2").compareTo(mValue.setScale(4, RoundingMode.HALF_UP)))

        // 1 in -> 25.4 mm
        val inValue = BigDecimal("1")
        val mmValue = UnitConverter.convertLinear(inValue, MeasurementUnit.INCH, MeasurementUnit.MILLIMETER)
        assertEquals(0, BigDecimal("25.4").compareTo(mmValue.setScale(4, RoundingMode.HALF_UP)))

        // 1 ft -> 304.8 mm
        val ftValue = BigDecimal("1")
        val ftMm = UnitConverter.convertLinear(ftValue, MeasurementUnit.FOOT, MeasurementUnit.MILLIMETER)
        assertEquals(0, BigDecimal("304.8").compareTo(ftMm.setScale(4, RoundingMode.HALF_UP)))
    }

    // 17. Area unit conversion (squared)
    @Test
    fun test17_areaUnitConversionSquared() {
        // 6000 cm² -> 0.60 m² (1 m² = 10000 cm²)
        val cm2Value = BigDecimal("6000")
        val m2Value = UnitConverter.convertArea(cm2Value, MeasurementUnit.CENTIMETER, MeasurementUnit.METER)
        assertEquals(0, BigDecimal("0.6").compareTo(m2Value.setScale(4, RoundingMode.HALF_UP)))

        // 1 in² -> 645.16 mm²
        val in2Value = BigDecimal("1")
        val inMm2 = UnitConverter.convertArea(in2Value, MeasurementUnit.INCH, MeasurementUnit.MILLIMETER)
        assertEquals(0, BigDecimal("645.16").compareTo(inMm2.setScale(2, RoundingMode.HALF_UP)))
    }

    // 18. Decimal dimensions (e.g. 33.3333 cm)
    @Test
    fun test18_decimalDimensions() {
        // 100 cm divided into 3 sections -> 33.3333... cm
        val lengthMm = UnitConverter.toMillimeters(BigDecimal("100"), MeasurementUnit.CENTIMETER)
        val widthMm = UnitConverter.toMillimeters(BigDecimal("30"), MeasurementUnit.CENTIMETER)
        val result = GeoSplitCalculator.calculateRectangle(lengthMm, widthMm, 3, RectangleSplitMode.ACROSS_LENGTH)

        val secLenCm = UnitConverter.fromMillimeters(result.sectionLengthMm!!, MeasurementUnit.CENTIMETER)
        val formatted = MathUtils.formatNumber(secLenCm, 2)
        assertEquals("33.33", formatted)

        val formatted4 = MathUtils.formatNumber(secLenCm, 4)
        assertEquals("33.3333", formatted4)
    }

    // 19. Zero dimensions
    @Test
    fun test19_zeroDimensions() {
        val result = ValidationUtils.validateDimension(BigDecimal.ZERO)
        assertTrue(result is ValidationUtils.ValidationResult.Invalid)
        assertEquals("Value must be greater than zero.", (result as ValidationUtils.ValidationResult.Invalid).message)
    }

    // 20. Negative dimensions
    @Test
    fun test20_negativeDimensions() {
        val result = ValidationUtils.validateDimension(BigDecimal("-15"))
        assertTrue(result is ValidationUtils.ValidationResult.Invalid)
        assertEquals("Value must be greater than zero.", (result as ValidationUtils.ValidationResult.Invalid).message)
    }

    // 21. Invalid section count
    @Test
    fun test21_invalidSectionCount() {
        val nullResult = ValidationUtils.validateSections(null)
        assertTrue(nullResult is ValidationUtils.ValidationResult.Invalid)

        val zeroResult = ValidationUtils.validateSections(0)
        assertTrue(zeroResult is ValidationUtils.ValidationResult.Invalid)

        val overMax = ValidationUtils.validateSections(105)
        assertTrue(overMax is ValidationUtils.ValidationResult.Invalid)
        assertEquals("Sections cannot exceed 100.", (overMax as ValidationUtils.ValidationResult.Invalid).message)
    }

    // 22. BigDecimal precision (Newton-Raphson sqrtBigDecimal and high precision calculations)
    @Test
    fun test22_bigDecimalPrecision() {
        // sqrt(2) to 25 decimal places
        val sqrt2 = MathUtils.sqrtBigDecimal(BigDecimal("2"))
        val sqrt2Squared = sqrt2.multiply(sqrt2, mc)
        assertEquals(0, BigDecimal("2").compareTo(sqrt2Squared.setScale(20, RoundingMode.HALF_UP)))

        // Large numbers
        val large = BigDecimal("1000000000000")
        val sqrtLarge = MathUtils.sqrtBigDecimal(large)
        assertEquals(0, BigDecimal("1000000").compareTo(sqrtLarge.setScale(5, RoundingMode.HALF_UP)))
    }

    // 23. History serialization & deserialization
    @Test
    fun test23_historySerialization() {
        val testRecord = CalculationRecord(
            projectName = "Floor Panels Test",
            shape = GeoShape.RECTANGLE,
            rectangleSplitMode = RectangleSplitMode.ACROSS_LENGTH,
            unit = MeasurementUnit.CENTIMETER,
            lengthText = "120",
            widthText = "50",
            sectionsText = "4",
            sectionCount = 4,
            dimensionsSummary = "120 × 50 cm",
            totalAreaFormatted = "6,000 cm²",
            areaPerSectionFormatted = "1,500 cm²",
            sectionDimensionsFormatted = "30 × 50 cm",
            sectionPercentageFormatted = "25%",
            isFavorite = true
        )

        val json = HistoryRepository.serializeRecords(listOf(testRecord))
        assertTrue(json.contains("Floor Panels Test"))
        assertTrue(json.contains("RECTANGLE"))

        val deserialized = HistoryRepository.deserializeRecords(json)
        assertEquals(1, deserialized.size)
        assertEquals("Floor Panels Test", deserialized[0].projectName)
        assertEquals(GeoShape.RECTANGLE, deserialized[0].shape)
        assertEquals(true, deserialized[0].isFavorite)
        assertEquals("1,500 cm²", deserialized[0].areaPerSectionFormatted)
    }

    // 24. Settings model defaults
    @Test
    fun test24_settingsDefaults() {
        val settings = AppSettings()
        assertEquals(AppThemeMode.SYSTEM, settings.themeMode)
        assertEquals(GeoShape.RECTANGLE, settings.defaultShape)
        assertEquals(4, settings.defaultSections)
        assertEquals(MeasurementUnit.CENTIMETER, settings.defaultUnit)
        assertEquals(2, settings.decimalPlaces)
        assertTrue(settings.showDiagram)
        assertTrue(settings.showFormula)
        assertTrue(settings.showSectionBreakdown)
        assertTrue(settings.hapticFeedback)
    }

    // 25. Favorites sorting
    @Test
    fun test25_favoritesSorting() {
        val rec1 = CalculationRecord(projectName = "Record 1", shape = GeoShape.RECTANGLE, unit = MeasurementUnit.CENTIMETER, sectionCount = 4, dimensionsSummary = "", totalAreaFormatted = "", areaPerSectionFormatted = "", sectionDimensionsFormatted = "", sectionPercentageFormatted = "", isFavorite = false, timestamp = 1000L)
        val rec2 = CalculationRecord(projectName = "Record 2", shape = GeoShape.SQUARE, unit = MeasurementUnit.CENTIMETER, sectionCount = 4, dimensionsSummary = "", totalAreaFormatted = "", areaPerSectionFormatted = "", sectionDimensionsFormatted = "", sectionPercentageFormatted = "", isFavorite = true, timestamp = 500L)
        val rec3 = CalculationRecord(projectName = "Record 3", shape = GeoShape.CIRCLE, unit = MeasurementUnit.CENTIMETER, sectionCount = 4, dimensionsSummary = "", totalAreaFormatted = "", areaPerSectionFormatted = "", sectionDimensionsFormatted = "", sectionPercentageFormatted = "", isFavorite = false, timestamp = 2000L)

        val list = listOf(rec1, rec2, rec3)
        val sorted = list.sortedWith(
            compareByDescending<CalculationRecord> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        // Favorite (rec2) must be first even though rec3 has higher timestamp
        assertEquals("Record 2", sorted[0].projectName)
        assertEquals("Record 3", sorted[1].projectName)
        assertEquals("Record 1", sorted[2].projectName)
    }
}
