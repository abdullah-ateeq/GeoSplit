package com.example.geosplit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geosplit.calculator.GeoSplitCalculator
import com.example.geosplit.data.AppSettings
import com.example.geosplit.data.AppThemeMode
import com.example.geosplit.data.HistoryRepository
import com.example.geosplit.data.SettingsRepository
import com.example.geosplit.model.CalculationRecord
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.GeoSplitResult
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareGridPreset
import com.example.geosplit.model.SquareSplitMode
import com.example.geosplit.util.MathUtils
import com.example.geosplit.util.UnitConverter
import com.example.geosplit.util.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext

class GeoSplitViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val historyRepository = HistoryRepository(application)

    private val _uiState = MutableStateFlow(GeoSplitUiState())
    val uiState: StateFlow<GeoSplitUiState> = _uiState.asStateFlow()

    private var isInitializedWithSettings = false

    init {
        // Collect Settings
        viewModelScope.launch {
            settingsRepository.settingsFlow.collectLatest { settings ->
                _uiState.update { current ->
                    if (!isInitializedWithSettings) {
                        isInitializedWithSettings = true
                        current.copy(
                            settings = settings,
                            shape = settings.defaultShape,
                            sectionsText = settings.defaultSections.toString(),
                            unit = settings.defaultUnit,
                            decimalPlaces = settings.decimalPlaces
                        )
                    } else {
                        current.copy(settings = settings)
                    }
                }
                recalculate()
            }
        }

        // Collect History
        viewModelScope.launch {
            historyRepository.historyFlow.collectLatest { historyList ->
                _uiState.update { current ->
                    current.copy(
                        historyRecords = historyList,
                        filteredHistoryRecords = filterAndSortHistory(historyList, current.historySearchQuery)
                    )
                }
            }
        }

        // Initial calculation
        recalculate()
    }

    // Input handlers
    fun setProjectName(name: String) {
        _uiState.update { it.copy(projectName = name) }
    }

    fun setShape(shape: GeoShape) {
        _uiState.update { it.copy(shape = shape) }
        recalculate()
    }

    fun setRectangleSplitMode(mode: RectangleSplitMode) {
        _uiState.update { it.copy(rectangleSplitMode = mode) }
        recalculate()
    }

    fun setSquareSplitMode(mode: SquareSplitMode) {
        _uiState.update { it.copy(squareSplitMode = mode) }
        recalculate()
    }

    fun setSquareGridPreset(preset: SquareGridPreset) {
        _uiState.update {
            if (preset == SquareGridPreset.CUSTOM) {
                it.copy(squareGridPreset = preset)
            } else {
                it.copy(
                    squareGridPreset = preset,
                    rowsText = preset.rows.toString(),
                    columnsText = preset.columns.toString()
                )
            }
        }
        recalculate()
    }

    fun setLengthText(text: String) {
        _uiState.update { it.copy(lengthText = text) }
        recalculate()
    }

    fun setWidthText(text: String) {
        _uiState.update { it.copy(widthText = text) }
        recalculate()
    }

    fun setSideText(text: String) {
        _uiState.update { it.copy(sideText = text) }
        recalculate()
    }

    fun setRadiusText(text: String) {
        _uiState.update { it.copy(radiusText = text) }
        recalculate()
    }

    fun setTriangleBaseText(text: String) {
        _uiState.update { it.copy(triangleBaseText = text) }
        recalculate()
    }

    fun setTriangleHeightText(text: String) {
        _uiState.update { it.copy(triangleHeightText = text) }
        recalculate()
    }

    fun setSectionsText(text: String) {
        _uiState.update { it.copy(sectionsText = text) }
        recalculate()
    }

    fun setRowsText(text: String) {
        _uiState.update { it.copy(rowsText = text, squareGridPreset = SquareGridPreset.CUSTOM) }
        recalculate()
    }

    fun setColumnsText(text: String) {
        _uiState.update { it.copy(columnsText = text, squareGridPreset = SquareGridPreset.CUSTOM) }
        recalculate()
    }

    /**
     * Unit change preserves physical dimensions.
     * E.g. 120 cm -> 1.20 m.
     */
    fun setUnit(newUnit: MeasurementUnit) {
        val current = _uiState.value
        val oldUnit = current.unit
        if (oldUnit == newUnit) return

        fun convertInput(text: String): String {
            val parsed = MathUtils.parseBigDecimal(text) ?: return text
            val converted = UnitConverter.convertLinear(parsed, oldUnit, newUnit)
            return MathUtils.formatTrimmed(converted)
        }

        _uiState.update { state ->
            state.copy(
                unit = newUnit,
                lengthText = convertInput(state.lengthText),
                widthText = convertInput(state.widthText),
                sideText = convertInput(state.sideText),
                radiusText = convertInput(state.radiusText),
                triangleBaseText = convertInput(state.triangleBaseText),
                triangleHeightText = convertInput(state.triangleHeightText)
            )
        }
        recalculate()
    }

    fun setDecimalPlaces(places: Int) {
        val clamped = places.coerceIn(0, 4)
        _uiState.update { it.copy(decimalPlaces = clamped) }
        viewModelScope.launch {
            settingsRepository.updateDecimalPlaces(clamped)
        }
        recalculate()
    }

    fun resetCalculator() {
        _uiState.update { current ->
            current.copy(
                projectName = "",
                shape = GeoShape.RECTANGLE,
                lengthText = "120",
                widthText = "50",
                sectionsText = "4",
                rectangleSplitMode = RectangleSplitMode.ACROSS_LENGTH,
                squareSplitMode = SquareSplitMode.STRIPS,
                squareGridPreset = SquareGridPreset.GRID_4X4,
                rowsText = "4",
                columnsText = "4",
                sideText = "100",
                radiusText = "10",
                triangleBaseText = "100",
                triangleHeightText = "60"
            )
        }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val unit = state.unit
        val decimalPlaces = state.decimalPlaces

        when (state.shape) {
            GeoShape.RECTANGLE -> calculateRectangleState(state, unit, decimalPlaces)
            GeoShape.SQUARE -> calculateSquareState(state, unit, decimalPlaces)
            GeoShape.CIRCLE -> calculateCircleState(state, unit, decimalPlaces)
            GeoShape.TRIANGLE -> calculateTriangleState(state, unit, decimalPlaces)
        }
    }

    private fun calculateRectangleState(state: GeoSplitUiState, unit: MeasurementUnit, decimals: Int) {
        val length = MathUtils.parseBigDecimal(state.lengthText)
        val width = MathUtils.parseBigDecimal(state.widthText)
        val sections = state.sectionsText.toIntOrNull()

        if (state.lengthText.isBlank() || state.widthText.isBlank() || state.sectionsText.isBlank()) {
            setIncompleteState("Enter the required dimensions.")
            return
        }

        val lenVal = ValidationUtils.validateDimension(length)
        if (lenVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(lenVal.message)
            return
        }
        val widVal = ValidationUtils.validateDimension(width)
        if (widVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(widVal.message)
            return
        }
        val secVal = ValidationUtils.validateSections(sections)
        if (secVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(secVal.message)
            return
        }

        requireNotNull(length)
        requireNotNull(width)
        requireNotNull(sections)

        val lengthMm = UnitConverter.toMillimeters(length, unit)
        val widthMm = UnitConverter.toMillimeters(width, unit)

        val result = GeoSplitCalculator.calculateRectangle(
            lengthMm = lengthMm,
            widthMm = widthMm,
            sections = sections,
            splitMode = state.rectangleSplitMode
        )

        // Convert results back to active unit
        val totalAreaUnit = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, unit)
        val areaPerSectionUnit = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, unit)
        val secLenUnit = result.sectionLengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
        val secWidUnit = result.sectionWidthMm?.let { UnitConverter.fromMillimeters(it, unit) }
        val perimeterUnit = result.perimeterMm?.let { UnitConverter.fromMillimeters(it, unit) }

        val formattedTotalArea = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ${unit.areaSymbol}"
        val formattedAreaPerSec = "${MathUtils.formatNumber(areaPerSectionUnit, decimals)} ${unit.areaSymbol}"
        val formattedPercentage = "${MathUtils.formatNumber(result.sectionPercentage, decimals)}%"
        val formattedSecDim = if (secLenUnit != null && secWidUnit != null) {
            "${MathUtils.formatNumber(secLenUnit, decimals)} × ${MathUtils.formatNumber(secWidUnit, decimals)} ${unit.symbol}"
        } else ""
        val formattedPerimeter = perimeterUnit?.let {
            "${MathUtils.formatNumber(it, decimals)} ${unit.symbol}"
        } ?: ""

        val breakdown = generateBreakdown(totalAreaUnit, areaPerSectionUnit, result.sectionPercentage, sections, unit, decimals)
        val formulas = listOf(
            FormulaStep(
                title = "TOTAL AREA",
                formulaText = "Area = Length × Width",
                calculationText = "${MathUtils.formatNumber(length, decimals)} × ${MathUtils.formatNumber(width, decimals)} = $formattedTotalArea"
            ),
            FormulaStep(
                title = "AREA PER SECTION",
                formulaText = "Section Area = Total Area ÷ Sections",
                calculationText = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ÷ $sections = $formattedAreaPerSec"
            ),
            FormulaStep(
                title = "SECTION DIMENSIONS",
                formulaText = if (state.rectangleSplitMode == RectangleSplitMode.ACROSS_LENGTH) {
                    "Length ÷ Sections × Width"
                } else {
                    "Length × (Width ÷ Sections)"
                },
                calculationText = formattedSecDim
            ),
            FormulaStep(
                title = "PERIMETER",
                formulaText = "Perimeter = 2 × (Length + Width)",
                calculationText = "2 × (${MathUtils.formatNumber(length, decimals)} + ${MathUtils.formatNumber(width, decimals)}) = $formattedPerimeter"
            )
        )

        _uiState.update {
            it.copy(
                isInputValid = true,
                validationErrorMessage = null,
                inputHelperMessage = null,
                calculationResult = result,
                formattedTotalArea = formattedTotalArea,
                formattedAreaPerSection = formattedAreaPerSec,
                formattedSectionPercentage = formattedPercentage,
                formattedSectionDimensions = formattedSecDim,
                formattedPerimeterOrCircumference = formattedPerimeter,
                formattedAnglePerSection = "",
                sectionBreakdownItems = breakdown.first,
                isBreakdownTruncated = breakdown.second,
                formulaSteps = formulas
            )
        }
    }

    private fun calculateSquareState(state: GeoSplitUiState, unit: MeasurementUnit, decimals: Int) {
        val side = MathUtils.parseBigDecimal(state.sideText)
        if (state.sideText.isBlank()) {
            setIncompleteState("Enter the required dimensions.")
            return
        }

        val sideVal = ValidationUtils.validateDimension(side)
        if (sideVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(sideVal.message)
            return
        }
        requireNotNull(side)
        val sideMm = UnitConverter.toMillimeters(side, unit)

        if (state.squareSplitMode == SquareSplitMode.GRID) {
            val rows = state.rowsText.toIntOrNull()
            val columns = state.columnsText.toIntOrNull()

            if (state.rowsText.isBlank() || state.columnsText.isBlank()) {
                setIncompleteState("Enter rows and columns.")
                return
            }

            val gridVal = ValidationUtils.validateGrid(rows, columns)
            if (gridVal is ValidationUtils.ValidationResult.Invalid) {
                setInvalidState(gridVal.message)
                return
            }
            requireNotNull(rows)
            requireNotNull(columns)

            val result = GeoSplitCalculator.calculateSquareGrid(sideMm, rows, columns)
            val totalSections = rows * columns

            val totalAreaUnit = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, unit)
            val areaPerSectionUnit = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, unit)
            val secLenUnit = result.sectionLengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
            val secWidUnit = result.sectionWidthMm?.let { UnitConverter.fromMillimeters(it, unit) }
            val perimeterUnit = result.perimeterMm?.let { UnitConverter.fromMillimeters(it, unit) }

            val formattedTotalArea = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ${unit.areaSymbol}"
            val formattedAreaPerSec = "${MathUtils.formatNumber(areaPerSectionUnit, decimals)} ${unit.areaSymbol}"
            val formattedPercentage = "${MathUtils.formatNumber(result.sectionPercentage, decimals)}%"
            val formattedSecDim = if (secLenUnit != null && secWidUnit != null) {
                "${MathUtils.formatNumber(secWidUnit, decimals)} × ${MathUtils.formatNumber(secLenUnit, decimals)} ${unit.symbol}"
            } else ""
            val formattedPerimeter = perimeterUnit?.let {
                "${MathUtils.formatNumber(it, decimals)} ${unit.symbol}"
            } ?: ""

            val breakdown = generateBreakdown(totalAreaUnit, areaPerSectionUnit, result.sectionPercentage, totalSections, unit, decimals)
            val formulas = listOf(
                FormulaStep(
                    title = "TOTAL AREA",
                    formulaText = "Area = Side²",
                    calculationText = "${MathUtils.formatNumber(side, decimals)}² = $formattedTotalArea"
                ),
                FormulaStep(
                    title = "GRID SECTIONS",
                    formulaText = "Sections = Rows × Columns",
                    calculationText = "$rows × $columns = $totalSections sections"
                ),
                FormulaStep(
                    title = "SECTION SIZE",
                    formulaText = "Width = Side ÷ Cols, Height = Side ÷ Rows",
                    calculationText = "${MathUtils.formatNumber(side, decimals)} ÷ $columns = ${MathUtils.formatNumber(secWidUnit, decimals)}, ${MathUtils.formatNumber(side, decimals)} ÷ $rows = ${MathUtils.formatNumber(secLenUnit, decimals)}"
                ),
                FormulaStep(
                    title = "SECTION AREA",
                    formulaText = "Section Area = Section Width × Section Height",
                    calculationText = "${MathUtils.formatNumber(secWidUnit, decimals)} × ${MathUtils.formatNumber(secLenUnit, decimals)} = $formattedAreaPerSec"
                )
            )

            _uiState.update {
                it.copy(
                    isInputValid = true,
                    validationErrorMessage = null,
                    inputHelperMessage = null,
                    calculationResult = result,
                    formattedTotalArea = formattedTotalArea,
                    formattedAreaPerSection = formattedAreaPerSec,
                    formattedSectionPercentage = formattedPercentage,
                    formattedSectionDimensions = formattedSecDim,
                    formattedPerimeterOrCircumference = formattedPerimeter,
                    formattedAnglePerSection = "",
                    sectionBreakdownItems = breakdown.first,
                    isBreakdownTruncated = breakdown.second,
                    formulaSteps = formulas
                )
            }
        } else {
            // STRIPS
            val sections = state.sectionsText.toIntOrNull()
            if (state.sectionsText.isBlank()) {
                setIncompleteState("Enter the required dimensions.")
                return
            }
            val secVal = ValidationUtils.validateSections(sections)
            if (secVal is ValidationUtils.ValidationResult.Invalid) {
                setInvalidState(secVal.message)
                return
            }
            requireNotNull(sections)

            val result = GeoSplitCalculator.calculateSquareStrips(
                sideMm = sideMm,
                sections = sections,
                splitMode = state.rectangleSplitMode
            )

            val totalAreaUnit = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, unit)
            val areaPerSectionUnit = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, unit)
            val secLenUnit = result.sectionLengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
            val secWidUnit = result.sectionWidthMm?.let { UnitConverter.fromMillimeters(it, unit) }
            val perimeterUnit = result.perimeterMm?.let { UnitConverter.fromMillimeters(it, unit) }

            val formattedTotalArea = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ${unit.areaSymbol}"
            val formattedAreaPerSec = "${MathUtils.formatNumber(areaPerSectionUnit, decimals)} ${unit.areaSymbol}"
            val formattedPercentage = "${MathUtils.formatNumber(result.sectionPercentage, decimals)}%"
            val formattedSecDim = if (secLenUnit != null && secWidUnit != null) {
                "${MathUtils.formatNumber(secLenUnit, decimals)} × ${MathUtils.formatNumber(secWidUnit, decimals)} ${unit.symbol}"
            } else ""
            val formattedPerimeter = perimeterUnit?.let {
                "${MathUtils.formatNumber(it, decimals)} ${unit.symbol}"
            } ?: ""

            val breakdown = generateBreakdown(totalAreaUnit, areaPerSectionUnit, result.sectionPercentage, sections, unit, decimals)
            val formulas = listOf(
                FormulaStep(
                    title = "TOTAL AREA",
                    formulaText = "Area = Side²",
                    calculationText = "${MathUtils.formatNumber(side, decimals)}² = $formattedTotalArea"
                ),
                FormulaStep(
                    title = "AREA PER SECTION",
                    formulaText = "Section Area = Total Area ÷ Sections",
                    calculationText = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ÷ $sections = $formattedAreaPerSec"
                ),
                FormulaStep(
                    title = "SECTION SIZE",
                    formulaText = "Side × (Side ÷ Sections)",
                    calculationText = formattedSecDim
                ),
                FormulaStep(
                    title = "PERIMETER",
                    formulaText = "Perimeter = 4 × Side",
                    calculationText = "4 × ${MathUtils.formatNumber(side, decimals)} = $formattedPerimeter"
                )
            )

            _uiState.update {
                it.copy(
                    isInputValid = true,
                    validationErrorMessage = null,
                    inputHelperMessage = null,
                    calculationResult = result,
                    formattedTotalArea = formattedTotalArea,
                    formattedAreaPerSection = formattedAreaPerSec,
                    formattedSectionPercentage = formattedPercentage,
                    formattedSectionDimensions = formattedSecDim,
                    formattedPerimeterOrCircumference = formattedPerimeter,
                    formattedAnglePerSection = "",
                    sectionBreakdownItems = breakdown.first,
                    isBreakdownTruncated = breakdown.second,
                    formulaSteps = formulas
                )
            }
        }
    }

    private fun calculateCircleState(state: GeoSplitUiState, unit: MeasurementUnit, decimals: Int) {
        val radius = MathUtils.parseBigDecimal(state.radiusText)
        val sections = state.sectionsText.toIntOrNull()

        if (state.radiusText.isBlank() || state.sectionsText.isBlank()) {
            setIncompleteState("Enter the required dimensions.")
            return
        }

        val radVal = ValidationUtils.validateDimension(radius)
        if (radVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(radVal.message)
            return
        }
        val secVal = ValidationUtils.validateSections(sections)
        if (secVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(secVal.message)
            return
        }

        requireNotNull(radius)
        requireNotNull(sections)

        val radiusMm = UnitConverter.toMillimeters(radius, unit)
        val result = GeoSplitCalculator.calculateCircle(radiusMm, sections)

        val totalAreaUnit = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, unit)
        val areaPerSectionUnit = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, unit)
        val circumUnit = result.circumferenceMm?.let { UnitConverter.fromMillimeters(it, unit) }

        val formattedTotalArea = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ${unit.areaSymbol}"
        val formattedAreaPerSec = "${MathUtils.formatNumber(areaPerSectionUnit, decimals)} ${unit.areaSymbol}"
        val formattedPercentage = "${MathUtils.formatNumber(result.sectionPercentage, decimals)}%"
        val formattedCircumference = circumUnit?.let {
            "${MathUtils.formatNumber(it, decimals)} ${unit.symbol}"
        } ?: ""
        val formattedAngle = result.sectionAngleDegrees?.let {
            "${MathUtils.formatNumber(it, decimals)}°"
        } ?: ""

        val breakdown = generateBreakdown(totalAreaUnit, areaPerSectionUnit, result.sectionPercentage, sections, unit, decimals)
        val formulas = listOf(
            FormulaStep(
                title = "TOTAL AREA",
                formulaText = "Area = π × Radius²",
                calculationText = "π × ${MathUtils.formatNumber(radius, decimals)}² = $formattedTotalArea"
            ),
            FormulaStep(
                title = "AREA PER SECTION",
                formulaText = "Section Area = Total Area ÷ Sections",
                calculationText = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ÷ $sections = $formattedAreaPerSec"
            ),
            FormulaStep(
                title = "CENTRAL ANGLE PER SECTOR",
                formulaText = "Angle = 360° ÷ Sections",
                calculationText = "360° ÷ $sections = $formattedAngle"
            ),
            FormulaStep(
                title = "CIRCUMFERENCE",
                formulaText = "Circumference = 2 × π × Radius",
                calculationText = "2 × π × ${MathUtils.formatNumber(radius, decimals)} = $formattedCircumference"
            )
        )

        _uiState.update {
            it.copy(
                isInputValid = true,
                validationErrorMessage = null,
                inputHelperMessage = null,
                calculationResult = result,
                formattedTotalArea = formattedTotalArea,
                formattedAreaPerSection = formattedAreaPerSec,
                formattedSectionPercentage = formattedPercentage,
                formattedSectionDimensions = formattedAngle,
                formattedPerimeterOrCircumference = formattedCircumference,
                formattedAnglePerSection = formattedAngle,
                sectionBreakdownItems = breakdown.first,
                isBreakdownTruncated = breakdown.second,
                formulaSteps = formulas
            )
        }
    }

    private fun calculateTriangleState(state: GeoSplitUiState, unit: MeasurementUnit, decimals: Int) {
        val base = MathUtils.parseBigDecimal(state.triangleBaseText)
        val height = MathUtils.parseBigDecimal(state.triangleHeightText)
        val sections = state.sectionsText.toIntOrNull()

        if (state.triangleBaseText.isBlank() || state.triangleHeightText.isBlank() || state.sectionsText.isBlank()) {
            setIncompleteState("Enter the required dimensions.")
            return
        }

        val baseVal = ValidationUtils.validateDimension(base)
        if (baseVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(baseVal.message)
            return
        }
        val heightVal = ValidationUtils.validateDimension(height)
        if (heightVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(heightVal.message)
            return
        }
        val secVal = ValidationUtils.validateSections(sections)
        if (secVal is ValidationUtils.ValidationResult.Invalid) {
            setInvalidState(secVal.message)
            return
        }

        requireNotNull(base)
        requireNotNull(height)
        requireNotNull(sections)

        val baseMm = UnitConverter.toMillimeters(base, unit)
        val heightMm = UnitConverter.toMillimeters(height, unit)
        val result = GeoSplitCalculator.calculateTriangle(baseMm, heightMm, sections)

        val totalAreaUnit = UnitConverter.fromSquareMillimeters(result.totalAreaMm2, unit)
        val areaPerSectionUnit = UnitConverter.fromSquareMillimeters(result.areaPerSectionMm2, unit)

        val formattedTotalArea = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ${unit.areaSymbol}"
        val formattedAreaPerSec = "${MathUtils.formatNumber(areaPerSectionUnit, decimals)} ${unit.areaSymbol}"
        val formattedPercentage = "${MathUtils.formatNumber(result.sectionPercentage, decimals)}%"

        val breakdown = generateBreakdown(totalAreaUnit, areaPerSectionUnit, result.sectionPercentage, sections, unit, decimals)
        val formulas = listOf(
            FormulaStep(
                title = "TOTAL AREA",
                formulaText = "Area = (Base × Height) ÷ 2",
                calculationText = "(${MathUtils.formatNumber(base, decimals)} × ${MathUtils.formatNumber(height, decimals)}) ÷ 2 = $formattedTotalArea"
            ),
            FormulaStep(
                title = "AREA PER SECTION",
                formulaText = "Section Area = Total Area ÷ Sections",
                calculationText = "${MathUtils.formatNumber(totalAreaUnit, decimals)} ÷ $sections = $formattedAreaPerSec"
            ),
            FormulaStep(
                title = "EQUAL-AREA BOUNDARIES",
                formulaText = "y(k) = Height × √(k ÷ n) from apex",
                calculationText = "Parallel lines dividing area equally (spacing widens toward apex)"
            )
        )

        _uiState.update {
            it.copy(
                isInputValid = true,
                validationErrorMessage = null,
                inputHelperMessage = null,
                calculationResult = result,
                formattedTotalArea = formattedTotalArea,
                formattedAreaPerSection = formattedAreaPerSec,
                formattedSectionPercentage = formattedPercentage,
                formattedSectionDimensions = formattedAreaPerSec,
                formattedPerimeterOrCircumference = "",
                formattedAnglePerSection = "",
                sectionBreakdownItems = breakdown.first,
                isBreakdownTruncated = breakdown.second,
                formulaSteps = formulas
            )
        }
    }

    private fun generateBreakdown(
        totalArea: BigDecimal,
        areaPerSec: BigDecimal,
        percentage: BigDecimal,
        sections: Int,
        unit: MeasurementUnit,
        decimals: Int
    ): Pair<List<SectionBreakdownItem>, Boolean> {
        val limit = minOf(sections, 20)
        val items = mutableListOf<SectionBreakdownItem>()
        val mc = MathUtils.DEFAULT_MATH_CONTEXT

        for (k in 1..limit) {
            val allocated = areaPerSec.multiply(BigDecimal(k), mc)
            val remaining = totalArea.subtract(allocated, mc).max(BigDecimal.ZERO)
            val remainingFormatted = "${MathUtils.formatNumber(remaining, decimals)} ${unit.areaSymbol}"

            items.add(
                SectionBreakdownItem(
                    sectionNumber = k,
                    areaFormatted = "${MathUtils.formatNumber(areaPerSec, decimals)} ${unit.areaSymbol}",
                    percentageFormatted = "${MathUtils.formatNumber(percentage, decimals)}%",
                    remainingAfterFormatted = remainingFormatted
                )
            )
        }
        return Pair(items, sections > 20)
    }

    private fun setIncompleteState(message: String) {
        _uiState.update {
            it.copy(
                isInputValid = false,
                validationErrorMessage = null,
                inputHelperMessage = message,
                calculationResult = null,
                formattedTotalArea = "",
                formattedAreaPerSection = "",
                formattedSectionPercentage = "",
                formattedSectionDimensions = "",
                formattedPerimeterOrCircumference = "",
                formattedAnglePerSection = "",
                sectionBreakdownItems = emptyList(),
                formulaSteps = emptyList()
            )
        }
    }

    private fun setInvalidState(errorMessage: String) {
        _uiState.update {
            it.copy(
                isInputValid = false,
                validationErrorMessage = errorMessage,
                inputHelperMessage = null,
                calculationResult = null,
                formattedTotalArea = "",
                formattedAreaPerSection = "",
                formattedSectionPercentage = "",
                formattedSectionDimensions = "",
                formattedPerimeterOrCircumference = "",
                formattedAnglePerSection = "",
                sectionBreakdownItems = emptyList(),
                formulaSteps = emptyList()
            )
        }
    }

    // History and Favorites
    fun saveCalculation() {
        val state = _uiState.value
        val result = state.calculationResult ?: return
        val baseName = if (state.projectName.isBlank()) "Untitled Split" else state.projectName

        val dimensionsSummary = when (state.shape) {
            GeoShape.RECTANGLE -> "${state.lengthText} × ${state.widthText} ${state.unit.symbol}"
            GeoShape.SQUARE -> "${state.sideText} × ${state.sideText} ${state.unit.symbol}"
            GeoShape.CIRCLE -> "Radius: ${state.radiusText} ${state.unit.symbol}"
            GeoShape.TRIANGLE -> "Base: ${state.triangleBaseText}, Height: ${state.triangleHeightText} ${state.unit.symbol}"
        }

        val record = CalculationRecord(
            projectName = baseName,
            shape = state.shape,
            rectangleSplitMode = if (state.shape == GeoShape.RECTANGLE) state.rectangleSplitMode else null,
            squareSplitMode = if (state.shape == GeoShape.SQUARE) state.squareSplitMode else null,
            squareGridPreset = if (state.shape == GeoShape.SQUARE && state.squareSplitMode == SquareSplitMode.GRID) state.squareGridPreset else null,
            unit = state.unit,
            lengthText = state.lengthText,
            widthText = state.widthText,
            sideText = state.sideText,
            radiusText = state.radiusText,
            triangleBaseText = state.triangleBaseText,
            triangleHeightText = state.triangleHeightText,
            sectionsText = state.sectionsText,
            rowsText = state.rowsText,
            columnsText = state.columnsText,
            sectionCount = result.totalSections,
            dimensionsSummary = dimensionsSummary,
            totalAreaFormatted = state.formattedTotalArea,
            areaPerSectionFormatted = state.formattedAreaPerSection,
            sectionDimensionsFormatted = state.formattedSectionDimensions,
            sectionPercentageFormatted = state.formattedSectionPercentage
        )

        viewModelScope.launch {
            historyRepository.saveCalculation(record)
            showSnackbar("Calculation saved to history")
        }
    }

    fun toggleFavorite(recordId: String) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(recordId)
        }
    }

    fun duplicateCalculation(record: CalculationRecord) {
        viewModelScope.launch {
            historyRepository.duplicateCalculation(record)
            showSnackbar("Duplicated '${record.projectName}'")
        }
    }

    fun deleteCalculation(recordId: String) {
        viewModelScope.launch {
            historyRepository.deleteCalculation(recordId)
            showSnackbar("Calculation deleted")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            showSnackbar("History cleared")
        }
    }

    fun restoreCalculation(record: CalculationRecord) {
        _uiState.update { current ->
            current.copy(
                projectName = record.projectName,
                shape = record.shape,
                rectangleSplitMode = record.rectangleSplitMode ?: current.rectangleSplitMode,
                squareSplitMode = record.squareSplitMode ?: current.squareSplitMode,
                squareGridPreset = record.squareGridPreset ?: current.squareGridPreset,
                unit = record.unit,
                lengthText = record.lengthText.ifEmpty { current.lengthText },
                widthText = record.widthText.ifEmpty { current.widthText },
                sideText = record.sideText.ifEmpty { current.sideText },
                radiusText = record.radiusText.ifEmpty { current.radiusText },
                triangleBaseText = record.triangleBaseText.ifEmpty { current.triangleBaseText },
                triangleHeightText = record.triangleHeightText.ifEmpty { current.triangleHeightText },
                sectionsText = record.sectionsText.ifEmpty { current.sectionsText },
                rowsText = record.rowsText.ifEmpty { current.rowsText },
                columnsText = record.columnsText.ifEmpty { current.columnsText }
            )
        }
        recalculate()
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.update { current ->
            current.copy(
                historySearchQuery = query,
                filteredHistoryRecords = filterAndSortHistory(current.historyRecords, query)
            )
        }
    }

    private fun filterAndSortHistory(records: List<CalculationRecord>, query: String): List<CalculationRecord> {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            records
        } else {
            records.filter {
                it.projectName.contains(trimmed, ignoreCase = true) ||
                it.shape.displayName.contains(trimmed, ignoreCase = true)
            }
        }
        // Favorites appear first
        return filtered.sortedWith(
            compareByDescending<CalculationRecord> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )
    }

    // Share & Copy
    fun getShareableResultText(): String {
        val state = _uiState.value
        val result = state.calculationResult ?: return ""
        val projectName = if (state.projectName.isBlank()) "Untitled Split" else state.projectName

        return buildString {
            appendLine("GeoSplit")
            appendLine("Project: $projectName")
            appendLine("Shape: ${state.shape.displayName}")
            when (state.shape) {
                GeoShape.RECTANGLE -> {
                    appendLine("Size: ${state.lengthText} × ${state.widthText} ${state.unit.symbol}")
                    appendLine("Sections: ${result.totalSections}")
                    appendLine("Split: ${state.rectangleSplitMode.displayName}")
                    appendLine("Total Area: ${state.formattedTotalArea}")
                    appendLine("Area Per Section: ${state.formattedAreaPerSection}")
                    appendLine("Section Size: ${state.formattedSectionDimensions}")
                    appendLine("Section Percentage: ${state.formattedSectionPercentage}")
                }
                GeoShape.SQUARE -> {
                    appendLine("Side: ${state.sideText} ${state.unit.symbol}")
                    if (state.squareSplitMode == SquareSplitMode.GRID) {
                        appendLine("Grid: ${result.rows} × ${result.columns} (${result.totalSections} sections)")
                    } else {
                        appendLine("Sections: ${result.totalSections} (${state.squareSplitMode.displayName})")
                    }
                    appendLine("Total Area: ${state.formattedTotalArea}")
                    appendLine("Area Per Section: ${state.formattedAreaPerSection}")
                    appendLine("Section Size: ${state.formattedSectionDimensions}")
                    appendLine("Section Percentage: ${state.formattedSectionPercentage}")
                }
                GeoShape.CIRCLE -> {
                    appendLine("Radius: ${state.radiusText} ${state.unit.symbol}")
                    appendLine("Sections: ${result.totalSections} sectors")
                    appendLine("Total Area: ${state.formattedTotalArea}")
                    appendLine("Area Per Section: ${state.formattedAreaPerSection}")
                    appendLine("Central Angle: ${state.formattedAnglePerSection}")
                    appendLine("Circumference: ${state.formattedPerimeterOrCircumference}")
                    appendLine("Section Percentage: ${state.formattedSectionPercentage}")
                }
                GeoShape.TRIANGLE -> {
                    appendLine("Base: ${state.triangleBaseText} ${state.unit.symbol}")
                    appendLine("Height: ${state.triangleHeightText} ${state.unit.symbol}")
                    appendLine("Sections: ${result.totalSections} (Equal-Area Parallel Slices)")
                    appendLine("Total Area: ${state.formattedTotalArea}")
                    appendLine("Area Per Section: ${state.formattedAreaPerSection}")
                    appendLine("Section Percentage: ${state.formattedSectionPercentage}")
                }
            }
        }.trimEnd()
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Settings actions
    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateDynamicColor(enabled) }
    }

    fun updateDefaultShape(shape: GeoShape) {
        viewModelScope.launch { settingsRepository.updateDefaultShape(shape) }
    }

    fun updateDefaultSections(sections: Int) {
        viewModelScope.launch { settingsRepository.updateDefaultSections(sections) }
    }

    fun updateDefaultUnit(unit: MeasurementUnit) {
        viewModelScope.launch { settingsRepository.updateDefaultUnit(unit) }
    }

    fun updateShowDiagram(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowDiagram(enabled) }
    }

    fun updateShowFormula(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowFormula(enabled) }
    }

    fun updateShowSectionBreakdown(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowSectionBreakdown(enabled) }
    }

    fun updateShowPercentages(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowPercentages(enabled) }
    }

    fun updateShowDimensions(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateShowDimensions(enabled) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHapticFeedback(enabled) }
    }

    fun updateConfirmDelete(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateConfirmDelete(enabled) }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetSettings()
            showSnackbar("Settings reset to defaults")
        }
    }
}
