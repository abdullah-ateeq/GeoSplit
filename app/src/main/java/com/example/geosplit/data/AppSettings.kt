package com.example.geosplit.data

import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val defaultShape: GeoShape = GeoShape.RECTANGLE,
    val defaultSections: Int = 4,
    val defaultUnit: MeasurementUnit = MeasurementUnit.CENTIMETER,
    val decimalPlaces: Int = 2,
    val showDiagram: Boolean = true,
    val showFormula: Boolean = true,
    val showSectionBreakdown: Boolean = true,
    val showPercentages: Boolean = true,
    val showDimensions: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
