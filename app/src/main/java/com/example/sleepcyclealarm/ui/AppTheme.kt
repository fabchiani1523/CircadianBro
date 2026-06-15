package com.example.sleepcyclealarm.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PurplePrimary = Color(0xFF6D28D9)
val PurpleDark = Color(0xFF3B0764)
val PurpleSoft = Color(0xFFA78BFA)
val PurplePale = Color(0xFFF7F2FF)
val PurpleLine = Color(0xFFE9D5FF)
val White = Color(0xFFFFFFFF)

private val LightColors: ColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = White,
    secondary = PurpleSoft,
    onSecondary = PurpleDark,
    background = PurplePale,
    onBackground = PurpleDark,
    surface = White,
    onSurface = PurpleDark,
    surfaceVariant = Color(0xFFF2E8FF),
    onSurfaceVariant = Color(0xFF5B476F),
    outline = PurpleLine
)

@Composable
fun SleepCycleTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) LightColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
