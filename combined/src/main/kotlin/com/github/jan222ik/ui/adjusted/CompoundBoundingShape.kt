package com.github.jan222ik.ui.adjusted

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

class CompoundBoundingShape(
    val children: List<BoundingRect>,
    override val debugName: String = Random.nextInt().toString()
) : IBoundingShape {

    override val topLeft: MutableState<Offset> = mutableStateOf(Offset.Zero)
    override val width: MutableState<Float> = mutableStateOf(0f)
    override val height: MutableState<Float> = mutableStateOf(0f)


    override fun isVisibleInViewport(viewport: Viewport): Boolean {
        return children.any { it.isVisibleInViewport(viewport) }
    }

    override fun containtsOffset(offset: Offset): Boolean {
        return children.any { it.containtsOffset(offset) }
    }
}