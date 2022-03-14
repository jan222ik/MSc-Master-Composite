package com.github.jan222ik.compose_mpp_charts.core.series

import com.github.jan222ik.ui.feature.main.diagram.canvas.series.ISeriesRenderer

fun compositeRenderer(vararg renderers: ISeriesRenderer) = ISeriesRenderer { series ->
    renderers.map { with(it) { render(series) } }.flatten()
}
