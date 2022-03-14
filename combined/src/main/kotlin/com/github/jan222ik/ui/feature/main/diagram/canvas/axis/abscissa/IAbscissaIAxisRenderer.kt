package com.github.jan222ik.ui.feature.main.diagram.canvas.axis.abscissa

import com.github.jan222ik.ui.feature.main.diagram.canvas.axis.IAxisRenderer

/**
 * [IAxisRenderer] with abscissa specific constants.
 */
fun interface IAbscissaIAxisRenderer : IAxisRenderer {
    companion object {
        const val Top = 0.0f
        const val Bottom = 1.0f
    }
}
