package com.example.geosplit.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MathUtils {

    val DEFAULT_MATH_CONTEXT = MathContext(34, RoundingMode.HALF_UP)

    // High precision Pi (34+ digits)
    val PI_BIG_DECIMAL = BigDecimal("3.141592653589793238462643383279502884197169399375105820974944592307816406286")
    val TWO = BigDecimal("2")
    val HUNDRED = BigDecimal("100")
    val THREE_SIXTY = BigDecimal("360")

    /**
     * High-precision square root using Newton-Raphson algorithm.
     * Guaranteed to work reliably on all Android versions.
     */
    fun sqrtBigDecimal(value: BigDecimal, mc: MathContext = DEFAULT_MATH_CONTEXT): BigDecimal {
        if (value.signum() < 0) {
            throw IllegalArgumentException("Cannot calculate square root of a negative value: $value")
        }
        if (value.signum() == 0) {
            return BigDecimal.ZERO
        }

        val doubleVal = value.toDouble()
        var x = if (doubleVal.isFinite() && doubleVal > 0) {
            BigDecimal(Math.sqrt(doubleVal), mc)
        } else {
            BigDecimal.ONE.scaleByPowerOfTen(value.precision() / 2)
        }

        for (i in 0 until 50) {
            val next = x.add(value.divide(x, mc), mc).divide(TWO, mc)
            if (next == x || next.subtract(x).abs().compareTo(BigDecimal("1e-28")) <= 0) {
                return next
            }
            x = next
        }
        return x
    }

    /**
     * Safe division returning BigDecimal with DEFAULT_MATH_CONTEXT.
     */
    fun divide(a: BigDecimal, b: BigDecimal, mc: MathContext = DEFAULT_MATH_CONTEXT): BigDecimal {
        if (b.signum() == 0) {
            throw ArithmeticException("Division by zero")
        }
        return a.divide(b, mc)
    }

    /**
     * Safely parse a user-input string into BigDecimal.
     * Returns null if blank, malformed, or invalid.
     */
    fun parseBigDecimal(text: String): BigDecimal? {
        val trimmed = text.trim().replace(',', '.')
        if (trimmed.isEmpty()) return null
        return try {
            val bd = BigDecimal(trimmed)
            if (bd.toDouble().isFinite()) bd else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Format a BigDecimal with the specified number of decimal places (0 to 4).
     * Includes localized grouping separators (e.g. 1,500.00).
     */
    fun formatNumber(
        value: BigDecimal?,
        decimalPlaces: Int = 2,
        useGrouping: Boolean = true
    ): String {
        if (value == null) return "0"
        val clampedDecimals = decimalPlaces.coerceIn(0, 4)
        
        val rounded = value.setScale(clampedDecimals, RoundingMode.HALF_UP)
        
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }
        
        val pattern = buildString {
            if (useGrouping) append("#,##0") else append("0")
            if (clampedDecimals > 0) {
                append(".")
                repeat(clampedDecimals) { append("0") }
            }
        }
        
        val df = DecimalFormat(pattern, symbols)
        return df.format(rounded)
    }

    /**
     * Clean format that trims trailing zeros for input fields when switching units.
     */
    fun formatTrimmed(value: BigDecimal, maxDecimals: Int = 4): String {
        val clamped = maxDecimals.coerceIn(0, 8)
        val rounded = value.setScale(clamped, RoundingMode.HALF_UP)
        val stripped = rounded.stripTrailingZeros()
        return if (stripped.scale() < 0) {
            stripped.setScale(0).toPlainString()
        } else {
            stripped.toPlainString()
        }
    }
}
