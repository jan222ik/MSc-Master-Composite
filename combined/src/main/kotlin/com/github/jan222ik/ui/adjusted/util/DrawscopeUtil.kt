package com.github.jan222ik.ui.adjusted.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

inline fun DrawScope.translate(offset: Offset, block: DrawScope.() -> Unit) {
    drawContext.transform.translate(left = offset.x, top = offset.y)
    block()
    drawContext.transform.translate(left = -offset.x, top = -offset.y)
}