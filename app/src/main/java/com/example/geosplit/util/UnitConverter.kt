package com.example.geosplit.util

import com.example.geosplit.model.MeasurementUnit
import java.math.BigDecimal
import java.math.MathContext

object UnitConverter {

    private val mc: MathContext = MathUtils.DEFAULT_MATH_CONTEXT

    /**
     * Converts a linear dimension in [unit] to millimeters (mm).
     */
    fun toMillimeters(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        return value.multiply(unit.factorToMm, mc)
    }

    /**
     * Converts a linear dimension from millimeters (mm) to [targetUnit].
     */
    fun fromMillimeters(valueMm: BigDecimal, targetUnit: MeasurementUnit): BigDecimal {
        return MathUtils.divide(valueMm, targetUnit.factorToMm, mc)
    }

    /**
     * Converts an area in [unit] to square millimeters (mm²).
     */
    fun toSquareMillimeters(value: BigDecimal, unit: MeasurementUnit): BigDecimal {
        return value.multiply(unit.factorToMm2, mc)
    }

    /**
     * Converts an area from square millimeters (mm²) to [targetUnit].
     */
    fun fromSquareMillimeters(valueMm2: BigDecimal, targetUnit: MeasurementUnit): BigDecimal {
        return MathUtils.divide(valueMm2, targetUnit.factorToMm2, mc)
    }

    /**
     * Converts a linear value directly from one unit to another preserving physical dimension.
     */
    fun convertLinear(value: BigDecimal, fromUnit: MeasurementUnit, toUnit: MeasurementUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        val mm = toMillimeters(value, fromUnit)
        return fromMillimeters(mm, toUnit)
    }

    /**
     * Converts an area value from one unit to another.
     */
    fun convertArea(value: BigDecimal, fromUnit: MeasurementUnit, toUnit: MeasurementUnit): BigDecimal {
        if (fromUnit == toUnit) return value
        val mm2 = toSquareMillimeters(value, fromUnit)
        return fromSquareMillimeters(mm2, toUnit)
    }
}
