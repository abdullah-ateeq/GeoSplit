package com.example.geosplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geosplit.data.AppThemeMode
import com.example.geosplit.ui.navigation.GeoSplitApp
import com.example.geosplit.ui.theme.GeoSplitTheme
import com.example.geosplit.viewmodel.GeoSplitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GeoSplitViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            val darkTheme = when (uiState.settings.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            GeoSplitTheme(
                darkTheme = darkTheme,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                GeoSplitApp(viewModel = viewModel)
            }
        }
    }
}