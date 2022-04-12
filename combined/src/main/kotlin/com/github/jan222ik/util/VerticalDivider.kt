@file:Suppress("FunctionName")

package com.github.jan222ik.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * <a href="https://material.io/components/dividers" class="external" target="_blank">Material Design divider</a>.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Dividers image](https://developer.android.com/images/reference/androidx/compose/material/dividers.png)
 *
 * @param color color of the divider line
 * @param thickness thickness of the divider line, 1 dp is used by default. Using [Dp.Hairline]
 * will produce a single pixel divider regardless of screen density.
 * @param startIndent start offset of this line, no offset by default
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.let {
        if (it.isLight) {
            Color.Black
        } else it.onSurface.copy(alpha = DividerAlpha)
    },
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp
) {
    val indentMod = if (startIndent.value != 0f) {
        Modifier.padding(top = startIndent)
    } else {
        Modifier
    }
    val targetThickness = if (thickness == Dp.Hairline) {
        (1f / LocalDensity.current.density).dp
    } else {
        thickness
    }
    Box(
        modifier.then(indentMod)
            .width(targetThickness)
            .background(color = color)
    )
}

/**
 * <a href="https://material.io/components/dividers" class="external" target="_blank">Material Design divider</a>.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Dividers image](https://developer.android.com/images/reference/androidx/compose/material/dividers.png)
 *
 * @param color color of the divider line
 * @param thickness thickness of the divider line, 1 dp is used by default. Using [Dp.Hairline]
 * will produce a single pixel divider regardless of screen density.
 * @param startIndent start offset of this line, no offset by default
 */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.let {
        if (it.isLight) {
            Color.Black
        } else it.onSurface.copy(alpha = DividerAlpha)
    },
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp
) {
    val indentMod = if (startIndent.value != 0f) {
        Modifier.padding(top = startIndent)
    } else {
        Modifier
    }
    val targetThickness = if (thickness == Dp.Hairline) {
        (1f / LocalDensity.current.density).dp
    } else {
        thickness
    }
    Box(
        modifier.then(indentMod)
            .height(targetThickness)
            .background(color = color)
    )
}

private const val DividerAlpha = 0.12f
