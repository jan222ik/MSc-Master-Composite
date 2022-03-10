package com.github.jan222ik.ui.feature.main.diagram.canvas.canvas

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.ChartContext

class ChartDrawScope(
    drawScope: DrawScope,
    val context: ChartContext
) : DrawScope by drawScope
