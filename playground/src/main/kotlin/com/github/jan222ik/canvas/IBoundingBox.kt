package com.github.jan222ik.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Double.sum
import kotlin.math.pow
import kotlin.math.sqrt

interface IBoundingBox {
    val size: IntSize
    val topLeft: Offset

    fun getVerticalAlignmentLines(): List<AlignmentLine.Vertical> {
        val left = AlignmentLine.Vertical(
            x = topLeft.x.toInt(),
            name = "left",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.START
        )
        val center = AlignmentLine.Vertical(
            x = topLeft.x.toInt() + size.width.div(2),
            name = "v-center",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.CENTER
        )
        val right = AlignmentLine.Vertical(
            x = topLeft.x.toInt() + size.width,
            name = "right",
            slot = AlignmentLine.Vertical.AlignVerticalSlot.END
        )
        return listOf(left, center, right)
    }

    fun getHorizontalAlignmentLines(): List<AlignmentLine.Horizontal> {
        val top = AlignmentLine.Horizontal(
            y = topLeft.y.toInt(),
            name = "top",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.TOP
        )
        val center = AlignmentLine.Horizontal(
            y = topLeft.y.toInt() + size.height.div(2),
            name = "h-center",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.CENTER
        )
        val bottom = AlignmentLine.Horizontal(
            y = topLeft.y.toInt() + size.height,
            name = "bottom",
            slot = AlignmentLine.Horizontal.AlignHorizontalSlot.BOTTOM
        )
        return listOf(top, center, bottom)

    }

    fun shortestDistanceTo(offset: Offset): Double {
        return listOf(
            topLeft,
            topLeft.copy(x = topLeft.x + size.width),
            topLeft.copy(y = topLeft.y + size.height),
            topLeft.copy(x = topLeft.x + size.width, y = topLeft.y + size.height)
        ).map { euclideanDist(start = offset, end = it) }.minOf { it }
    }

    fun euclideanDist(start: Offset, end: Offset): Double {
        return sqrt(sum(end.x.toDouble().minus(start.x).pow(2), end.y.toDouble().minus(start.y).pow(2)))
    }
}

data class RectangleBoundingBox(
    override val size: IntSize,
    override val topLeft: Offset
) : IBoundingBox

sealed class AlignmentLine {
    data class Horizontal(val y: Int, val name: String, val slot: AlignHorizontalSlot) : AlignmentLine() {
        enum class AlignHorizontalSlot {
            TOP, CENTER, BOTTOM
        }
    }

    data class Vertical(val x: Int, val name: String, val slot: AlignVerticalSlot) : AlignmentLine() {
        enum class AlignVerticalSlot {
            START, CENTER, END
        }
    }
}


class AlignmentHelper(
    val scope: CoroutineScope,
    val boundingBoxes: StateFlow<List<IBoundingBox>>,
) {
    val verticalLines: MutableList<Pair<AlignmentLine.Vertical, IBoundingBox>> = mutableListOf()
    val horizontalLines: MutableList<Pair<AlignmentLine.Horizontal, IBoundingBox>> = mutableListOf()

    val alignmentBlock: MutableStateFlow<List<IBoundingBox>> = MutableStateFlow(emptyList())
    val alignmentLines: MutableStateFlow<List<Pair<AlignmentLine, IBoundingBox>>> = MutableStateFlow(emptyList())

    init {
        val action: suspend (List<IBoundingBox>) -> Unit = { boxes ->
            verticalLines.clear()
            horizontalLines.clear()
            boxes.forEach { box ->
                verticalLines += box.getVerticalAlignmentLines().map { it to box }
                horizontalLines += box.getHorizontalAlignmentLines().map { it to box }
            }
        }
        boundingBoxes.onEach(action)
        boundingBoxes.value.let { scope.launch { action.invoke(it) } }
    }

    fun onPointerChange(offset: Offset, direction: AlignmentMoveDirection.Combined) {
        val hLines = when (direction.vertical) {
            AlignmentMoveDirection.Vertical.None -> emptyList()
            AlignmentMoveDirection.Vertical.South -> horizontalLines.filter { it.first.y >= offset.y }
            AlignmentMoveDirection.Vertical.North -> horizontalLines.filter { it.first.y <= offset.y }
        }
            .sortedBy { it.second.shortestDistanceTo(offset) }
            .take(2)

        val vLines = when (direction.horizontal) {
            AlignmentMoveDirection.Horizontal.None -> emptyList()
            AlignmentMoveDirection.Horizontal.West -> verticalLines.filter { it.first.x >= offset.x }
            AlignmentMoveDirection.Horizontal.East -> verticalLines.filter { it.first.x <= offset.x }
        }
            .sortedBy { it.second.shortestDistanceTo(offset) }
            .take(2)

        scope.launch {
            alignmentLines.emit(vLines + hLines)
        }
    }

    fun onPointerChange(offset: Offset) {
        val lines = boundingBoxes.value
            .associateBy { it.shortestDistanceTo(offset) }
            .entries
            .sortedBy { it.key }
            .take(2)
            .map { it.value }
        scope.launch { alignmentBlock.emit(lines) }
    }

    fun clear() {
        scope.launch { alignmentBlock.emit(emptyList()) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val example = RectangleBoundingBox(
        size = IntSize(50, 50),
        topLeft = Offset(50f, 50f)
    )
    val example2 = RectangleBoundingBox(
        size = IntSize(30, 50),
        topLeft = Offset(155f, 50f)
    )
    val example3 = RectangleBoundingBox(
        size = IntSize(10, 60),
        topLeft = Offset(60f, 390f)
    )
    val boxes = MutableStateFlow<List<IBoundingBox>>(listOf(example, example2, example3))
    singleWindowApplication {
        val boxesState = boxes.collectAsState()
        val scope = rememberCoroutineScope()
        val helper = remember(scope) { AlignmentHelper(scope, boxes) }
        val lines = helper.alignmentLines.collectAsState()
        val offsetState = remember { mutableStateOf(Offset.Zero) }
        val toAlign = RectangleBoundingBox(
            size = IntSize(30, 30),
            topLeft = offsetState.value
        )
        val moveDirection = remember { mutableStateOf(AlignmentMoveDirection.Combined.NONE) }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            val sufficientDrag = dragAmount.getDistanceSquared() > 1.0
                            val pos = change.position
                            val prevPos = change.previousPosition
                            offsetState.value = pos
                            val epsilon = 0.0
                            if (sufficientDrag) {
                                val movementDirection = AlignmentMoveDirection.Combined(
                                    vertical = when {
                                        prevPos.y - pos.y > epsilon -> AlignmentMoveDirection.Vertical.North
                                        prevPos.y - pos.y < -epsilon -> AlignmentMoveDirection.Vertical.South
                                        else -> AlignmentMoveDirection.Vertical.None
                                    },
                                    horizontal = when {
                                        prevPos.x - pos.x > epsilon -> AlignmentMoveDirection.Horizontal.East
                                        prevPos.x - pos.x < -epsilon -> AlignmentMoveDirection.Horizontal.West
                                        else -> AlignmentMoveDirection.Horizontal.None
                                    }
                                )
                                moveDirection.value = movementDirection
                            }
                            helper.onPointerChange(pos)
                            helper.onPointerChange(pos, moveDirection.value)
                        },
                        onDragEnd = {
                            helper.clear()
                        },
                        onDragCancel = {
                            helper.clear()
                        }
                    )
                }
        ) {
            boxesState.value.forEach {
                drawRect(Color.Magenta, topLeft = it.topLeft, size = it.size.toSize())
            }
            lines.value.forEach {
                drawAlignmentLine(it.first)
            }
            drawRect(Color.Cyan, topLeft = toAlign.topLeft, size = toAlign.size.toSize())
        }
    }
}

fun DrawScope.drawAlignmentLine(alignmentLine: AlignmentLine) {
    when (alignmentLine) {
        is AlignmentLine.Horizontal -> {
            val start = Offset(x = 0f, y = alignmentLine.y.toFloat())
            drawLine(Color.Black, start = start, end = start.copy(x = this.size.width))
        }
        is AlignmentLine.Vertical -> {
            val start = Offset(x = alignmentLine.x.toFloat(), y = 0f)
            drawLine(Color.Black, start = start, end = start.copy(y = this.size.height))
        }
    }
}

sealed class AlignmentMoveDirection {
    sealed class Horizontal : AlignmentMoveDirection() {
        object None : Horizontal()
        object West : Horizontal()
        object East : Horizontal()
    }

    sealed class Vertical : AlignmentMoveDirection() {
        object None : Vertical()
        object North : Vertical()
        object South : Vertical()
    }

    data class Combined(
        val vertical: Vertical = Vertical.None,
        val horizontal: Horizontal = Horizontal.None
    ) : AlignmentMoveDirection() {
        companion object {
            val NONE = Combined()
        }
    }
}