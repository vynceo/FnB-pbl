package com.dadarberedar.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = White,
    primaryContainer = AmberLight,
    onPrimaryContainer = White,
    secondary = AccentGold,
    onSecondary = TextPrimary,
    tertiary = AccentOrange,
    background = BackgroundCream,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberLight,
    onPrimary = TextPrimary,
    primaryContainer = Amber,
    onPrimaryContainer = White,
    secondary = AccentGold,
    onSecondary = TextPrimary,
    tertiary = AccentOrange,
    background = Color(0xFF1C1410),
    surface = Color(0xFF2A1F18),
    surfaceVariant = Color(0xFF3E2F24),
    onBackground = Color(0xFFEFEBE9),
    onSurface = Color(0xFFEFEBE9),
    onSurfaceVariant = Color(0xFFBCAAA4),
    outline = Color(0xFF5D4037),
    error = Color(0xFFEF5350)
)

@Composable
fun DadarBeredarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            if (darkTheme) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            } else {
                window.statusBarColor = SurfaceWhite.toArgb()
                window.navigationBarColor = SurfaceWhite.toArgb()
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
