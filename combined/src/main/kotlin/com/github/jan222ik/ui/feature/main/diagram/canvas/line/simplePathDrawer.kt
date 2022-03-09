package com.github.jan222ik.ui.feature.main.diagram.canvas.line

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke

fun simplePathDrawer(
    brush: Brush = SolidColor(Color.Black),
    style: DrawStyle = Stroke(width = 3f, cap = StrokeCap.Round),
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode
) = IPathDrawer { linePoints ->
    drawPath(
        path = Path().apply {
            linePoints.windowed(2) {
                moveTo(it[0].x, it[0].y)
                lineTo(it[1].x, it[1].y)
            }
            close()
        },
        brush = brush,
        style = style,
        alpha = alpha,
        colorFilter = colorFilter,
        blendMode = blendMode
    )
}
