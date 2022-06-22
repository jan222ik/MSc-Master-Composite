package com.github.jan222ik.ui.value

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object EditorColors {
    val focusActive = Color(0xFF2675BF)
    val focusInactive = Color(0xFFD4D4D4)

    val backgroundGray = Color(0xFFF2F2F2)
    val dividerGray = Color(0xFFD1D1D1)
    val closeBtn = Color(0xFFE81123)

    val LightTheme = lightColors() // TODO

    val DarkTheme = darkColors(
        primary = R.color.PictonBlue,
        onPrimary = Color.White,
        secondary = R.color.Elephant,
        onSecondary = Color.White,
        surface = R.color.BigStone,
        error = R.color.WildWatermelon
    )
}