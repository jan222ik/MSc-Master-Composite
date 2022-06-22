package com.github.jan222ik.canvas.line

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.jan222ik.canvas.data.DrawPoint

fun interface IPathDrawer {
    fun DrawScope.draw(points: List<DrawPoint>)
}
