package com.example.geosplit.util

import java.math.BigDecimal

object ValidationUtils {

    const val MIN_SECTIONS = 2
    const val MAX_SECTIONS = 100

    const val MIN_GRID_DIMENSION = 1
    const val MAX_GRID_DIMENSION = 20
    const val MIN_GRID_TOTAL_SECTIONS = 2
    const val MAX_GRID_TOTAL_SECTIONS = 400

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    fun validateDimension(value: BigDecimal?): ValidationResult {
        if (value == null) {
            return ValidationResult.Invalid("Enter a valid dimension.")
        }
        if (value.signum() <= 0) {
            return ValidationResult.Invalid("Value must be greater than zero.")
        }
        return ValidationResult.Valid
    }

    fun validateSections(sections: Int?): ValidationResult {
        if (sections == null) {
            return ValidationResult.Invalid("Enter a whole number of sections from 2 to 100.")
        }
        if (sections < MIN_SECTIONS) {
            return ValidationResult.Invalid("Enter at least 2 sections.")
        }
        if (sections > MAX_SECTIONS) {
            return ValidationResult.Invalid("Sections cannot exceed 100.")
        }
        return ValidationResult.Valid
    }

    fun validateGrid(rows: Int?, columns: Int?): ValidationResult {
        if (rows == null || columns == null) {
            return ValidationResult.Invalid("Enter rows and columns between 1 and 20.")
        }
        if (rows < MIN_GRID_DIMENSION || rows > MAX_GRID_DIMENSION ||
            columns < MIN_GRID_DIMENSION || columns > MAX_GRID_DIMENSION
        ) {
            return ValidationResult.Invalid("Rows and columns must be between 1 and 20.")
        }
        val total = rows * columns
        if (total < MIN_GRID_TOTAL_SECTIONS) {
            return ValidationResult.Invalid("Grid must contain at least 2 sections.")
        }
        if (total > MAX_GRID_TOTAL_SECTIONS) {
            return ValidationResult.Invalid("Total grid sections cannot exceed 400.")
        }
        return ValidationResult.Valid
    }
}
