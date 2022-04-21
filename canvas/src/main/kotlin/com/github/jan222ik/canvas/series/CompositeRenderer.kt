package com.github.jan222ik.canvas.series

fun compositeRenderer(vararg renderers: ISeriesRenderer) = ISeriesRenderer { series ->
    renderers.map { with(it) { render(series) } }.flatten()
}
