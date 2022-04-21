package com.github.jan222ik.canvas.series

import com.github.jan222ik.canvas.canvas.ChartDrawScope
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.interaction.IBoundingShape2D

fun interface ISeriesRenderer {
    fun ChartDrawScope.render(series: List<DataPoint>): List<IBoundingShape2D>
}
