package com.github.jan222ik.ui.value

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color set
val LightTheme = lightColors() // TODO :
val DarkTheme = darkColors(
    primary = R.color.PictonBlue,
    onPrimary = Color.White,
    secondary = R.color.Elephant,
    onSecondary = Color.White,
    surface = R.color.BigStone,
    error = R.color.WildWatermelon
)

@Composable
fun AppTheme(
    isDark: Boolean = true, // TODO: If you want to support both light theme and dark theme, you'll need to implement it manually.
    content: @Composable ColumnScope.() -> Unit,
) {
    MaterialTheme(
        colors = if (isDark) DarkTheme else LightTheme,
        typography = AppTypography
    ) {
        Surface {
            Column {
                content()
            }
        }
    }
}