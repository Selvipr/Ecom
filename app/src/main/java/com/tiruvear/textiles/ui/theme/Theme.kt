package com.tiruvear.textiles.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tiruvear.textiles.R

private val DarkColorPalette = darkColors(
    primary = Color(0xFF2D4059),
    primaryVariant = Color(0xFF1A2635),
    secondary = Color(0xFFEA5455),
    background = Color(0xFF121212),
    surface = Color(0xFF121212)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF2D4059), 
    primaryVariant = Color(0xFF1A2635),
    secondary = Color(0xFFEA5455),
    background = Color.White,
    surface = Color.White
)

@Composable
fun TiruvearTextileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
} 