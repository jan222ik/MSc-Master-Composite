package com.github.jan222ik.canvas.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.github.jan222ik.canvas.data.DataPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Viewport(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    val width = abs(maxX.minus(minX))
    val height = abs(maxY.minus(minY))

    val size: Size = Size(
        width = width,
        height = height
    )

    fun applyZoom(zoom: Float, direction: Offset, minSize: Size, maxSize: Size): Viewport {
        val dx = (width / zoom)
        val dy = (height / zoom)
        val ddx = (width - dx) / 2 * direction.x
        val ddy = (height - dy) / 2 * direction.y
        var minX = minX + ddx
        var maxX = maxX - ddx
        var minY = minY + ddy
        var maxY = maxY - ddy
        val width = abs(maxX - minX)
        val height = abs(maxY - minY)

        if (width < minSize.width) {
            val diff = abs(minSize.width - width) / 2
            minX -= diff
            maxX += diff
        }
        if (height < minSize.height) {
            val diff = abs(minSize.height - height) / 2
            minY -= diff
            maxY += diff
        }

        if (width > maxSize.width) {
            val diff = abs(width - maxSize.width) / 2
            minX += diff
            maxX -= diff
        }
        if (height > maxSize.height) {
            val diff = abs(height - maxSize.height) / 2
            minY += diff
            maxY -= diff
        }
        return Viewport(minX = minX, maxX = maxX, minY = minY, maxY = maxY)
    }

    fun applyPan(panX: Float, panY: Float, maxViewport: Viewport): Viewport {
        var newMinX = minX + panX
        var newMaxX = maxX + panX
        if (newMinX < maxViewport.minX) {
            newMaxX += abs(maxViewport.minX - newMinX)
            newMinX = maxViewport.minX
        }
        if (newMaxX > maxViewport.maxX) {
            newMinX -= abs(maxViewport.maxX - newMaxX)
            newMaxX = maxViewport.maxX
        }

        var newMinY = minY + panY
        var newMaxY = maxY + panY
        if (newMinY < maxViewport.minY) {
            newMaxY += abs(maxViewport.minY - newMinY)
            newMinY = maxViewport.minY
        }
        if (newMaxY > maxViewport.maxY) {
            newMinY -= abs(maxViewport.maxY - newMaxY)
            newMaxY = maxViewport.maxY
        }

        return Viewport(minX = newMinX, maxX = newMaxX, minY = newMinY, maxY = newMaxY)
    }

    fun contains(x: Float, y: Float) = x in minX..maxX && y in minY..maxY
    fun contains(dataPoint: DataPoint) = contains(x = dataPoint.x, y = dataPoint.y)

    operator fun plus(other: Viewport) = Viewport(
        minX = min(minX, other.minX),
        maxX = max(maxX, other.maxX),
        minY = min(minY, other.minY),
        maxY = max(maxY, other.maxY)
    )

    override fun toString(): String {
        return "Viewport(minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY)"
    }


}
