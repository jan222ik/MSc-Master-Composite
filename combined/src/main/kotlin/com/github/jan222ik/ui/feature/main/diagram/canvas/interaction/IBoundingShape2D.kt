package com.github.jan222ik.ui.feature.main.diagram.canvas.interaction

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DataPoint
import com.github.jan222ik.ui.feature.main.diagram.canvas.data.DrawPoint

/**
 * Defines a shared basis for bounding shapes.
 * A bounding shape may be used to map a click-offset to an item in the graph.
 */
interface IBoundingShape2D {
    /**
     * Point ([DataPoint])  connected to the shape.
     */
    val dataPoint: DataPoint

    /**
     * Point ([DrawPoint]) that can be used for anchoring other elements.
     */
    val anchorRenderPoint: DrawPoint

    /**
     * Check if an offset in the canvas space is contained inside the bounding shape.
     */
    fun containtsOffset(offset: Offset): Boolean
}
