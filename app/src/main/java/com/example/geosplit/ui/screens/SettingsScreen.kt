package com.example.geosplit.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geosplit.data.AppThemeMode
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.viewmodel.GeoSplitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GeoSplitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val snackbarHostState = remember { SnackbarHostState() }

    var showResetConfirm by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Mode
            SettingsSectionHeader("APPEARANCE")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.SYSTEM,
                        onClick = { viewModel.updateThemeMode(AppThemeMode.SYSTEM) },
                        label = { Text("System") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.LIGHT,
                        onClick = { viewModel.updateThemeMode(AppThemeMode.LIGHT) },
                        label = { Text("Light") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = settings.themeMode == AppThemeMode.DARK,
                        onClick = { viewModel.updateThemeMode(AppThemeMode.DARK) },
                        label = { Text("Dark") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SettingsToggleRow(
                title = "Dynamic Color",
                subtitle = "Use Material You system wallpaper colors (Android 12+)",
                checked = settings.dynamicColor,
                onCheckedChange = { viewModel.updateDynamicColor(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Defaults
            SettingsSectionHeader("CALCULATOR DEFAULTS")

            // Default Shape
            var shapeMenuExpanded by remember { mutableStateOf(false) }
            Box {
                SettingsClickableRow(
                    title = "Default Shape",
                    value = settings.defaultShape.displayName,
                    onClick = { shapeMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = shapeMenuExpanded,
                    onDismissRequest = { shapeMenuExpanded = false }
                ) {
                    GeoShape.values().forEach { shape ->
                        DropdownMenuItem(
                            text = { Text(shape.displayName) },
                            onClick = {
                                shapeMenuExpanded = false
                                viewModel.updateDefaultShape(shape)
                            }
                        )
                    }
                }
            }

            // Default Unit
            var unitMenuExpanded by remember { mutableStateOf(false) }
            Box {
                SettingsClickableRow(
                    title = "Default Unit",
                    value = settings.defaultUnit.displayName,
                    onClick = { unitMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    MeasurementUnit.values().forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName) },
                            onClick = {
                                unitMenuExpanded = false
                                viewModel.updateDefaultUnit(unit)
                            }
                        )
                    }
                }
            }

            // Decimal Places
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Decimal Places: ${settings.decimalPlaces}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..4).forEach { places ->
                        FilterChip(
                            selected = settings.decimalPlaces == places,
                            onClick = { viewModel.setDecimalPlaces(places) },
                            label = { Text("$places") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Display Preferences
            SettingsSectionHeader("DISPLAY & CONTROLS")

            SettingsToggleRow(
                title = "Show Diagram",
                subtitle = "Display technical division diagram",
                checked = settings.showDiagram,
                onCheckedChange = { viewModel.updateShowDiagram(it) }
            )

            SettingsToggleRow(
                title = "Show Formula",
                subtitle = "Display step-by-step formula breakdown",
                checked = settings.showFormula,
                onCheckedChange = { viewModel.updateShowFormula(it) }
            )

            SettingsToggleRow(
                title = "Show Section Breakdown",
                subtitle = "Display list of individual sections and remaining areas",
                checked = settings.showSectionBreakdown,
                onCheckedChange = { viewModel.updateShowSectionBreakdown(it) }
            )

            SettingsToggleRow(
                title = "Show Section Percentages",
                subtitle = "Display section share of total area",
                checked = settings.showPercentages,
                onCheckedChange = { viewModel.updateShowPercentages(it) }
            )

            SettingsToggleRow(
                title = "Show Section Dimensions",
                subtitle = "Display individual cut length and width",
                checked = settings.showDimensions,
                onCheckedChange = { viewModel.updateShowDimensions(it) }
            )

            SettingsToggleRow(
                title = "Haptic Feedback",
                subtitle = "Subtle tactile response on taps",
                checked = settings.hapticFeedback,
                onCheckedChange = { viewModel.updateHapticFeedback(it) }
            )

            SettingsToggleRow(
                title = "Confirm Delete",
                subtitle = "Show confirmation dialog before deleting calculations",
                checked = settings.confirmDelete,
                onCheckedChange = { viewModel.updateConfirmDelete(it) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Danger & Reset Zone
            SettingsSectionHeader("DATA MANAGEMENT")

            OutlinedButton(
                onClick = { showResetConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset Settings to Default")
            }

            OutlinedButton(
                onClick = { showClearHistoryConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Clear Calculation History")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset confirmation dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences to default values? Your saved calculation history will be preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetConfirm = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history confirmation dialog
    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear All History") },
            text = { Text("Are you sure you want to delete all saved calculations? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryConfirm = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
