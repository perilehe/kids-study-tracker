package com.kidsstudy.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VibrantOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCC5),
    onPrimaryContainer = Color(0xFF331100),
    secondary = SunnyYellow,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0B6),
    onSecondaryContainer = Color(0xFF2D2000),
    tertiary = MintGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8F5DD),
    onTertiaryContainer = Color(0xFF002117),
    error = PassionRed,
    onError = Color.White,
    background = WarmBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5EED9),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFE0D9C5)
)

@Composable
fun KidsStudyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WarmBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
