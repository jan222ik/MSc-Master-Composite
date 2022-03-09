package com.github.jan222ik.ui.feature.main.diagram.canvas.axis.drawer

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

fun simpleAxisLineDrawer(
    brush: Brush = SolidColor(Color.Black),
    strokeWidth: Float = Stroke.HairlineWidth,
    alpha: Float = 1f,
    pathEffect: PathEffect? = null,
    cap: StrokeCap = Stroke.DefaultCap,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
) = IAxisLineDrawer { start, end ->
    drawLine(
        brush = brush,
        strokeWidth = strokeWidth,
        pathEffect = pathEffect,
        cap = cap,
        alpha = alpha,
        start = start,
        end = end,
        colorFilter = colorFilter,
        blendMode = blendMode
    )
}
