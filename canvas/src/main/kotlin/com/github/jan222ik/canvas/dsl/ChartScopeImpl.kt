package com.github.jan222ik.canvas.dsl

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.canvas.axis.abscissa.IAbscissaIAxisRenderer
import com.github.jan222ik.canvas.axis.ordinate.IOrdinateIAxisRenderer
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.dsl.axis.AbscissaBuilder
import com.github.jan222ik.canvas.dsl.axis.OrdinateBuilder
import com.github.jan222ik.canvas.grid.IGridRenderer
import com.github.jan222ik.canvas.interaction.IBoundingShape2D
import com.github.jan222ik.canvas.labels.provoder.ILabelProvider
import com.github.jan222ik.canvas.math.ILinearFunctionRenderer
import com.github.jan222ik.canvas.series.ISeriesRenderer

class ChartScopeImpl : ChartScope {
    val abscissaAxisRenderer = mutableListOf<IAbscissaIAxisRenderer>()
    val ordinateAxisRenderer = mutableListOf<IOrdinateIAxisRenderer>()
    val seriesMap: MutableMap<List<DataPoint>, ISeriesRenderer> = mutableMapOf()
    val gridRenderer: MutableList<IGridRenderer> = mutableListOf()
    val linearFunctionRenderer: MutableList<ILinearFunctionRenderer> = mutableListOf()
    val labels = mutableMapOf<ChartLabelSlot, Pair<ILabelProvider, @Composable (Pair<String, Float>) -> Unit>>()
    var onClickPopupLabel: (@Composable (offset: Offset, shape: IBoundingShape2D?) -> Boolean)? = null
    var onClick: ((offset: Offset, shape: IBoundingShape2D?) -> Unit)? = null


    override fun abscissaAxis(config: AbscissaBuilder.() -> Unit) {
        abscissaAxisRenderer.add(AbscissaBuilder().apply(config::invoke).buildAxisRenderer())
    }

    override fun ordinateAxis(config: (OrdinateBuilder.() -> Unit)?) {
        ordinateAxisRenderer.add(OrdinateBuilder().apply { config?.invoke(this) }.build())
    }

    override fun series(data: List<DataPoint>, renderer: ISeriesRenderer) {
        seriesMap[data] = renderer
    }

    override fun grid(renderer: IGridRenderer) {
        gridRenderer.add(renderer)
    }

    override fun linearFunction(renderer: ILinearFunctionRenderer) {
        linearFunctionRenderer.add(renderer)
    }

    override fun label(
        slot: ChartLabelSlot,
        labelProvider: ILabelProvider,
        content: @Composable (Pair<String, Float>) -> Unit
    ) {
        labels[slot] = labelProvider to content
    }

    override fun onClick(impl: (offset: Offset, shape: IBoundingShape2D?) -> Unit) {
        onClick = impl
    }

    override fun onClickPopupLabel(content: @Composable (offset: Offset, shape: IBoundingShape2D?) -> Boolean) {
        onClickPopupLabel = content
    }

}
