package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ManualDarkColorScheme = darkColorScheme(
    primary = ManualAccent,
    onPrimary = Color.Black,
    primaryContainer = ManualPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = ManualPrimary,
    onSecondary = Color.White,
    background = ManualBg,
    onBackground = TextPrimary,
    surface = ManualSurface,
    onSurface = TextPrimary,
    surfaceVariant = ManualSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ManualBorder,
    error = StatusRed,
    onError = Color.White
)

val AutoDarkColorScheme = darkColorScheme(
    primary = AutoAccent,
    onPrimary = Color.Black,
    primaryContainer = AutoPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = AutoPrimary,
    onSecondary = Color.White,
    background = AutoBg,
    onBackground = TextPrimary,
    surface = AutoSurface,
    onSurface = TextPrimary,
    surfaceVariant = AutoSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AutoBorder,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun OnionRobotTheme(
    isAutoMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAutoMode) AutoDarkColorScheme else ManualDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
