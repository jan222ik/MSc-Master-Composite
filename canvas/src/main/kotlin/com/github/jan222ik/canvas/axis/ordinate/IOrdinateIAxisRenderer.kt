package com.github.jan222ik.canvas.axis.ordinate

import com.github.jan222ik.canvas.axis.IAxisRenderer

/**
 * [IAxisRenderer] with ordinate specific constants.
 */
fun interface IOrdinateIAxisRenderer : com.github.jan222ik.canvas.axis.IAxisRenderer {
    companion object {
        const val Right = 1f
        const val Left = 0f
    }
}
