package com.github.jan222ik.canvas.grid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.line.ILineDrawer

/**
 * Grid Renderer for a Integer-based grid, provided drawers are used to draw the ordinate and abscissa axis.
 *
 * @param abscissaDrawer defines an [ILineDrawer] to draw the abscissa lines of the grid.
 * A null value disables the drawing of the lines.
 * @param ordinateDrawer defines an [ILineDrawer] to draw the ordinate lines of the grid.
 * A null value disables the drawing of the lines.
 * @param drawOrdinateFirst if true, the ordinate lines will be drawn before the abscissa axis.
 * Thus, the abscissa will be drawn above the ordinate.
 * @param stepAbscissa Step between the abscissa lines
 * @param stepOrdinate Step between the ordinate lines
 */
fun intGridRenderer(
    abscissaDrawer: ILineDrawer? = simpleAxisLineDrawer(brush = SolidColor(Color.Gray)),
    ordinateDrawer: ILineDrawer? = simpleAxisLineDrawer(brush = SolidColor(Color.Gray)),
    drawOrdinateFirst: Boolean = true,
    stepAbscissa: Int = 1,
    stepOrdinate: Int = 1,
) = IGridRenderer {
    /**
     * Returns a list of the line positions for the int-based grid.
     * @param min Minimum value in the viewport to account for
     * @param max Maximum value in the viewport to account for
     * @param step Step between the lines
     *
     * @return List of the line positions
     */
    /**
     * Returns a list of the line positions for the int-based grid.
     * @param min Minimum value in the viewport to account for
     * @param max Maximum value in the viewport to account for
     * @param step Step between the lines
     *
     * @return List of the line positions
     */
    /**
     * Returns a list of the line positions for the int-based grid.
     * @param min Minimum value in the viewport to account for
     * @param max Maximum value in the viewport to account for
     * @param step Step between the lines
     *
     * @return List of the line positions
     */
    /**
     * Returns a list of the line positions for the int-based grid.
     * @param min Minimum value in the viewport to account for
     * @param max Maximum value in the viewport to account for
     * @param step Step between the lines
     *
     * @return List of the line positions
     */
    fun calcLinesBetween(min: Float, max: Float, step: Int): List<Float> {
        return IntProgression
            .fromClosedRange(rangeStart = min.toInt(), rangeEnd = max.toInt(), step = step)
            .map(Int::toFloat)
    }

    val (minX, minY, maxX, maxY) = context.viewport

    val ordinate: (ILineDrawer) -> Unit = { r ->
        calcLinesBetween(minX, maxX, stepOrdinate)
            .map {
                val x = with(context) { it.toRendererX() }
                val start = Offset(x = x, y = 0f)
                val end = Offset(x = x, y = context.canvasSize.height)
                start to end
            }
            .forEach { with(r) { draw(it.first, it.second) } }
    }

    val abscissa: (ILineDrawer) -> Unit = { r ->
        calcLinesBetween(minY, maxY, stepAbscissa)
            .map {
                val y = with(context) { it.toRendererY() }
                val start = Offset(x = 0f, y = y)
                val end = Offset(x = context.canvasSize.width, y = y)
                start to end
            }
            .forEach { with(r) { draw(it.first, it.second) } }
    }

    if (drawOrdinateFirst) {
        ordinateDrawer?.let(ordinate)
        abscissaDrawer?.let(abscissa)
    } else {
        abscissaDrawer?.let(abscissa)
        ordinateDrawer?.let(ordinate)
    }
}
