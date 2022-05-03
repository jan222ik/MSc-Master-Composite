package com.github.jan222ik.canvas.adjusted

import androidx.compose.ui.geometry.Offset

interface IBoundingShape {
    val topLeft: Offset
    val debugName: String

    fun isVisibleInViewport(viewport: Viewport) : Boolean
}