package com.gitaapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Saffron / Gold inspired spiritual palette ─────────────────────────────────

val SaffronPrimary = Color(0xFFB45309)          // Deep saffron
val SaffronOnPrimary = Color(0xFFFFFFFF)
val SaffronPrimaryContainer = Color(0xFFFEF3C7) // Light golden
val SaffronOnPrimaryContainer = Color(0xFF451A03)

val SaffronSecondary = Color(0xFF7C5C3A)        // Warm sandalwood brown
val SaffronOnSecondary = Color(0xFFFFFFFF)
val SaffronSecondaryContainer = Color(0xFFFEEBD0)
val SaffronOnSecondaryContainer = Color(0xFF2A1505)

val SaffronTertiary = Color(0xFF4E7C45)         // Sacred forest green
val SaffronOnTertiary = Color(0xFFFFFFFF)
val SaffronTertiaryContainer = Color(0xFFD8F1CE)
val SaffronOnTertiaryContainer = Color(0xFF0B2007)

val SaffronSurface = Color(0xFFFFFBF5)          // Warm parchment
val SaffronOnSurface = Color(0xFF1C1B17)
val SaffronSurfaceVariant = Color(0xFFF5ECD7)
val SaffronOnSurfaceVariant = Color(0xFF4E4530)
val SaffronBackground = Color(0xFFFFFBF5)
val SaffronOutline = Color(0xFF9C8B6E)

// ── Dark theme palette ────────────────────────────────────────────────────────

val SaffronDarkPrimary = Color(0xFFFFBA1C)      // Bright gold in dark
val SaffronDarkOnPrimary = Color(0xFF3D2100)
val SaffronDarkPrimaryContainer = Color(0xFF5A3500)
val SaffronDarkOnPrimaryContainer = Color(0xFFFFDDB4)

val SaffronDarkSecondary = Color(0xFFE1B98A)
val SaffronDarkOnSecondary = Color(0xFF432B10)
val SaffronDarkSecondaryContainer = Color(0xFF5C3D1F)
val SaffronDarkOnSecondaryContainer = Color(0xFFFFDDB4)

val SaffronDarkTertiary = Color(0xFFB3D9A6)
val SaffronDarkOnTertiary = Color(0xFF1E3E16)
val SaffronDarkTertiaryContainer = Color(0xFF35572B)
val SaffronDarkOnTertiaryContainer = Color(0xFFCFF6C0)

val SaffronDarkSurface = Color(0xFF161410)      // Deep dark warm
val SaffronDarkOnSurface = Color(0xFFEBE1CF)
val SaffronDarkSurfaceVariant = Color(0xFF2E2A1E)
val SaffronDarkOnSurfaceVariant = Color(0xFFD0C3A3)
val SaffronDarkBackground = Color(0xFF161410)
val SaffronDarkOutline = Color(0xFF9C8B6E)

// Error stays standard Material
val ErrorColor = Color(0xFFB00020)
val ErrorDarkColor = Color(0xFFCF6679)

// ── Color Schemes ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = SaffronOnPrimary,
    primaryContainer = SaffronPrimaryContainer,
    onPrimaryContainer = SaffronOnPrimaryContainer,
    secondary = SaffronSecondary,
    onSecondary = SaffronOnSecondary,
    secondaryContainer = SaffronSecondaryContainer,
    onSecondaryContainer = SaffronOnSecondaryContainer,
    tertiary = SaffronTertiary,
    onTertiary = SaffronOnTertiary,
    tertiaryContainer = SaffronTertiaryContainer,
    onTertiaryContainer = SaffronOnTertiaryContainer,
    surface = SaffronSurface,
    onSurface = SaffronOnSurface,
    surfaceVariant = SaffronSurfaceVariant,
    onSurfaceVariant = SaffronOnSurfaceVariant,
    background = SaffronBackground,
    onBackground = SaffronOnSurface,
    outline = SaffronOutline,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronDarkPrimary,
    onPrimary = SaffronDarkOnPrimary,
    primaryContainer = SaffronDarkPrimaryContainer,
    onPrimaryContainer = SaffronDarkOnPrimaryContainer,
    secondary = SaffronDarkSecondary,
    onSecondary = SaffronDarkOnSecondary,
    secondaryContainer = SaffronDarkSecondaryContainer,
    onSecondaryContainer = SaffronDarkOnSecondaryContainer,
    tertiary = SaffronDarkTertiary,
    onTertiary = SaffronDarkOnTertiary,
    tertiaryContainer = SaffronDarkTertiaryContainer,
    onTertiaryContainer = SaffronDarkOnTertiaryContainer,
    surface = SaffronDarkSurface,
    onSurface = SaffronDarkOnSurface,
    surfaceVariant = SaffronDarkSurfaceVariant,
    onSurfaceVariant = SaffronDarkOnSurfaceVariant,
    background = SaffronDarkBackground,
    onBackground = SaffronDarkOnSurface,
    outline = SaffronDarkOutline,
    error = ErrorDarkColor,
    onError = Color.Black
)

/**
 * Main app theme composable.
 * Supports dynamic color on Android 12+ while falling back gracefully.
 */
@Composable
fun GitaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GitaTypography,
        content = content
    )
}
