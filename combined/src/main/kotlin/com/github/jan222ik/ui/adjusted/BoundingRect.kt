package com.github.jan222ik.ui.adjusted

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.util.packFloats
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import kotlin.random.Random

class BoundingRect(
    initTopLeft: Offset,
    initWidth: Float,
    initHeight: Float,
    override val debugName: String = Random.nextInt().toString()
) : IBoundingShape {
    override val topLeft: MutableState<Offset> = mutableStateOf(initTopLeft)
    override val width: MutableState<Float> = mutableStateOf(initWidth)
    override val height: MutableState<Float> = mutableStateOf(initHeight)
    fun drawWireframe(
        drawScope: DrawScope,
        color: Color = Color.Cyan,
        fill: Boolean,
        showText: Boolean,
        showBox: Boolean
    ) {
        drawScope.translate(
            left = topLeft.value.x,
            top = topLeft.value.y
        ) {
            if (showBox) {
                drawRect(
                    color = color,
                    style = if (fill) Fill else Stroke(),
                    topLeft = Offset.Zero,
                    size = Size(width = width.value, height = height.value)
                )
                drawLine(
                    color = color,
                    start = Offset.Zero,
                    end = Offset(width.value, height.value)
                )
                drawLine(
                    color = color,
                    start = Offset(0f, height.value),
                    end = Offset(width.value, 0f)
                )
            }
            if (showText) {
                val textLine = TextLine.make(debugName, Font())
                this.drawContext.canvas.nativeCanvas.drawTextLine(
                    textLine,
                    0f,
                    0f,
                    Paint().asFrameworkPaint()
                )
            }
        }
    }

    override fun isVisibleInViewport(viewport: Viewport): Boolean {
        val aBottomRight = topLeft.value.plus(Offset(width.value, height.value))
        val viewPortBottomRight = viewport.origin.plus(viewport.size.toOffset())
        val aTop = topLeft.value.y
        val aLeft = topLeft.value.x
        val aBottom = aBottomRight.y
        val aRight = aBottomRight.x
        val bTop = viewport.origin.y
        val bLeft = viewport.origin.x
        val bBottom = viewPortBottomRight.y
        val bRight = viewPortBottomRight.x
        //Cond1. If A's left edge is to the right of the B's right edge, - then A is Totally to right Of B
        val cond1 = aLeft >= bRight
        //Cond2. If A's right edge is to the left of the B's left edge, - then A is Totally to left Of B
        val cond2 = aRight <= bLeft
        //Cond3. If A's top edge is below B's bottom edge, - then A is Totally below B
        val cond3 = aTop >= bBottom
        //Cond4. If A's bottom edge is above B's top edge, - then A is Totally above B
        val cond4 = aBottom <= bTop
        return !(cond1 || cond2 || cond3 || cond4)
    }

    fun addWidth(amount: Float) {
        width.value = width.value.plus(amount).coerceAtLeast(5f)
    }

    fun addHeight(amount: Float) {
        height.value = height.value.plus(amount).coerceAtLeast(5f)
    }

    fun addX(amount: Float) {
        topLeft.value = topLeft.value.addX(amount)
    }

    fun addY(amount: Float) {
        topLeft.value = topLeft.value.addY(amount)
    }

    fun move(amount: Offset) {
        val tmp = topLeft.value.addX(amount.x)
        topLeft.value = tmp.addY(amount.y)
    }

    private fun Offset.addX(
        amount: Float,
        coerceAtLeast: Float = 0f,
        coerceAtMost: Float = ScrollableCanvasDefaults.viewportSizeMaxWidth
    ) = this.copy(x = (this.x + amount).coerceIn(coerceAtLeast, coerceAtMost))

    private fun Offset.addY(
        amount: Float,
        coerceAtLeast: Float = 0f,
        coerceAtMost: Float = ScrollableCanvasDefaults.viewportSizeMaxHeight
    ) = this.copy(y = (this.y + amount).coerceIn(coerceAtLeast, coerceAtMost))

    fun toState(): BoundingRectState {
        return BoundingRectState(topLeft.value.let { packFloats(it.x, it.y) }, width.value, height.value)
    }

    fun updateFromState(state: BoundingRectState): BoundingRect {
        topLeft.value = state.topLeft
        width.value = state.width
        height.value = state.height
        return this
    }
}

