package com.example.geosplit.viewmodel

import com.example.geosplit.data.AppSettings
import com.example.geosplit.model.CalculationRecord
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.GeoSplitResult
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareGridPreset
import com.example.geosplit.model.SquareSplitMode
import java.math.BigDecimal

data class SectionBreakdownItem(
    val sectionNumber: Int,
    val areaFormatted: String,
    val percentageFormatted: String,
    val remainingAfterFormatted: String? = null
)

data class FormulaStep(
    val title: String,
    val formulaText: String,
    val calculationText: String
)

data class GeoSplitUiState(
    // Inputs
    val projectName: String = "",
    val shape: GeoShape = GeoShape.RECTANGLE,
    val lengthText: String = "120",
    val widthText: String = "50",
    val sideText: String = "100",
    val radiusText: String = "10",
    val triangleBaseText: String = "100",
    val triangleHeightText: String = "60",
    val sectionsText: String = "4",
    val rowsText: String = "4",
    val columnsText: String = "4",
    val rectangleSplitMode: RectangleSplitMode = RectangleSplitMode.ACROSS_LENGTH,
    val squareSplitMode: SquareSplitMode = SquareSplitMode.STRIPS,
    val squareGridPreset: SquareGridPreset = SquareGridPreset.GRID_4X4,
    val unit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val decimalPlaces: Int = 2,

    // Validation
    val isInputValid: Boolean = true,
    val validationErrorMessage: String? = null,
    val inputHelperMessage: String? = null,

    // Calculation Result
    val calculationResult: GeoSplitResult? = null,

    // Formatted outputs in active unit
    val formattedTotalArea: String = "",
    val formattedAreaPerSection: String = "",
    val formattedSectionPercentage: String = "",
    val formattedPerimeterOrCircumference: String = "",
    val formattedSectionDimensions: String = "",
    val formattedAnglePerSection: String = "",

    // Breakdown & Formula
    val sectionBreakdownItems: List<SectionBreakdownItem> = emptyList(),
    val isBreakdownTruncated: Boolean = false,
    val formulaSteps: List<FormulaStep> = emptyList(),

    // History & Favorites
    val historyRecords: List<CalculationRecord> = emptyList(),
    val historySearchQuery: String = "",
    val filteredHistoryRecords: List<CalculationRecord> = emptyList(),

    // Settings
    val settings: AppSettings = AppSettings(),

    // UI feedback
    val snackbarMessage: String? = null
)
