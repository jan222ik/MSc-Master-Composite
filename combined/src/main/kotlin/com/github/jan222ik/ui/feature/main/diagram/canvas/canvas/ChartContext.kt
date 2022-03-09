package com.github.jan222ik.ui.feature.main.diagram.canvas.canvas

import androidx.compose.ui.geometry.Size
import com.github.jan222ik.ui.feature.main.diagram.canvas.viewport.Viewport

data class ChartContext(
    val canvasSize: Size,
    val scaleX: Float,
    val scaleY: Float,
    val viewport: Viewport,
) {
    fun Float.toRendererX() = (this - viewport.minX) * scaleX

    fun Float.toRendererY() = canvasSize.height - (this - viewport.minY) * scaleY

    fun Float.toChartX() = this / scaleX + viewport.minX

    fun Float.toChartY() = (canvasSize.height - this) / scaleY - viewport.minY

    fun toRendererWidth(width: Float) = width * scaleX

    fun toRendererHeight(height: Float) = height * scaleY

    fun toChartWidth(width: Float) = width / scaleX

    fun toChartHeight(height: Float) = height / scaleY

    companion object {
        fun of(
            viewport: Viewport,
            canvasSize: Size,
        ) = ChartContext(
            canvasSize = canvasSize,
            scaleX = canvasSize.width / viewport.width,
            scaleY = canvasSize.height / viewport.height,
            viewport = viewport
        )
    }
}

