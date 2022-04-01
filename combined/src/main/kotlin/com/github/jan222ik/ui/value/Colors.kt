package com.github.jan222ik.ui.value

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

object Colors {
    val focusActive = Color(0xFF2675BF)
    val focusInactive = Color(0xFFD4D4D4)

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