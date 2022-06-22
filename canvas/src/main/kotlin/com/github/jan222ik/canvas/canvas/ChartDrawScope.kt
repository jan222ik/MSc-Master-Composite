package com.github.jan222ik.canvas.canvas

import androidx.compose.ui.graphics.drawscope.DrawScope

class ChartDrawScope(
    drawScope: DrawScope,
    val context: ChartContext
) : DrawScope by drawScope
