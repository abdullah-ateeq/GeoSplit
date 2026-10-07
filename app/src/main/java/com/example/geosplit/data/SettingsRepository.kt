package com.example.geosplit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "geosplit_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DEFAULT_SHAPE = stringPreferencesKey("default_shape")
        val DEFAULT_SECTIONS = intPreferencesKey("default_sections")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val SHOW_DIAGRAM = booleanPreferencesKey("show_diagram")
        val SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val SHOW_SECTION_BREAKDOWN = booleanPreferencesKey("show_section_breakdown")
        val SHOW_PERCENTAGES = booleanPreferencesKey("show_percentages")
        val SHOW_DIMENSIONS = booleanPreferencesKey("show_dimensions")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val shapeStr = preferences[PreferencesKeys.DEFAULT_SHAPE] ?: GeoShape.RECTANGLE.name
        val defaultShape = try {
            GeoShape.valueOf(shapeStr)
        } catch (_: Exception) {
            GeoShape.RECTANGLE
        }

        val unitStr = preferences[PreferencesKeys.DEFAULT_UNIT] ?: MeasurementUnit.CENTIMETER.name
        val defaultUnit = try {
            MeasurementUnit.valueOf(unitStr)
        } catch (_: Exception) {
            MeasurementUnit.CENTIMETER
        }

        AppSettings(
            themeMode = themeMode,
            dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false,
            defaultShape = defaultShape,
            defaultSections = (preferences[PreferencesKeys.DEFAULT_SECTIONS] ?: 4).coerceIn(2, 100),
            defaultUnit = defaultUnit,
            decimalPlaces = (preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2).coerceIn(0, 4),
            showDiagram = preferences[PreferencesKeys.SHOW_DIAGRAM] ?: true,
            showFormula = preferences[PreferencesKeys.SHOW_FORMULA] ?: true,
            showSectionBreakdown = preferences[PreferencesKeys.SHOW_SECTION_BREAKDOWN] ?: true,
            showPercentages = preferences[PreferencesKeys.SHOW_PERCENTAGES] ?: true,
            showDimensions = preferences[PreferencesKeys.SHOW_DIMENSIONS] ?: true,
            hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            confirmDelete = preferences[PreferencesKeys.CONFIRM_DELETE] ?: true
        )
    }

    suspend fun updateThemeMode(themeMode: AppThemeMode) {
        context.settingsDataStore.edit { it[PreferencesKeys.THEME_MODE] = themeMode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateDefaultShape(shape: GeoShape) {
        context.settingsDataStore.edit { it[PreferencesKeys.DEFAULT_SHAPE] = shape.name }
    }

    suspend fun updateDefaultSections(sections: Int) {
        context.settingsDataStore.edit {
            it[PreferencesKeys.DEFAULT_SECTIONS] = sections.coerceIn(2, 100)
        }
    }

    suspend fun updateDefaultUnit(unit: MeasurementUnit) {
        context.settingsDataStore.edit { it[PreferencesKeys.DEFAULT_UNIT] = unit.name }
    }

    suspend fun updateDecimalPlaces(places: Int) {
        context.settingsDataStore.edit {
            it[PreferencesKeys.DECIMAL_PLACES] = places.coerceIn(0, 4)
        }
    }

    suspend fun updateShowDiagram(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.SHOW_DIAGRAM] = enabled }
    }

    suspend fun updateShowFormula(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.SHOW_FORMULA] = enabled }
    }

    suspend fun updateShowSectionBreakdown(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.SHOW_SECTION_BREAKDOWN] = enabled }
    }

    suspend fun updateShowPercentages(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.SHOW_PERCENTAGES] = enabled }
    }

    suspend fun updateShowDimensions(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.SHOW_DIMENSIONS] = enabled }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun updateConfirmDelete(enabled: Boolean) {
        context.settingsDataStore.edit { it[PreferencesKeys.CONFIRM_DELETE] = enabled }
    }

    suspend fun resetSettings() {
        context.settingsDataStore.edit {
            it.clear()
        }
    }
}
