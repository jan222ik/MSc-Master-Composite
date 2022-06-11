package com.github.jan222ik.ui.adjusted

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.ui.adjusted.helper.AlignmentMoveDirection
import com.github.jan222ik.ui.adjusted.helper.AlignmentMoveDirection.Horizontal
import com.github.jan222ik.ui.adjusted.helper.AlignmentMoveDirection.Vertical
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

interface IBoundingShape {
    val topLeft: MutableState<Offset>
    val width: MutableState<Float>
    val height: MutableState<Float>
    val debugName: String

    fun isVisibleInViewport(viewport: Viewport): Boolean

    fun snapToAlignments(
        horizontalSnap: AlignmentLine.Horizontal?,
        verticalSnap: AlignmentLine.Vertical?,
        direction: AlignmentMoveDirection.Combined
    ) {
        val x = when {
            direction.vertical is Vertical.None || verticalSnap == null -> topLeft.value.x
            else ->  verticalSnap.x
        }.applySnapDirectionAdjustment(direction.vertical)
        val y = when {
            direction.horizontal is Horizontal.None || horizontalSnap == null -> topLeft.value.y
            else -> horizontalSnap.y
        }.applySnapDirectionAdjustment(direction.horizontal)
        println("topLeft = $topLeft snapTo = ${Offset(x, y)}, direction = $direction")
        topLeft.value = Offset(x, y)
    }

    private fun Float.applySnapDirectionAdjustment(direction: AlignmentMoveDirection) : Float {
        return when (direction) {
            is AlignmentMoveDirection.Combined -> throw IllegalArgumentException()
            Horizontal.East -> this + width.value
            Horizontal.None -> this
            Horizontal.West -> this
            Vertical.None -> this
            Vertical.North -> this
            Vertical.South -> this + height.value
        }
    }

    fun getVerticalAlignmentLines(): List<AlignmentLine.Vertical> {
        val left = AlignmentLine.Vertical(
            x = topLeft.value.x,
            name = "left",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.START
        )
        val center = AlignmentLine.Vertical(
            x = topLeft.value.x + width.value.div(2),
            name = "v-center",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.CENTER
        )
        val right = AlignmentLine.Vertical(
            x = topLeft.value.x + width.value,
            name = "right",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.END
        )
        return listOf(left, center, right)
    }

    fun getHorizontalAlignmentLines(): List<AlignmentLine.Horizontal> {
        val top = AlignmentLine.Horizontal(
            y = topLeft.value.y,
            name = "top",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.TOP
        )
        val center = AlignmentLine.Horizontal(
            y = topLeft.value.y + height.value.div(2),
            name = "h-center",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.CENTER
        )
        val bottom = AlignmentLine.Horizontal(
            y = topLeft.value.y + height.value,
            name = "bottom",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.BOTTOM
        )
        return listOf(top, center, bottom)

    }

    fun shortestDistanceTo(offset: Offset): Double {
        return listOf(
            topLeft.value,
            topLeft.value.copy(x = topLeft.value.x + width.value),
            topLeft.value.copy(y = topLeft.value.y + height.value),
            topLeft.value.copy(x = topLeft.value.x + width.value, y = topLeft.value.y + height.value)
        ).map { euclideanDist(start = offset, end = it) }.minOf { it }
    }

    fun euclideanDist(start: Offset, end: Offset): Double {
        return sqrt(
            java.lang.Double.sum(
                end.x.toDouble().minus(start.x).pow(2), end.y.toDouble().minus(start.y).pow(2)
            )
        )
    }
}

sealed class AlignmentLine {
    fun isInlineWith(movingBox: IBoundingShape, tolerance: Int = 0): Boolean =
        absoluteDifferenceTo(movingBox) <= tolerance

    abstract fun absoluteDifferenceTo(movingBox: IBoundingShape): Float

    data class Horizontal(val y: Float, val name: String, val slot: AlignHorizontalSlot) : AlignmentLine() {
        enum class AlignHorizontalSlot {
            TOP, CENTER, BOTTOM
        }

        override fun absoluteDifferenceTo(movingBox: IBoundingShape): Float {
            val movingY = movingBox.topLeft.value.y
            val diff = when (slot) {
                AlignHorizontalSlot.TOP -> y - movingY
                AlignHorizontalSlot.CENTER -> y - movingY.plus(movingBox.height.value.div(2))
                AlignHorizontalSlot.BOTTOM -> y - movingY.plus(movingBox.height.value)
            }
            return abs(diff)
        }
    }

    data class Vertical(val x: Float, val name: String, val slot: AlignVerticalSlot) : AlignmentLine() {
        enum class AlignVerticalSlot {
            START, CENTER, END
        }

        override fun absoluteDifferenceTo(movingBox: IBoundingShape): Float {
            val movingX = movingBox.topLeft.value.x
            val diff = when (slot) {
                AlignVerticalSlot.START -> x - movingX
                AlignVerticalSlot.CENTER -> x - movingX.plus(movingBox.width.value.div(2))
                AlignVerticalSlot.END -> x - movingX.plus(movingBox.width.value)
            }
            return abs(diff)
        }
    }
}


