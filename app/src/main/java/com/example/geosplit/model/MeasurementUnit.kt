package com.example.geosplit.model

import java.math.BigDecimal

enum class MeasurementUnit(
    val symbol: String,
    val areaSymbol: String,
    val factorToMm: BigDecimal,
    val factorToMm2: BigDecimal,
    val displayName: String
) {
    MILLIMETER(
        symbol = "mm",
        areaSymbol = "mm²",
        factorToMm = BigDecimal("1"),
        factorToMm2 = BigDecimal("1"),
        displayName = "Millimeters (mm)"
    ),
    CENTIMETER(
        symbol = "cm",
        areaSymbol = "cm²",
        factorToMm = BigDecimal("10"),
        factorToMm2 = BigDecimal("100"),
        displayName = "Centimeters (cm)"
    ),
    METER(
        symbol = "m",
        areaSymbol = "m²",
        factorToMm = BigDecimal("1000"),
        factorToMm2 = BigDecimal("1000000"),
        displayName = "Meters (m)"
    ),
    INCH(
        symbol = "in",
        areaSymbol = "in²",
        factorToMm = BigDecimal("25.4"),
        factorToMm2 = BigDecimal("645.16"),
        displayName = "Inches (in)"
    ),
    FOOT(
        symbol = "ft",
        areaSymbol = "ft²",
        factorToMm = BigDecimal("304.8"),
        factorToMm2 = BigDecimal("92903.04"),
        displayName = "Feet (ft)"
    )
}
