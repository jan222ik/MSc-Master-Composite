package com.github.jan222ik.canvas.dsl.axis

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.line.ILineDrawer

@AxisLineDrawerScopeMarker
data class AxisLineDrawerBuilder(
    var brush: Brush = SolidColor(Color.Black),
    var strokeWidth: Float = Stroke.HairlineWidth,
    var alpha: Float = 1f,
    var pathEffect: PathEffect? = null,
    var cap: StrokeCap = Stroke.DefaultCap,
    var colorFilter: ColorFilter? = null,
    var blendMode: BlendMode = DrawScope.DefaultBlendMode,
) {
    fun toDrawer(): ILineDrawer {
        return simpleAxisLineDrawer(
            brush = brush,
            strokeWidth = strokeWidth,
            alpha = alpha,
            pathEffect = pathEffect,
            cap = cap,
            colorFilter = colorFilter,
            blendMode = blendMode
        )
    }
}
