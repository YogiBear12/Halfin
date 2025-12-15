package com.github.damontecres.wholphin.ui.theme.colors

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import com.github.damontecres.wholphin.ui.theme.ThemeColors

/**
 * Catppuccin Mocha theme for Halfin
 * Based on https://jellyfin.catppuccin.com/catppuccin-mocha.css
 * Main color: Mauve (#cba6f7)
 * Background: #1e1e2e
 * Dark background: #181825
 */
val CatppuccinMacchiatoThemeColors =
    object : ThemeColors {
        // Catppuccin Mocha Light Theme (derived from dark theme)
        val primaryLight = Color(0xFFCBA6F7) // mauve
        val onPrimaryLight = Color(0xFF181825) // dark background
        val primaryContainerLight = Color(0xFFB4BEFE) // lavender
        val onPrimaryContainerLight = Color(0xFF181825)
        val secondaryLight = Color(0xFF89B4FA) // blue
        val onSecondaryLight = Color(0xFF181825)
        val secondaryContainerLight = Color(0xFF74C7EC) // sapphire
        val onSecondaryContainerLight = Color(0xFF181825)
        val tertiaryLight = Color(0xFFF5C2E7) // pink
        val onTertiaryLight = Color(0xFF181825)
        val tertiaryContainerLight = Color(0xFFCBA6F7) // mauve
        val onTertiaryContainerLight = Color(0xFF181825)
        val errorLight = Color(0xFFF38BA8) // red
        val onErrorLight = Color(0xFFFFFFFF)
        val errorContainerLight = Color(0xFFEBA0AC) // maroon
        val onErrorContainerLight = Color(0xFF181825)
        val backgroundLight = Color(0xFFF4F4F5) // Light variant
        val onBackgroundLight = Color(0xFF181825)
        val surfaceLight = Color(0xFFF9F9FA) // Light variant
        val onSurfaceLight = Color(0xFF181825)
        val surfaceVariantLight = Color(0xFFE8E8E9) // Light variant
        val onSurfaceVariantLight = Color(0xFF313244) // hover background
        val outlineLight = Color(0xFF9399B2) // dimmer text
        val outlineVariantLight = Color(0xFFC4C4C5)
        val scrimLight = Color(0xFF000000)
        val inverseSurfaceLight = Color(0xFF181825)
        val inverseOnSurfaceLight = Color(0xFFCDD6F4) // main text
        val inversePrimaryLight = Color(0xFFCBA6F7) // mauve
        val surfaceDimLight = Color(0xFFE8E8E9)
        val surfaceBrightLight = Color(0xFFFFFFFF)
        val surfaceContainerLowestLight = Color(0xFFFFFFFF)
        val surfaceContainerLowLight = Color(0xFFF9F9FA)
        val surfaceContainerLight = Color(0xFFF4F4F5)
        val surfaceContainerHighLight = Color(0xFFEEEEEF)
        val surfaceContainerHighestLight = Color(0xFFE8E8E9)

        // Catppuccin Mocha Dark Theme (from CSS)
        val primaryDark = Color(0xFFCBA6F7) // mauve - main color
        val onPrimaryDark = Color(0xFF181825) // dark background
        val primaryContainerDark = Color(0xFF313244) // hover background
        val onPrimaryContainerDark = Color(0xFFCDD6F4) // main text
        val secondaryDark = Color(0xFF89B4FA) // blue
        val onSecondaryDark = Color(0xFF181825)
        val secondaryContainerDark = Color(0xFF313244) // hover background
        val onSecondaryContainerDark = Color(0xFFCDD6F4)
        val tertiaryDark = Color(0xFFF5C2E7) // pink
        val onTertiaryDark = Color(0xFF181825)
        val tertiaryContainerDark = Color(0xFF313244)
        val onTertiaryContainerDark = Color(0xFFCDD6F4)
        val errorDark = Color(0xFFF38BA8) // red
        val onErrorDark = Color(0xFF181825)
        val errorContainerDark = Color(0xFFEBA0AC) // maroon
        val onErrorContainerDark = Color(0xFF181825)
        val backgroundDark = Color(0xFF1E1E2E) // main background
        val onBackgroundDark = Color(0xFFCDD6F4) // main text
        val surfaceDark = Color(0xFF1E1E2E) // main background
        val onSurfaceDark = Color(0xFFCDD6F4) // main text
        val surfaceVariantDark = Color(0xFF313244) // hover background
        val onSurfaceVariantDark = Color(0xFF9399B2) // dimmer text
        val outlineDark = Color(0xFF9399B2) // dimmer text
        val outlineVariantDark = Color(0xFF313244)
        val scrimDark = Color(0xFF000000)
        val inverseSurfaceDark = Color(0xFFCDD6F4) // main text
        val inverseOnSurfaceDark = Color(0xFF1E1E2E) // main background
        val inversePrimaryDark = Color(0xFFCBA6F7) // mauve - for focus borders
        val surfaceDimDark = Color(0xFF1E2030) // dark background
        val surfaceBrightDark = Color(0xFF363A4F) // hover background
        val surfaceContainerLowestDark = Color(0xFF1E2030) // dark background
        val surfaceContainerLowDark = Color(0xFF1E2030)
        val surfaceContainerDark = Color(0xFF24273A) // main background
        val surfaceContainerHighDark = Color(0xFF2A2D3E)
        val surfaceContainerHighestDark = Color(0xFF363A4F) // hover background

        override val lightSchemeMaterial: androidx.compose.material3.ColorScheme =
            androidx.compose.material3.lightColorScheme(
                primary = primaryLight,
                onPrimary = onPrimaryLight,
                primaryContainer = primaryContainerLight,
                onPrimaryContainer = onPrimaryContainerLight,
                secondary = secondaryLight,
                onSecondary = onSecondaryLight,
                secondaryContainer = secondaryContainerLight,
                onSecondaryContainer = onSecondaryContainerLight,
                tertiary = tertiaryLight,
                onTertiary = onTertiaryLight,
                tertiaryContainer = tertiaryContainerLight,
                onTertiaryContainer = onTertiaryContainerLight,
                error = errorLight,
                onError = onErrorLight,
                errorContainer = errorContainerLight,
                onErrorContainer = onErrorContainerLight,
                background = backgroundLight,
                onBackground = onBackgroundLight,
                surface = surfaceLight,
                onSurface = onSurfaceLight,
                surfaceVariant = surfaceVariantLight,
                onSurfaceVariant = onSurfaceVariantLight,
                scrim = scrimLight,
                inverseSurface = inverseSurfaceLight,
                inverseOnSurface = inverseOnSurfaceLight,
                inversePrimary = inversePrimaryLight,
            )

        override val lightScheme =
            lightColorScheme(
                primary = primaryLight,
                onPrimary = onPrimaryLight,
                primaryContainer = primaryContainerLight,
                onPrimaryContainer = onPrimaryContainerLight,
                secondary = secondaryLight,
                onSecondary = onSecondaryLight,
                secondaryContainer = secondaryContainerLight,
                onSecondaryContainer = onSecondaryContainerLight,
                tertiary = tertiaryLight,
                onTertiary = onTertiaryLight,
                tertiaryContainer = tertiaryContainerLight,
                onTertiaryContainer = onTertiaryContainerLight,
                error = errorLight,
                onError = onErrorLight,
                errorContainer = errorContainerLight,
                onErrorContainer = onErrorContainerLight,
                background = backgroundLight,
                onBackground = onBackgroundLight,
                surface = surfaceLight,
                onSurface = onSurfaceLight,
                surfaceVariant = surfaceVariantLight,
                onSurfaceVariant = onSurfaceVariantLight,
                scrim = scrimLight,
                inverseSurface = inverseSurfaceLight,
                inverseOnSurface = inverseOnSurfaceLight,
                inversePrimary = inversePrimaryLight,
                border = inversePrimaryLight,
            )

        override val darkSchemeMaterial =
            androidx.compose.material3.darkColorScheme(
                primary = primaryDark,
                onPrimary = onPrimaryDark,
                primaryContainer = primaryContainerDark,
                onPrimaryContainer = onPrimaryContainerDark,
                secondary = secondaryDark,
                onSecondary = onSecondaryDark,
                secondaryContainer = secondaryContainerDark,
                onSecondaryContainer = onSecondaryContainerDark,
                tertiary = tertiaryDark,
                onTertiary = onTertiaryDark,
                tertiaryContainer = tertiaryContainerDark,
                onTertiaryContainer = onTertiaryContainerDark,
                error = errorDark,
                onError = onErrorDark,
                errorContainer = errorContainerDark,
                onErrorContainer = onErrorContainerDark,
                background = backgroundDark,
                onBackground = onBackgroundDark,
                surface = surfaceDark,
                onSurface = onSurfaceDark,
                surfaceVariant = surfaceVariantDark,
                onSurfaceVariant = onSurfaceVariantDark,
                scrim = scrimDark,
                inverseSurface = inverseSurfaceDark,
                inverseOnSurface = inverseOnSurfaceDark,
                inversePrimary = inversePrimaryDark,
            )

        override val darkScheme =
            darkColorScheme(
                primary = primaryDark,
                onPrimary = onPrimaryDark,
                primaryContainer = primaryContainerDark,
                onPrimaryContainer = onPrimaryContainerDark,
                secondary = secondaryDark,
                onSecondary = onSecondaryDark,
                secondaryContainer = secondaryContainerDark,
                onSecondaryContainer = onSecondaryContainerDark,
                tertiary = tertiaryDark,
                onTertiary = onTertiaryDark,
                tertiaryContainer = tertiaryContainerDark,
                onTertiaryContainer = onTertiaryContainerDark,
                error = errorDark,
                onError = onErrorDark,
                errorContainer = errorContainerDark,
                onErrorContainer = onErrorContainerDark,
                background = backgroundDark,
                onBackground = onBackgroundDark,
                surface = surfaceDark,
                onSurface = onSurfaceDark,
                surfaceVariant = surfaceVariantDark,
                onSurfaceVariant = onSurfaceVariantDark,
                scrim = scrimDark,
                inverseSurface = inverseSurfaceDark,
                inverseOnSurface = inverseOnSurfaceDark,
                inversePrimary = inversePrimaryDark,
                border = inversePrimaryDark, // Mauve for focus borders
            )
    }

