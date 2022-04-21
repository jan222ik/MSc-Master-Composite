package com.github.jan222ik.canvas.grid

import com.github.jan222ik.canvas.canvas.ChartDrawScope

/**
 * IGridRenderer defines a render function on the ChartDrawScope.
 */
fun interface IGridRenderer {
    /**
     * Render call for a grid for given [ChartDrawScope].
     */
    fun ChartDrawScope.render()
}
