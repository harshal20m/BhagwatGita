package com.gitaapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.gitaapp.data.model.AppTheme

// ── Saffron / Agni (default) ──────────────────────────────────────────────────
private val SaffronLight = lightColorScheme(
    primary = Color(0xFFB45309), onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7), onPrimaryContainer = Color(0xFF451A03),
    secondary = Color(0xFF7C5C3A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEEBD0), onSecondaryContainer = Color(0xFF2A1505),
    surface = Color(0xFFFFFBF5), onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFF5ECD7), onSurfaceVariant = Color(0xFF4E4530),
    background = Color(0xFFFFFBF5), onBackground = Color(0xFF1C1B17),
    outline = Color(0xFF9C8B6E)
)
private val SaffronDark = darkColorScheme(
    primary = Color(0xFFFFBA1C), onPrimary = Color(0xFF3D2100),
    primaryContainer = Color(0xFF5A3500), onPrimaryContainer = Color(0xFFFFDDB4),
    secondary = Color(0xFFE1B98A), onSecondary = Color(0xFF432B10),
    secondaryContainer = Color(0xFF5C3D1F), onSecondaryContainer = Color(0xFFFFDDB4),
    surface = Color(0xFF161410), onSurface = Color(0xFFEBE1CF),
    surfaceVariant = Color(0xFF2E2A1E), onSurfaceVariant = Color(0xFFD0C3A3),
    background = Color(0xFF161410), onBackground = Color(0xFFEBE1CF),
    outline = Color(0xFF9C8B6E)
)

// ── Lotus / Bhakti (pink-rose) ─────────────────────────────────────────────────
private val LotusLight = lightColorScheme(
    primary = Color(0xFFBE185D), onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE7F3), onPrimaryContainer = Color(0xFF500724),
    secondary = Color(0xFF9D174D), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBD5E8), onSecondaryContainer = Color(0xFF3B0A1F),
    surface = Color(0xFFFFF5F8), onSurface = Color(0xFF1A0D12),
    surfaceVariant = Color(0xFFF8E0EB), onSurfaceVariant = Color(0xFF4D2D3A),
    background = Color(0xFFFFF5F8), onBackground = Color(0xFF1A0D12),
    outline = Color(0xFFA87090)
)
private val LotusDark = darkColorScheme(
    primary = Color(0xFFF472B6), onPrimary = Color(0xFF5A0D2E),
    primaryContainer = Color(0xFF7C1040), onPrimaryContainer = Color(0xFFFDD7E8),
    secondary = Color(0xFFE779A5), onSecondary = Color(0xFF4A0D24),
    secondaryContainer = Color(0xFF681535), onSecondaryContainer = Color(0xFFFDD7E8),
    surface = Color(0xFF1A0D12), onSurface = Color(0xFFF0DCE5),
    surfaceVariant = Color(0xFF321520), onSurfaceVariant = Color(0xFFD6B5C2),
    background = Color(0xFF1A0D12), onBackground = Color(0xFFF0DCE5),
    outline = Color(0xFFA87090)
)

// ── Krishna / Neela (deep blue) ────────────────────────────────────────────────
private val KrishnaLight = lightColorScheme(
    primary = Color(0xFF1D4ED8), onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE), onPrimaryContainer = Color(0xFF0A1F5E),
    secondary = Color(0xFF1E40AF), onSecondary = Color.White,
    secondaryContainer = Color(0xFFBFD4FD), onSecondaryContainer = Color(0xFF071540),
    surface = Color(0xFFF5F8FF), onSurface = Color(0xFF0D1526),
    surfaceVariant = Color(0xFFDDE6F8), onSurfaceVariant = Color(0xFF2C3D60),
    background = Color(0xFFF5F8FF), onBackground = Color(0xFF0D1526),
    outline = Color(0xFF6080B8)
)
private val KrishnaDark = darkColorScheme(
    primary = Color(0xFF7DA4F8), onPrimary = Color(0xFF0A1F5E),
    primaryContainer = Color(0xFF1438A8), onPrimaryContainer = Color(0xFFD6E4FD),
    secondary = Color(0xFF93B4F5), onSecondary = Color(0xFF071540),
    secondaryContainer = Color(0xFF162DA8), onSecondaryContainer = Color(0xFFD6E4FD),
    surface = Color(0xFF0D1526), onSurface = Color(0xFFDDE8FC),
    surfaceVariant = Color(0xFF1A2840), onSurfaceVariant = Color(0xFFBFCFE8),
    background = Color(0xFF0D1526), onBackground = Color(0xFFDDE8FC),
    outline = Color(0xFF6080B8)
)

// ── Tulsi / Prakriti (forest green) ───────────────────────────────────────────
private val TulsiLight = lightColorScheme(
    primary = Color(0xFF15803D), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7), onPrimaryContainer = Color(0xFF052E16),
    secondary = Color(0xFF166534), onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBF7D0), onSecondaryContainer = Color(0xFF042010),
    surface = Color(0xFFF5FFF7), onSurface = Color(0xFF0E1A12),
    surfaceVariant = Color(0xFFDBF0E0), onSurfaceVariant = Color(0xFF2C4830),
    background = Color(0xFFF5FFF7), onBackground = Color(0xFF0E1A12),
    outline = Color(0xFF5E9B68)
)
private val TulsiDark = darkColorScheme(
    primary = Color(0xFF6EE79A), onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF0A5628), onPrimaryContainer = Color(0xFFD0F5DE),
    secondary = Color(0xFF86E8A5), onSecondary = Color(0xFF042010),
    secondaryContainer = Color(0xFF0C4420), onSecondaryContainer = Color(0xFFD0F5DE),
    surface = Color(0xFF0E1A12), onSurface = Color(0xFFDCF0E2),
    surfaceVariant = Color(0xFF172E1D), onSurfaceVariant = Color(0xFFBAD4C2),
    background = Color(0xFF0E1A12), onBackground = Color(0xFFDCF0E2),
    outline = Color(0xFF5E9B68)
)

// ── Ganga / Shanti (teal) ──────────────────────────────────────────────────────
private val GangaLight = lightColorScheme(
    primary = Color(0xFF0E7490), onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE), onPrimaryContainer = Color(0xFF042F38),
    secondary = Color(0xFF155E75), onSecondary = Color.White,
    secondaryContainer = Color(0xFFA5F3FC), onSecondaryContainer = Color(0xFF082028),
    surface = Color(0xFFF0FBFF), onSurface = Color(0xFF0C1A1E),
    surfaceVariant = Color(0xFFCAF0F8), onSurfaceVariant = Color(0xFF254550),
    background = Color(0xFFF0FBFF), onBackground = Color(0xFF0C1A1E),
    outline = Color(0xFF4E97A8)
)
private val GangaDark = darkColorScheme(
    primary = Color(0xFF67D4E8), onPrimary = Color(0xFF042F38),
    primaryContainer = Color(0xFF0A5568), onPrimaryContainer = Color(0xFFCAF0F8),
    secondary = Color(0xFF7DE0F2), onSecondary = Color(0xFF082028),
    secondaryContainer = Color(0xFF0C4454), onSecondaryContainer = Color(0xFFCAF0F8),
    surface = Color(0xFF0C1A1E), onSurface = Color(0xFFCCEEF5),
    surfaceVariant = Color(0xFF142830), onSurfaceVariant = Color(0xFFADD0DA),
    background = Color(0xFF0C1A1E), onBackground = Color(0xFFCCEEF5),
    outline = Color(0xFF4E97A8)
)

// ── Rudra / Shakti (deep purple) ──────────────────────────────────────────────
private val RudraLight = lightColorScheme(
    primary = Color(0xFF9333EA), onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF), onPrimaryContainer = Color(0xFF3B0764),
    secondary = Color(0xFF7C3AED), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE), onSecondaryContainer = Color(0xFF2D035C),
    surface = Color(0xFFFAF5FF), onSurface = Color(0xFF160C22),
    surfaceVariant = Color(0xFFEDE0FA), onSurfaceVariant = Color(0xFF3D2860),
    background = Color(0xFFFAF5FF), onBackground = Color(0xFF160C22),
    outline = Color(0xFF9060C0)
)
private val RudraDark = darkColorScheme(
    primary = Color(0xFFD08AF8), onPrimary = Color(0xFF3B0764),
    primaryContainer = Color(0xFF5C1AA0), onPrimaryContainer = Color(0xFFF0D8FC),
    secondary = Color(0xFFBF9DF5), onSecondary = Color(0xFF2D035C),
    secondaryContainer = Color(0xFF4A128A), onSecondaryContainer = Color(0xFFF0D8FC),
    surface = Color(0xFF160C22), onSurface = Color(0xFFECDCFC),
    surfaceVariant = Color(0xFF271540), onSurfaceVariant = Color(0xFFD0B8E8),
    background = Color(0xFF160C22), onBackground = Color(0xFFECDCFC),
    outline = Color(0xFF9060C0)
)

fun appThemeColors(theme: AppTheme, dark: Boolean): ColorScheme = when (theme) {
    AppTheme.SAFFRON      -> if (dark) SaffronDark else SaffronLight
    AppTheme.LOTUS        -> if (dark) LotusDark   else LotusLight
    AppTheme.KRISHNA      -> if (dark) KrishnaDark else KrishnaLight
    AppTheme.TULSI        -> if (dark) TulsiDark   else TulsiLight
    AppTheme.SAFFRON_DARK -> if (dark) GangaDark   else GangaLight
    AppTheme.RUDRA        -> if (dark) RudraDark   else RudraLight
}

@Composable
fun GitaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.SAFFRON,
    dynamicColor: Boolean = false, // disabled so our spiritual themes show
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val ctx = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else {
        appThemeColors(appTheme, darkTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = GitaTypography, content = content)
}
