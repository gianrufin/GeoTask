package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppColorPalette(val displayName: String, val description: String) {
    SLATE_CYAN("Deep Slate & Cyan", "Balanced contrast with calm tones"),
    HIGH_CONTRAST_AMBER("High Contrast Amber", "Maximum visibility on dark background"),
    EMERALD("Emerald Focus", "Calm nature green tones"),
    MONOCHROME("High Contrast B&W", "Pure stark accessibility")
}

enum class DarkModePreference(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

private val SlateCyanDarkScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = CyanOnPrimaryDark,
    primaryContainer = Color(0xFF164E63),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = CyanSecondaryDark,
    onSecondary = Color(0xFF0F172A),
    background = CyanBackgroundDark,
    onBackground = CyanOnSurfaceDark,
    surface = CyanSurfaceDark,
    onSurface = CyanOnSurfaceDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B)
)

private val SlateCyanLightScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = CyanOnPrimaryLight,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CyanSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    background = CyanBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = CyanSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8)
)

private val AmberDarkScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = AmberOnPrimaryDark,
    primaryContainer = Color(0xFF451A03),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = AmberSecondaryDark,
    onSecondary = Color(0xFF000000),
    background = AmberBackgroundDark,
    onBackground = AmberOnSurfaceDark,
    surface = AmberSurfaceDark,
    onSurface = AmberOnSurfaceDark,
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFE4E4E7),
    outline = AmberCardBorderDark
)

private val AmberLightScheme = lightColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = AmberOnPrimaryLight,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = AmberSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    background = AmberBackgroundLight,
    onBackground = AmberOnSurfaceLight,
    surface = AmberSurfaceLight,
    onSurface = AmberOnSurfaceLight,
    surfaceVariant = Color(0xFFF5F5F4),
    onSurfaceVariant = Color(0xFF44403C),
    outline = Color(0xFFA8A29E)
)

private val EmeraldDarkScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = EmeraldSecondaryDark,
    onSecondary = Color(0xFF062D24),
    background = EmeraldBackgroundDark,
    onBackground = EmeraldOnSurfaceDark,
    surface = EmeraldSurfaceDark,
    onSurface = EmeraldOnSurfaceDark,
    surfaceVariant = Color(0xFF134E3E),
    onSurfaceVariant = Color(0xFFD1FAE5),
    outline = Color(0xFF10B981)
)

private val EmeraldLightScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFFFFFFFF),
    background = EmeraldBackgroundLight,
    onBackground = EmeraldOnSurfaceLight,
    surface = EmeraldSurfaceLight,
    onSurface = EmeraldOnSurfaceLight,
    surfaceVariant = Color(0xFFF0FDF4),
    onSurfaceVariant = Color(0xFF166534),
    outline = Color(0xFF6EE7B7)
)

private val MonoDarkScheme = darkColorScheme(
    primary = MonoPrimaryDark,
    onPrimary = MonoOnPrimaryDark,
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFE5E5E5),
    onSecondary = Color(0xFF000000),
    background = MonoBackgroundDark,
    onBackground = MonoOnSurfaceDark,
    surface = MonoSurfaceDark,
    onSurface = MonoOnSurfaceDark,
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFE5E5E5),
    outline = Color(0xFF737373)
)

private val MonoLightScheme = lightColorScheme(
    primary = MonoPrimaryLight,
    onPrimary = MonoOnPrimaryLight,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF404040),
    onSecondary = Color(0xFFFFFFFF),
    background = MonoBackgroundLight,
    onBackground = MonoOnSurfaceLight,
    surface = MonoSurfaceLight,
    onSurface = MonoOnSurfaceLight,
    surfaceVariant = Color(0xFFE5E5E5),
    onSurfaceVariant = Color(0xFF262626),
    outline = Color(0xFFA3A3A3)
)

@Composable
fun MyApplicationTheme(
    palette: AppColorPalette = AppColorPalette.SLATE_CYAN,
    darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
    darkTheme: Boolean = when (darkModePreference) {
        DarkModePreference.SYSTEM -> isSystemInDarkTheme()
        DarkModePreference.LIGHT -> false
        DarkModePreference.DARK -> true
    },
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (palette) {
        AppColorPalette.SLATE_CYAN -> if (darkTheme) SlateCyanDarkScheme else SlateCyanLightScheme
        AppColorPalette.HIGH_CONTRAST_AMBER -> if (darkTheme) AmberDarkScheme else AmberLightScheme
        AppColorPalette.EMERALD -> if (darkTheme) EmeraldDarkScheme else EmeraldLightScheme
        AppColorPalette.MONOCHROME -> if (darkTheme) MonoDarkScheme else MonoLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
