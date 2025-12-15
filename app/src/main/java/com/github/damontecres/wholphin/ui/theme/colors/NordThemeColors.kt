package com.github.damontecres.wholphin.ui.theme.colors

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import com.github.damontecres.wholphin.ui.theme.ThemeColors

/**
 * Nord theme for Halfin
 * Based on https://www.nordtheme.com/docs/colors-and-palettes
 * Primary accent: nord8 (#88c0d0) - Frost palette
 * Background: nord0 (#2e3440) - Polar Night palette
 */
val NordThemeColors =
    object : ThemeColors {
        // Nord Light Theme (derived from dark theme)
        val primaryLight = Color(0xFF88C0D0) // nord8 - Frost primary accent
        val onPrimaryLight = Color(0xFF2E3440) // nord0
        val primaryContainerLight = Color(0xFFD8DEE9) // nord4
        val onPrimaryContainerLight = Color(0xFF2E3440) // nord0
        val secondaryLight = Color(0xFF81A1C1) // nord9
        val onSecondaryLight = Color(0xFF2E3440) // nord0
        val secondaryContainerLight = Color(0xFFE5E9F0) // nord5
        val onSecondaryContainerLight = Color(0xFF2E3440) // nord0
        val tertiaryLight = Color(0xFF8FBCBB) // nord7
        val onTertiaryLight = Color(0xFF2E3440) // nord0
        val tertiaryContainerLight = Color(0xFFE5E9F0) // nord5
        val onTertiaryContainerLight = Color(0xFF2E3440) // nord0
        val errorLight = Color(0xFFBF616A) // nord11 - Aurora red
        val onErrorLight = Color(0xFFFFFFFF)
        val errorContainerLight = Color(0xFFD08770) // nord12 - Aurora orange
        val onErrorContainerLight = Color(0xFF2E3440) // nord0
        val backgroundLight = Color(0xFFECEFF4) // nord6 - Snow Storm brightest
        val onBackgroundLight = Color(0xFF2E3440) // nord0
        val surfaceLight = Color(0xFFECEFF4) // nord6
        val onSurfaceLight = Color(0xFF2E3440) // nord0
        val surfaceVariantLight = Color(0xFFD8DEE9) // nord4
        val onSurfaceVariantLight = Color(0xFF3B4252) // nord1
        val outlineLight = Color(0xFF4C566A) // nord3
        val outlineVariantLight = Color(0xFFD8DEE9) // nord4
        val scrimLight = Color(0xFF000000)
        val inverseSurfaceLight = Color(0xFF2E3440) // nord0
        val inverseOnSurfaceLight = Color(0xFFECEFF4) // nord6
        val inversePrimaryLight = Color(0xFF88C0D0) // nord8
        val surfaceDimLight = Color(0xFFD8DEE9) // nord4
        val surfaceBrightLight = Color(0xFFFFFFFF)
        val surfaceContainerLowestLight = Color(0xFFFFFFFF)
        val surfaceContainerLowLight = Color(0xFFF5F7FA)
        val surfaceContainerLight = Color(0xFFE5E9F0) // nord5
        val surfaceContainerHighLight = Color(0xFFD8DEE9) // nord4
        val surfaceContainerHighestLight = Color(0xFFD8DEE9) // nord4

        // Nord Dark Theme (from Nord palette)
        val primaryDark = Color(0xFF88C0D0) // nord8 - Frost primary accent
        val onPrimaryDark = Color(0xFF2E3440) // nord0
        val primaryContainerDark = Color(0xFF4C566A) // nord3
        val onPrimaryContainerDark = Color(0xFFE5E9F0) // nord5
        val secondaryDark = Color(0xFF81A1C1) // nord9 - Frost secondary
        val onSecondaryDark = Color(0xFF2E3440) // nord0
        val secondaryContainerDark = Color(0xFF3B4252) // nord1
        val onSecondaryContainerDark = Color(0xFFD8DEE9) // nord4
        val tertiaryDark = Color(0xFF8FBCBB) // nord7 - Frost tertiary
        val onTertiaryDark = Color(0xFF2E3440) // nord0
        val tertiaryContainerDark = Color(0xFF3B4252) // nord1
        val onTertiaryContainerDark = Color(0xFFD8DEE9) // nord4
        val errorDark = Color(0xFFBF616A) // nord11 - Aurora red
        val onErrorDark = Color(0xFF2E3440) // nord0
        val errorContainerDark = Color(0xFF4C1E24) // Darker red variant
        val onErrorContainerDark = Color(0xFFEECFD3) // Light red variant
        val backgroundDark = Color(0xFF2E3440) // nord0 - Polar Night origin
        val onBackgroundDark = Color(0xFFE5E9F0) // nord5 - Snow Storm
        val surfaceDark = Color(0xFF3B4252) // nord1 - Polar Night brighter shade
        val onSurfaceDark = Color(0xFFE5E9F0) // nord5
        val surfaceVariantDark = Color(0xFF434C5E) // nord2 - Polar Night even brighter
        val onSurfaceVariantDark = Color(0xFFC7D1DD) // Lighter variant
        val outlineDark = Color(0xFF4C566A) // nord3 - Polar Night brightest
        val outlineVariantDark = Color(0xFF434C5E) // nord2
        val scrimDark = Color(0xFF000000)
        val inverseSurfaceDark = Color(0xFFECEFF4) // nord6
        val inverseOnSurfaceDark = Color(0xFF2E3440) // nord0
        val inversePrimaryDark = Color(0xFF88C0D0) // nord8 - for focus borders
        val surfaceDimDark = Color(0xFF2E3440) // nord0
        val surfaceBrightDark = Color(0xFF4C566A) // nord3
        val surfaceContainerLowestDark = Color(0xFF1F242D) // Darker than nord0
        val surfaceContainerLowDark = Color(0xFF242A33) // Slightly darker than nord0
        val surfaceContainerDark = Color(0xFF272E37) // Between nord0 and nord1
        val surfaceContainerHighDark = Color(0xFF2C333D) // Between nord0 and nord1
        val surfaceContainerHighestDark = Color(0xFF313844) // Between nord1 and nord2

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
                border = inversePrimaryLight, // nord8 for focus borders
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
                border = inversePrimaryDark, // nord8 (#88c0d0) for card focus borders
            )
    }
