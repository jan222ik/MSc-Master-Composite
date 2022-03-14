package com.github.jan222ik.ui.feature.main.diagram.canvas.data

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.ChartContext

/**
 * Base Data representation for a point.
 * @property x x-Position of the point in its reference system
 * @property y y-Position of the point in its reference system
 * @property offset Getter for a offset with x and y
 */
interface Point {
    val x: Float
    val y: Float

    /**
     * Checks if the reference system of the Point is in the Render/Draw space.
     */
    fun isRenderPoint() = this::class == DrawPoint::class

    val offset: Offset
        get() = Offset(x, y)
}

/**
 * Representation of a point in the Render/Draw space.
 */
data class DrawPoint(
    override val x: Float,
    override val y: Float
) : Point {
    /**
     * Converts this [DrawPoint] into a [DataPoint]
     * @param ctx [ChartContext] to be used during conversion
     * @return Conversion result as [DataPoint]
     */
    fun asDataPoint(ctx: ChartContext): DataPoint = with(ctx) {
        return DataPoint(
            x = x.toChartX(),
            y = y.toChartY()
        )
    }
}

data class DataPoint(
    override val x: Float,
    override val y: Float
) : Point {
    /**
     * Converts this [DataPoint] into a [DrawPoint]
     * @param ctx [ChartContext] to be used during conversion
     * @return Conversion result as [DataPoint]
     */
    fun asDrawPoint(ctx: ChartContext): DrawPoint = with(ctx) {
        return DrawPoint(
            x = x.toRendererX(),
            y = y.toRendererY()
        )
    }

}


fun pointAt(x: Float, y: Float) = DataPoint(x, y)
