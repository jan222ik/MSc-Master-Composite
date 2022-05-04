package com.github.jan222ik.ui.adjusted

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset

interface IBoundingShape {
    val topLeft: MutableState<Offset>
    val debugName: String

    fun isVisibleInViewport(viewport: Viewport) : Boolean
}