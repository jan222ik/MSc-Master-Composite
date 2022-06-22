package com.github.jan222ik.canvas.interaction

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.data.DrawPoint
import kotlin.math.pow

data class BoundingCircle(
    override val dataPoint: DataPoint,
    override val anchorRenderPoint: DrawPoint,
    val center: DrawPoint,
    val radius: Float
) : IBoundingShape2D {
    override fun containtsOffset(offset: Offset): Boolean {
        return radius.pow(2) - (center.x - offset.x).pow(2) - (center.y - offset.y).pow(2) >= 0f
    }
}
