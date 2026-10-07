package com.example.geosplit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.geosplit.model.CalculationRecord
import com.example.geosplit.model.GeoShape
import com.example.geosplit.model.MeasurementUnit
import com.example.geosplit.model.RectangleSplitMode
import com.example.geosplit.model.SquareGridPreset
import com.example.geosplit.model.SquareSplitMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "geosplit_history")

class HistoryRepository(private val context: Context) {

    companion object {
        const val MAX_HISTORY_RECORDS = 50
        val HISTORY_KEY = stringPreferencesKey("saved_calculations_json")

        fun serializeRecords(records: List<CalculationRecord>): String {
            val jsonArray = JSONArray()
            records.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("projectName", item.projectName)
                    put("shape", item.shape.name)
                    item.rectangleSplitMode?.let { put("rectangleSplitMode", it.name) }
                    item.squareSplitMode?.let { put("squareSplitMode", it.name) }
                    item.squareGridPreset?.let { put("squareGridPreset", it.name) }
                    put("unit", item.unit.name)
                    put("lengthText", item.lengthText)
                    put("widthText", item.widthText)
                    put("sideText", item.sideText)
                    put("radiusText", item.radiusText)
                    put("triangleBaseText", item.triangleBaseText)
                    put("triangleHeightText", item.triangleHeightText)
                    put("sectionsText", item.sectionsText)
                    put("rowsText", item.rowsText)
                    put("columnsText", item.columnsText)
                    put("sectionCount", item.sectionCount)
                    put("dimensionsSummary", item.dimensionsSummary)
                    put("totalAreaFormatted", item.totalAreaFormatted)
                    put("areaPerSectionFormatted", item.areaPerSectionFormatted)
                    put("sectionDimensionsFormatted", item.sectionDimensionsFormatted)
                    put("sectionPercentageFormatted", item.sectionPercentageFormatted)
                    put("timestamp", item.timestamp)
                    put("isFavorite", item.isFavorite)
                }
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun deserializeRecords(jsonString: String): List<CalculationRecord> {
            val list = mutableListOf<CalculationRecord>()
            if (jsonString.isBlank()) return list
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val shapeStr = obj.optString("shape", GeoShape.RECTANGLE.name)
                    val shape = try { GeoShape.valueOf(shapeStr) } catch (_: Exception) { GeoShape.RECTANGLE }

                    val unitStr = obj.optString("unit", MeasurementUnit.CENTIMETER.name)
                    val unit = try { MeasurementUnit.valueOf(unitStr) } catch (_: Exception) { MeasurementUnit.CENTIMETER }

                    val rectSplitStr = obj.optString("rectangleSplitMode", "")
                    val rectSplit = if (rectSplitStr.isNotEmpty()) {
                        try { RectangleSplitMode.valueOf(rectSplitStr) } catch (_: Exception) { null }
                    } else null

                    val sqSplitStr = obj.optString("squareSplitMode", "")
                    val sqSplit = if (sqSplitStr.isNotEmpty()) {
                        try { SquareSplitMode.valueOf(sqSplitStr) } catch (_: Exception) { null }
                    } else null

                    val gridPresetStr = obj.optString("squareGridPreset", "")
                    val gridPreset = if (gridPresetStr.isNotEmpty()) {
                        try { SquareGridPreset.valueOf(gridPresetStr) } catch (_: Exception) { null }
                    } else null

                    val record = CalculationRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        projectName = obj.optString("projectName", ""),
                        shape = shape,
                        rectangleSplitMode = rectSplit,
                        squareSplitMode = sqSplit,
                        squareGridPreset = gridPreset,
                        unit = unit,
                        lengthText = obj.optString("lengthText", ""),
                        widthText = obj.optString("widthText", ""),
                        sideText = obj.optString("sideText", ""),
                        radiusText = obj.optString("radiusText", ""),
                        triangleBaseText = obj.optString("triangleBaseText", ""),
                        triangleHeightText = obj.optString("triangleHeightText", ""),
                        sectionsText = obj.optString("sectionsText", ""),
                        rowsText = obj.optString("rowsText", ""),
                        columnsText = obj.optString("columnsText", ""),
                        sectionCount = obj.optInt("sectionCount", 1),
                        dimensionsSummary = obj.optString("dimensionsSummary", ""),
                        totalAreaFormatted = obj.optString("totalAreaFormatted", ""),
                        areaPerSectionFormatted = obj.optString("areaPerSectionFormatted", ""),
                        sectionDimensionsFormatted = obj.optString("sectionDimensionsFormatted", ""),
                        sectionPercentageFormatted = obj.optString("sectionPercentageFormatted", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isFavorite = obj.optBoolean("isFavorite", false)
                    )
                    list.add(record)
                }
            } catch (_: Exception) {
                // Return successfully parsed records or empty list on corrupted data
            }
            return list
        }
    }

    val historyFlow: Flow<List<CalculationRecord>> = context.historyDataStore.data.map { prefs ->
        val jsonString = prefs[HISTORY_KEY] ?: "[]"
        deserializeRecords(jsonString)
    }

    suspend fun saveCalculation(record: CalculationRecord) {
        context.historyDataStore.edit { prefs ->
            val existing = deserializeRecords(prefs[HISTORY_KEY] ?: "[]").toMutableList()
            // Add new record at the top
            existing.add(0, record)
            // Keep at most MAX_HISTORY_RECORDS
            val trimmed = if (existing.size > MAX_HISTORY_RECORDS) {
                existing.take(MAX_HISTORY_RECORDS)
            } else {
                existing
            }
            prefs[HISTORY_KEY] = serializeRecords(trimmed)
        }
    }

    suspend fun toggleFavorite(recordId: String) {
        context.historyDataStore.edit { prefs ->
            val existing = deserializeRecords(prefs[HISTORY_KEY] ?: "[]").toMutableList()
            val index = existing.indexOfFirst { it.id == recordId }
            if (index != -1) {
                val current = existing[index]
                existing[index] = current.copy(isFavorite = !current.isFavorite)
                prefs[HISTORY_KEY] = serializeRecords(existing)
            }
        }
    }

    suspend fun duplicateCalculation(record: CalculationRecord): CalculationRecord {
        val baseName = if (record.projectName.isBlank()) "Untitled Split" else record.projectName
        val duplicated = record.copy(
            id = UUID.randomUUID().toString(),
            projectName = "Copy of $baseName",
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )
        saveCalculation(duplicated)
        return duplicated
    }

    suspend fun deleteCalculation(recordId: String) {
        context.historyDataStore.edit { prefs ->
            val existing = deserializeRecords(prefs[HISTORY_KEY] ?: "[]").toMutableList()
            existing.removeAll { it.id == recordId }
            prefs[HISTORY_KEY] = serializeRecords(existing)
        }
    }

    suspend fun clearAllHistory() {
        context.historyDataStore.edit { prefs ->
            prefs.remove(HISTORY_KEY)
        }
    }
}
