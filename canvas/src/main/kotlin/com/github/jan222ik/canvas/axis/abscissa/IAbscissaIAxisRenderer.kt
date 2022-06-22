package com.github.jan222ik.canvas.axis.abscissa

import com.github.jan222ik.canvas.axis.IAxisRenderer

/**
 * [IAxisRenderer] with abscissa specific constants.
 */
fun interface IAbscissaIAxisRenderer : com.github.jan222ik.canvas.axis.IAxisRenderer {
    companion object {
        const val Top = 0.0f
        const val Bottom = 1.0f
    }
}
