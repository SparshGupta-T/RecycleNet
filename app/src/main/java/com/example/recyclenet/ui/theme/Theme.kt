package com.example.recyclenet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Dark Color Scheme for RecycleNet.
 * Follows Material 3 Expressive guidelines with a focus on high contrast and Teal/Blue accents.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BlueMedium, // Main action color in dark mode
    secondary = TealPrimary, // Brand accent color
    tertiary = BlueLight,
    background = DarkBackground, // Deep background to reduce eye strain
    surface = DarkSurface, // Elevated surfaces (though elevation is kept at 0dp per requirement)
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = TealPrimary,
    onPrimaryContainer = Color.White,
    secondaryContainer = BlueMedium.copy(alpha = 0.2f),
    onSecondaryContainer = BlueMedium
)

/**
 * Light Color Scheme for RecycleNet.
 * Uses the brand Teal (#176B87) as the primary anchor for a clean, professional look.
 */
private val LightColorScheme = lightColorScheme(
    primary = TealPrimary, // Brand Teal
    secondary = BlueMedium, // Medium Blue for secondary actions
    tertiary = BlueLight,
    background = LightBackground, // Very Light Blue (#EEF5FF)
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    primaryContainer = BlueVeryLight,
    onPrimaryContainer = TealPrimary,
    secondaryContainer = BlueLight.copy(alpha = 0.3f),
    onSecondaryContainer = TealPrimary
)

/**
 * The main Theme Composable for the application.
 * Controls the look and feel of all Material components.
 */
@Composable
fun RecycleNetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to maintain brand consistency with the Teal palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
