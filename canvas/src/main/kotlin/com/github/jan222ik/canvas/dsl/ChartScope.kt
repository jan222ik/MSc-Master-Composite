package com.github.jan222ik.canvas.dsl

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.canvas.dsl.axis.AbscissaBuilder
import com.github.jan222ik.canvas.labels.provoder.ILabelProvider
import com.github.jan222ik.canvas.math.ILinearFunctionRenderer
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.dsl.axis.OrdinateBuilder
import com.github.jan222ik.canvas.grid.IGridRenderer
import com.github.jan222ik.canvas.interaction.IBoundingShape2D
import com.github.jan222ik.canvas.series.ISeriesRenderer

@ChartScopeMarker
interface ChartScope {

    fun abscissaAxis(config: AbscissaBuilder.() -> Unit)
    fun xAxis(config: AbscissaBuilder.() -> Unit) = abscissaAxis(config)

    fun ordinateAxis(config: (OrdinateBuilder.() -> Unit)? = null)
    fun yAxis(config: (OrdinateBuilder.() -> Unit)? = null) = ordinateAxis(config)

    fun series(data: List<DataPoint>, renderer: ISeriesRenderer)

    fun grid(renderer: IGridRenderer)

    fun linearFunction(renderer: ILinearFunctionRenderer)

    fun label(slot: ChartLabelSlot, labelProvider: ILabelProvider, content: @Composable (Pair<String, Float>) -> Unit)

    fun onClick(impl: (offset: Offset, data: IBoundingShape2D?) -> Unit)

    /**
     * Return of lambda indicates if the click is consumed
     */
    fun onClickPopupLabel(content: @Composable (offset: Offset, shape: IBoundingShape2D?) -> Boolean)

}
