package com.example.geosplit.model

enum class RectangleSplitMode(val displayName: String) {
    ACROSS_LENGTH("Across Length"),
    ACROSS_WIDTH("Across Width")
}

enum class SquareSplitMode(val displayName: String) {
    STRIPS("Equal Strips"),
    GRID("Grid")
}

enum class SquareGridPreset(val rows: Int, val columns: Int, val label: String) {
    GRID_2X2(2, 2, "2 × 2"),
    GRID_3X3(3, 3, "3 × 3"),
    GRID_4X4(4, 4, "4 × 4"),
    GRID_5X5(5, 5, "5 × 5"),
    GRID_10X10(10, 10, "10 × 10"),
    CUSTOM(0, 0, "Custom")
}
