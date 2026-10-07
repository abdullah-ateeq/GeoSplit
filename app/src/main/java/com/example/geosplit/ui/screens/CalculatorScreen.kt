package com.example.geosplit.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareGridPreset
import com.example.geosplit.model.SquareSplitMode
import com.example.geosplit.ui.components.FormulaCard
import com.example.geosplit.ui.components.HapticFeedbackHelper
import com.example.geosplit.ui.components.PrimaryResultCard
import com.example.geosplit.ui.components.SecondaryResultsSection
import com.example.geosplit.ui.components.SectionBreakdownCard
import com.example.geosplit.ui.components.ShapeSelector
import com.example.geosplit.ui.components.TechnicalDiagram
import com.example.geosplit.viewmodel.GeoSplitViewModel

@Composable
fun CalculatorScreen(
    viewModel: GeoSplitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            HeaderSection()

            // Optional Project Reference Name
            OutlinedTextField(
                value = uiState.projectName,
                onValueChange = { viewModel.setProjectName(it) },
                label = { Text("Project / Part Name (optional)") },
                placeholder = { Text("e.g. Floor Panels, Garden Bed, Workshop Sheet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Shape Selector
            ShapeSelector(
                selectedShape = uiState.shape,
                onShapeSelected = {
                    HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    viewModel.setShape(it)
                }
            )

            // Unit & Decimal Precision Row
            UnitAndPrecisionRow(
                activeUnit = uiState.unit,
                activeDecimals = uiState.decimalPlaces,
                onUnitChanged = {
                    HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    viewModel.setUnit(it)
                },
                onDecimalsChanged = {
                    HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    viewModel.setDecimalPlaces(it)
                }
            )

            // Shape Specific Dimensions Input
            ShapeDimensionInputs(
                shape = uiState.shape,
                uiState = uiState,
                viewModel = viewModel,
                unitSymbol = uiState.unit.symbol
            )

            // Section count & Split controls
            SectionAndSplitControls(
                shape = uiState.shape,
                uiState = uiState,
                viewModel = viewModel,
                onHaptic = { HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback) }
            )

            // Validation & Helper notices
            if (!uiState.isInputValid) {
                val errorMsg = uiState.validationErrorMessage ?: uiState.inputHelperMessage ?: ""
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Results Section (Visible when valid)
            if (uiState.isInputValid && uiState.calculationResult != null) {
                // Technical Diagram
                if (uiState.settings.showDiagram) {
                    val lengthRatio = calculateLengthRatio(uiState)
                    TechnicalDiagram(
                        shape = uiState.shape,
                        rectangleSplitMode = uiState.rectangleSplitMode,
                        squareSplitMode = uiState.squareSplitMode,
                        result = uiState.calculationResult,
                        lengthRatio = lengthRatio
                    )
                }

                // Primary Area-Per-Section Result Card
                PrimaryResultCard(
                    areaPerSectionFormatted = uiState.formattedAreaPerSection,
                    totalSections = uiState.calculationResult?.totalSections ?: 0,
                    sectionDimensionsFormatted = uiState.formattedSectionDimensions,
                    sectionPercentageFormatted = uiState.formattedSectionPercentage,
                    showDimensions = uiState.settings.showDimensions,
                    showPercentages = uiState.settings.showPercentages
                )

                // Secondary Metrics Card
                SecondaryResultsSection(
                    shape = uiState.shape,
                    totalAreaFormatted = uiState.formattedTotalArea,
                    perimeterOrCircumferenceFormatted = uiState.formattedPerimeterOrCircumference,
                    sectionDimensionsFormatted = uiState.formattedSectionDimensions,
                    anglePerSectionFormatted = uiState.formattedAnglePerSection
                )

                // Section Breakdown (Collapsible)
                if (uiState.settings.showSectionBreakdown && uiState.sectionBreakdownItems.isNotEmpty()) {
                    SectionBreakdownCard(
                        items = uiState.sectionBreakdownItems,
                        isTruncated = uiState.isBreakdownTruncated,
                        totalSections = uiState.calculationResult?.totalSections ?: 0
                    )
                }

                // Dynamic Formula Card (Collapsible)
                if (uiState.settings.showFormula && uiState.formulaSteps.isNotEmpty()) {
                    FormulaCard(steps = uiState.formulaSteps)
                }

                // Primary Actions: Copy, Share, Save, Reset
                ActionButtonsRow(
                    onCopy = {
                        val text = viewModel.getShareableResultText()
                        copyToClipboard(context, text)
                        viewModel.showSnackbar("Result copied")
                        HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    },
                    onShare = {
                        val text = viewModel.getShareableResultText()
                        shareText(context, text)
                        HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    },
                    onSave = {
                        viewModel.saveCalculation()
                        HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    },
                    onReset = {
                        viewModel.resetCalculator()
                        HapticFeedbackHelper.performLightHaptic(view, uiState.settings.hapticFeedback)
                    }
                )
            }

            // Construction & Geometry Disclaimer
            DisclaimerNotice()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "GEOSPLIT",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Divide geometric areas into equal sections.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UnitAndPrecisionRow(
    activeUnit: MeasurementUnit,
    activeDecimals: Int,
    onUnitChanged: (MeasurementUnit) -> Unit,
    onDecimalsChanged: (Int) -> Unit
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var decimalsMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Unit Selector
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { unitMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Unit: ${activeUnit.symbol} (${activeUnit.areaSymbol})")
            }

            DropdownMenu(
                expanded = unitMenuExpanded,
                onDismissRequest = { unitMenuExpanded = false }
            ) {
                MeasurementUnit.values().forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName) },
                        onClick = {
                            unitMenuExpanded = false
                            onUnitChanged(unit)
                        }
                    )
                }
            }
        }

        // Decimals Precision Selector
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { decimalsMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Decimals: $activeDecimals")
            }

            DropdownMenu(
                expanded = decimalsMenuExpanded,
                onDismissRequest = { decimalsMenuExpanded = false }
            ) {
                (0..4).forEach { decimals ->
                    DropdownMenuItem(
                        text = { Text("$decimals decimal places") },
                        onClick = {
                            decimalsMenuExpanded = false
                            onDecimalsChanged(decimals)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShapeDimensionInputs(
    shape: GeoShape,
    uiState: com.example.geosplit.viewmodel.GeoSplitUiState,
    viewModel: GeoSplitViewModel,
    unitSymbol: String
) {
    when (shape) {
        GeoShape.RECTANGLE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.lengthText,
                    onValueChange = { viewModel.setLengthText(it) },
                    label = { Text("Length") },
                    trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.widthText,
                    onValueChange = { viewModel.setWidthText(it) },
                    label = { Text("Width") },
                    trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
        GeoShape.SQUARE -> {
            OutlinedTextField(
                value = uiState.sideText,
                onValueChange = { viewModel.setSideText(it) },
                label = { Text("Side") },
                trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(10.dp)
            )
        }
        GeoShape.CIRCLE -> {
            OutlinedTextField(
                value = uiState.radiusText,
                onValueChange = { viewModel.setRadiusText(it) },
                label = { Text("Radius") },
                trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(10.dp)
            )
        }
        GeoShape.TRIANGLE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.triangleBaseText,
                    onValueChange = { viewModel.setTriangleBaseText(it) },
                    label = { Text("Base") },
                    trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.triangleHeightText,
                    onValueChange = { viewModel.setTriangleHeightText(it) },
                    label = { Text("Height") },
                    trailingIcon = { Text(unitSymbol, modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionAndSplitControls(
    shape: GeoShape,
    uiState: com.example.geosplit.viewmodel.GeoSplitUiState,
    viewModel: GeoSplitViewModel,
    onHaptic: () -> Unit
) {
    when (shape) {
        GeoShape.RECTANGLE -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.sectionsText,
                    onValueChange = { viewModel.setSectionsText(it) },
                    label = { Text("Number of Sections (2–100)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.rectangleSplitMode == RectangleSplitMode.ACROSS_LENGTH,
                        onClick = {
                            onHaptic()
                            viewModel.setRectangleSplitMode(RectangleSplitMode.ACROSS_LENGTH)
                        },
                        label = { Text("Across Length") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = uiState.rectangleSplitMode == RectangleSplitMode.ACROSS_WIDTH,
                        onClick = {
                            onHaptic()
                            viewModel.setRectangleSplitMode(RectangleSplitMode.ACROSS_WIDTH)
                        },
                        label = { Text("Across Width") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        GeoShape.SQUARE -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mode selector: Equal Strips vs Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.squareSplitMode == SquareSplitMode.STRIPS,
                        onClick = {
                            onHaptic()
                            viewModel.setSquareSplitMode(SquareSplitMode.STRIPS)
                        },
                        label = { Text("Equal Strips") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = uiState.squareSplitMode == SquareSplitMode.GRID,
                        onClick = {
                            onHaptic()
                            viewModel.setSquareSplitMode(SquareSplitMode.GRID)
                        },
                        label = { Text("Grid") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (uiState.squareSplitMode == SquareSplitMode.STRIPS) {
                    OutlinedTextField(
                        value = uiState.sectionsText,
                        onValueChange = { viewModel.setSectionsText(it) },
                        label = { Text("Number of Sections (2–100)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    // Grid Presets
                    Text(
                        text = "Grid Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            SquareGridPreset.GRID_2X2,
                            SquareGridPreset.GRID_3X3,
                            SquareGridPreset.GRID_4X4,
                            SquareGridPreset.GRID_5X5,
                            SquareGridPreset.GRID_10X10
                        ).forEach { preset ->
                            FilterChip(
                                selected = uiState.squareGridPreset == preset,
                                onClick = {
                                    onHaptic()
                                    viewModel.setSquareGridPreset(preset)
                                },
                                label = { Text(preset.label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Custom Rows & Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.rowsText,
                            onValueChange = { viewModel.setRowsText(it) },
                            label = { Text("Rows (1–20)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = uiState.columnsText,
                            onValueChange = { viewModel.setColumnsText(it) },
                            label = { Text("Columns (1–20)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }
        GeoShape.CIRCLE, GeoShape.TRIANGLE -> {
            OutlinedTextField(
                value = uiState.sectionsText,
                onValueChange = { viewModel.setSectionsText(it) },
                label = { Text("Number of Sections (2–100)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }

            OutlinedButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset")
            }
        }
    }
}

@Composable
private fun DisclaimerNotice() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GeoSplit performs basic geometric calculations only. Verify measurements, tolerances, material specifications, and project requirements before construction or fabrication.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

private fun calculateLengthRatio(uiState: com.example.geosplit.viewmodel.GeoSplitUiState): Float {
    return when (uiState.shape) {
        GeoShape.RECTANGLE -> {
            val l = uiState.lengthText.toFloatOrNull() ?: 120f
            val w = uiState.widthText.toFloatOrNull() ?: 50f
            if (w > 0) l / w else 1f
        }
        GeoShape.SQUARE, GeoShape.CIRCLE, GeoShape.TRIANGLE -> 1f
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("GeoSplit Result", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "GeoSplit Calculation Result")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share GeoSplit Result"))
}
