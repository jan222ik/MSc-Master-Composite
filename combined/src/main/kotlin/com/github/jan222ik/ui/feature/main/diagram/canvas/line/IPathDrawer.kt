package com.github.jan222ik.ui.feature.main.diagram.canvas.line

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DrawPoint

fun interface IPathDrawer {
    fun DrawScope.draw(points: List<DrawPoint>)
}
