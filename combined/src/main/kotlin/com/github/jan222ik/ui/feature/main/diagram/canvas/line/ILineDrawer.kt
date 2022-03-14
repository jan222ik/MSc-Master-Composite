package com.github.jan222ik.ui.feature.main.diagram.canvas.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

fun interface ILineDrawer {
    fun DrawScope.draw(start: Offset, end: Offset)
}
