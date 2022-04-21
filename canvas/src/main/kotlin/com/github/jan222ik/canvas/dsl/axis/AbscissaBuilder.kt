package com.github.jan222ik.canvas.dsl.axis

import com.github.jan222ik.canvas.axis.abscissa.IAbscissaIAxisRenderer
import com.github.jan222ik.canvas.axis.abscissa.abscissaAxisRenderer
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.dsl.ChartScopeMarker
import com.github.jan222ik.canvas.line.ILineDrawer


@ChartScopeMarker
@AxisLineDrawerScopeMarker
class AbscissaBuilder(
    var location: Float = IAbscissaIAxisRenderer.Bottom,
    var axisLineDrawer: ILineDrawer = simpleAxisLineDrawer(),
) {
    // Capital because they are constants
    @Suppress("PropertyName")
    val Top = IAbscissaIAxisRenderer.Top

    @Suppress("PropertyName")
    val Bottom = IAbscissaIAxisRenderer.Bottom

    fun drawer(
        builder: AxisLineDrawerBuilder = AxisLineDrawerBuilder(),
        config: (AxisLineDrawerBuilder.() -> Unit)? = null
    ): AxisLineDrawerBuilder {
        config?.invoke(builder)
        return builder.also {
            axisLineDrawer = it.toDrawer()
        }
    }

    fun buildAxisRenderer(): IAbscissaIAxisRenderer {
        return abscissaAxisRenderer(
            location = location,
            axisLineDrawer = axisLineDrawer
        )
    }
}
