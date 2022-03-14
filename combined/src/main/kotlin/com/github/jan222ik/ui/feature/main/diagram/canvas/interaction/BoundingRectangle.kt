package com.github.jan222ik.ui.feature.main.diagram.canvas.interaction

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DataPoint
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DrawPoint

class BoundingRectangle(
    override val dataPoint: DataPoint,
    override val anchorRenderPoint: DrawPoint,
    val width: Float,
    val height: Float
) : IBoundingShape2D {
    override fun containtsOffset(offset: Offset): Boolean {
        return offset.x in anchorRenderPoint.x..anchorRenderPoint.x.plus(width) && offset.y in anchorRenderPoint.y..anchorRenderPoint.y.plus(height)
    }
}

