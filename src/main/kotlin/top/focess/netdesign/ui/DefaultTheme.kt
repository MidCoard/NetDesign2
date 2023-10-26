package top.focess.netdesign.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DefaultTheme {

    val FORCE_LIGHT = true

    val darkColors = darkColors(
        primary = Color(0xFF2196F3), // Blue
        primaryVariant = Color(0xFF1976D2),
        secondary = Color(0xFF03DAC6),
        secondaryVariant = Color(0xFF018786),
        background = Color(0xFF1C1C1C),
        surface = Color(0xFFE0E0E0),
        error = Color(0xFFCF6679),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFFFFFFF),
        onError = Color(0xFF000000),
    )


    val lightColors = lightColors(
        primary = Color(0xFF4CAF50),
        primaryVariant = Color(0xFF388E3C),
        secondary = Color(0xFF2196F3),
        secondaryVariant = Color(0xFF1976D2),
        background = Color(0xFFFFEB3B),
        surface = Color(0xFFEEEEEE),
        error = Color(0xFFF44336),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFFFFFFFF),
        onBackground = Color(0xFF333333),
        onSurface = Color(0xFF666666),
        onError = Color(0xFFFFFFFF),
    )

    @Composable
    fun colors() =
        if (isSystemInDarkTheme() && !FORCE_LIGHT)
            darkColors
        else
            lightColors

    @Composable
    fun slightlyColors() = if (isSystemInDarkTheme()) darkColors else lightColors


}