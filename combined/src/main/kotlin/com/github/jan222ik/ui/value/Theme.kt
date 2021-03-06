package com.github.jan222ik.ui.value

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    isDark: Boolean = true, // TODO: If you want to support both light theme and dark theme, you'll need to implement it manually.
    content: @Composable ColumnScope.() -> Unit,
) {
    MaterialTheme(
        colors = if (isDark) EditorColors.DarkTheme else EditorColors.LightTheme,
        typography = AppTypography
    ) {
        Surface {
            Column {
                content()
            }
        }
    }
}