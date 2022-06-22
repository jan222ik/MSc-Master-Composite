package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.ScrollbarAdapter
import com.github.jan222ik.ui.adjusted.scroll.CanvasScrollState
import kotlin.math.roundToInt

class ScrollableScrollbarAdapter(
    val scrollState: CanvasScrollState,
    private val vertical: Boolean
) : ScrollbarAdapter {
    override val scrollOffset: Float get() = scrollState.value.toFloat()

    override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
        scrollState.scrollTo(scrollOffset.roundToInt())
    }

    override fun maxScrollOffset(containerSize: Int) =
        scrollState.maxValue.toFloat()

    fun updateViewport(value: Viewport) {
        scrollState.updateFromViewport(value, object : (Viewport) -> Int {
            override fun invoke(p1: Viewport): Int {
                return if (vertical) {
                    p1.origin.y
                } else {
                    p1.origin.x
                }.roundToInt()
            }
        })
    }
}