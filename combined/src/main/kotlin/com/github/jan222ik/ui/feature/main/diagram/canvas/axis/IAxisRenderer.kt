package com.github.jan222ik.ui.feature.main.diagram.canvas.axis

import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 *  An AxisRenderer defines a render method with which the axis is drawn into the drawscope.
 */
fun interface IAxisRenderer {
    fun DrawScope.render()
}
