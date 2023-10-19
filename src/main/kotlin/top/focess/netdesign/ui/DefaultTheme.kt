package top.focess.netdesign.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DefaultTheme {

    val darkColors = darkColors(
        primary = Color(0xFF1F1F1F),         // Dark Charcoal Gray
        primaryVariant = Color(0xFF191919),  // Slightly Darker Charcoal Gray
        secondary = Color(0xFFD32F2F),      // Dark Red
        secondaryVariant = Color(0xFFB71C1C), // Darker Red
        background = Color(0xFF121212),     // Dark Gray
        surface = Color(0xFF333333),        // Slightly lighter Gray
        error = Color(0xFFFF5722),          // Red
        onPrimary = Color(0xFFFFFFFF),     // White
        onSecondary = Color(0xFFFFFFFF),   // White
        onBackground = Color(0xFFFFFFFF),  // White
        onSurface = Color(0xFFFFFFFF),      // White
        onError = Color(0xFFFFFFFF),        // White
    )

    val lightColors = lightColors(
        primary = Color(0xFFA5A5A5),         // Light Gray
        primaryVariant = Color(0xFF7E7E7E),  // Slightly Darker Gray
        secondary = Color(0xFFD32F2F),      // Dark Red
        secondaryVariant = Color(0xFFB71C1C), // Darker Red
        background = Color(0xFFFFFFFF),     // White
        surface = Color(0xFFE0E0E0),        // Light Gray
        error = Color(0xFFFF5722),          // Red
        onPrimary = Color(0xFF000000),     // Black
        onSecondary = Color(0xFF000000),   // Black
        onBackground = Color(0xFF000000),  // Black
        onSurface = Color(0xFF000000),      // Black
        onError = Color(0xFFFFFFFF),        // White
    )

//    @Composable
//    fun colors() =
//        if (isSystemInDarkTheme())
//            darkColors
//        else
//            lightColors

    @Composable
    fun colors() = lightColors

}