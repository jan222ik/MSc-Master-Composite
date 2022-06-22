package com.github.jan222ik.ui.adjusted

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

data class Viewport(
    val origin: Offset = Offset.Zero,
    val size: Size = Size.Zero
) {
    fun applyPan(pan: Offset, maxViewportSize: Size): Viewport {
        val nX = origin.x - pan.x
        val nY = origin.y - pan.y
        return this.copy(
            origin = origin.copy(
                x = nX.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.width.minus(size.width)
                ),
                y = nY.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.height.minus(size.height)
                )
            )
        )
    }

    fun moveTo(pos: Offset, maxViewportSize: Size): Viewport {
        return this.copy(
            origin = origin.copy(
                x = pos.x.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.width.minus(size.width)
                ),
                y = pos.y.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.height.minus(size.height)
                )
            )
        )
    }
}