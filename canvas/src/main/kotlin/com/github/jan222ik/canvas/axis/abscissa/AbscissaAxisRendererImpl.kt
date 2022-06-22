package com.github.jan222ik.canvas.axis.abscissa

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.line.ILineDrawer

/**
 * Renderer for an abscissa axis ([IAbscissaIAxisRenderer]).
 * @param location Defines the location of the axis.
 * A value of 0 is at the top and a value of 1 is ate the bottom of the chart
 * @param axisLineDrawer [ILineDrawer] which will be used to draw the abscissa line
 */
fun abscissaAxisRenderer(
    location: Float = IAbscissaIAxisRenderer.Bottom,
    axisLineDrawer: ILineDrawer = simpleAxisLineDrawer()
) = IAbscissaIAxisRenderer {
    val y = location * size.height
    with(axisLineDrawer) {
        draw(
            start = Offset(x = 0f, y = y),
            end = Offset(x = size.width, y = y)
        )
    }
}

/**
 * Alias for [IAbscissaIAxisRenderer].
 * Introduced to offer more language agnostic completion.
 */
typealias IHorizontalAxisRenderer = IAbscissaIAxisRenderer

/**
 * Alias for [abscissaAxisRenderer] function.
 * Renderer for an horizontal axis ([IAbscissaIAxisRenderer]).
 * @param location Defines the location of the axis.
 * A value of 0 ([IHorizontalAxisRenderer.Bottom]) is at the top and a value of 1 is ate the bottom of the chart
 * @param axisLineDrawer [ILineDrawer] which will be used to draw the horizontal line
 */
fun horizontalAxisRenderer(
    location: Float = IAbscissaIAxisRenderer.Bottom,
    axisLineDrawer: ILineDrawer = simpleAxisLineDrawer()
) = abscissaAxisRenderer(location, axisLineDrawer)
