package com.github.jan222ik.canvas.line

import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.series.ISeriesRenderer
import com.github.jan222ik.canvas.viewport.Viewport


fun lineRenderer(drawer: IPathDrawer = simplePathDrawer()) = ISeriesRenderer { series ->
    series.getLineInViewport(context.viewport)
        .map { it.asDrawPoint(context) }
        .let { drawPoints ->
            with(this) {
                drawer.apply { draw(drawPoints) }
            }
        }
    return@ISeriesRenderer listOf()
}

fun List<DataPoint>.getLineInViewport(viewport: Viewport): List<DataPoint> {
    val result = mutableListOf<DataPoint>()
    var index = 0
    while (index < size) {
        val startIndex = indexOfFirstFrom(index) { viewport.contains(it) }
        if (startIndex < 0) return result

        if (startIndex > 0) result += get(startIndex - 1)
        val nextOutsideBounds = indexOfFirstFrom(startIndex) { !viewport.contains(it) }
        val endIndex = if (nextOutsideBounds == -1) {
            index = size
            size
        } else {
            index = nextOutsideBounds
            nextOutsideBounds + 1
        }
        result.addAll(subList(startIndex, endIndex))
    }
    return result
}

inline fun <T> List<T>.indexOfFirstFrom(fromIndex: Int, predicate: (T) -> Boolean): Int {
    require(fromIndex < this.size) { "Index out of bounds $fromIndex, size: ${this.size}" }
    require(fromIndex >= 0) { "Index cannot be negative." }
    for (i in fromIndex until this.size) {
        if (predicate(this[i])) return i
    }
    return -1
}
