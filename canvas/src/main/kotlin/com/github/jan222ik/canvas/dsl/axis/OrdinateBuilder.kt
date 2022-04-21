@file:Suppress("FunctionName")

package com.github.jan222ik.canvas.dsl.axis

import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.axis.ordinate.IOrdinateIAxisRenderer
import com.github.jan222ik.canvas.axis.ordinate.ordinateAxisRenderer
import com.github.jan222ik.canvas.dsl.ChartScopeMarker
import com.github.jan222ik.canvas.line.ILineDrawer

@ChartScopeMarker
@AxisLineDrawerScopeMarker
class OrdinateBuilder(
    var location: Float = IOrdinateIAxisRenderer.Left,
    var axisLineDrawer: ILineDrawer = simpleAxisLineDrawer()
) {
    // Capital because they are constants
    @Suppress("PropertyName")
    val Left = IOrdinateIAxisRenderer.Left

    @Suppress("PropertyName")
    val Right = IOrdinateIAxisRenderer.Right

    fun drawer(
        builder: AxisLineDrawerBuilder = AxisLineDrawerBuilder(),
        config: (AxisLineDrawerBuilder.() -> Unit)? = null
    ): AxisLineDrawerBuilder {
        config?.invoke(builder)
        return builder.also {
            axisLineDrawer = simpleAxisLineDrawer(
                brush = it.brush,
                strokeWidth = it.strokeWidth,
                alpha = it.alpha,
                pathEffect = it.pathEffect,
                cap = it.cap,
                colorFilter = it.colorFilter,
                blendMode = it.blendMode
            )
        }
    }

    fun build(): IOrdinateIAxisRenderer {
        return ordinateAxisRenderer(
            location = location,
            axisLineDrawer = axisLineDrawer
        )
    }
}
