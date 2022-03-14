package com.github.jan222ik.ui.feature.main.diagram.canvas.series

import com.github.jan222ik.ui.feature.main.diagram.canvas.interaction.IBoundingShape2D
import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.ChartDrawScope
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DataPoint

fun interface ISeriesRenderer {
    fun ChartDrawScope.render(series: List<DataPoint>) : List<IBoundingShape2D>
}
