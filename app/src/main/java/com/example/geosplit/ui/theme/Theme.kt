package com.example.geosplit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BlueprintTealDark,
    onPrimary = Color(0xFF003649),
    primaryContainer = BlueprintCyanContainerDark,
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = SlateSecondaryDark,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = SlateSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = TechnicalAmberDark,
    onTertiary = Color(0xFF451A03),
    background = TechNearBlack,
    onBackground = Color(0xFFF1F5F9),
    surface = TechGraphiteSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = TechGraphiteSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = TechOutlineDark,
    outlineVariant = Color(0xFF2E3846)
)

private val LightColorScheme = lightColorScheme(
    primary = BlueprintTealLight,
    onPrimary = Color.White,
    primaryContainer = BlueprintCyanContainerLight,
    onPrimaryContainer = Color(0xFF004D65),
    secondary = SlateSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SlateSecondaryContainerLight,
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = TechnicalAmberLight,
    onTertiary = Color.White,
    background = TechWarmWhite,
    onBackground = Color(0xFF0F172A),
    surface = TechLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = TechLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = TechOutlineLight,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun GeoSplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}