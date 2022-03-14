package com.github.jan222ik.ui.feature.main.diagram.canvas.axis.ordinate

import com.github.jan222ik.ui.feature.main.diagram.canvas.axis.IAxisRenderer

/**
 * [IAxisRenderer] with ordinate specific constants.
 */
fun interface IOrdinateIAxisRenderer : IAxisRenderer {
    companion object {
        const val Right = 1f
        const val Left = 0f
    }
}
