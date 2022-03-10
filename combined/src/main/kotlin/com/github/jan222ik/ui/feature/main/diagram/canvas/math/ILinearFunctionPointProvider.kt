package com.github.jan222ik.ui.feature.main.diagram.canvas.math

import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.ChartDrawScope

/**
 * Provider for start and end Linear Function Points.
 */
fun interface ILinearFunctionPointProvider {
    /**
     * Provides the offsets of the start and end points of the linear function for a given viewport by the [ChartDrawScope].
     *
     * @receiver [ChartDrawScope] Contains the current [com.github.jan222ik.compose_mpp_charts.core.viewport.Viewport] in its context
     * @return [LinearFunctionResult] which pairs the calculated start and end offsets
     *
     */
    fun ChartDrawScope.provide(): LinearFunctionResult
}
