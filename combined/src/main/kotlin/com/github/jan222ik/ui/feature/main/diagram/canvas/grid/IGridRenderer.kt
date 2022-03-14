package com.github.jan222ik.ui.feature.main.diagram.canvas.grid

import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.ChartDrawScope

/**
 * IGridRenderer defines a render function on the ChartDrawScope.
 */
fun interface IGridRenderer {
    /**
     * Render call for a grid for given [ChartDrawScope].
     */
    fun ChartDrawScope.render()
}
