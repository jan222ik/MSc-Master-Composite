package com.github.jan222ik.ui.adjusted.helper

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import com.github.jan222ik.ui.adjusted.AlignmentLine
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.IBoundingShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AlignmentHelper(
    val scope: CoroutineScope
) {
    val alignmentLines: MutableStateFlow<List<Pair<AlignmentLine, IBoundingShape>>> = MutableStateFlow(emptyList())

    val movingBox: MutableStateFlow<IBoundingShape?> = MutableStateFlow(null)
    private val moveDirection: MutableStateFlow<AlignmentMoveDirection.Combined> = MutableStateFlow(AlignmentMoveDirection.Combined.NONE)

    val boundingBoxes: MutableStateFlow<List<IBoundingShape>> = MutableStateFlow(emptyList())


    fun onPointerChange(
        offset: Offset,
    ) {
        val direction: AlignmentMoveDirection.Combined = moveDirection.value
        val movingBoxVal = movingBox.value ?: return

        val nonMovingBoxes = boundingBoxes.value.filterNot { it == movingBoxVal }
        val horizontalLines = nonMovingBoxes
            .flatMap { box -> box.getHorizontalAlignmentLines().map { it to box } }

        val verticalLines = nonMovingBoxes
            .flatMap { box -> box.getVerticalAlignmentLines().map { it to box } }

        val horizontalDirectMatches = horizontalLines
            .associateBy { it.first.absoluteDifferenceTo(movingBoxVal) }
            .filter { it.key < 5 }


        val verticalDirectMatches = verticalLines
            .associateBy { it.first.absoluteDifferenceTo(movingBoxVal) }
            .filter { it.key < 5 }


        val horizontalSnap = if (horizontalDirectMatches.isNotEmpty() && horizontalDirectMatches.none { it.key < 0 }) {
            val hLines = horizontalLines.filter { it.second == movingBoxVal }
            when (direction.vertical) {
                AlignmentMoveDirection.Vertical.None -> emptyList()
                AlignmentMoveDirection.Vertical.South -> hLines.filter { it.first.y >= offset.y }
                AlignmentMoveDirection.Vertical.North -> hLines.filter { it.first.y <= offset.y }
            }
                .minByOrNull { it.second.shortestDistanceTo(offset) }
                ?.takeIf { it.second.shortestDistanceTo(offset) < 5 }
                ?.first
        } else null

        val verticalSnap = if (verticalDirectMatches.isNotEmpty() && verticalDirectMatches.none { it.key < 0 }) {
            val vLines = verticalLines.filter { it.second == movingBoxVal }
            when (direction.horizontal) {
                AlignmentMoveDirection.Horizontal.None -> emptyList()
                AlignmentMoveDirection.Horizontal.West -> vLines.filter { it.first.x >= offset.x }
                AlignmentMoveDirection.Horizontal.East -> vLines.filter { it.first.x <= offset.x }
            }
                .minByOrNull { it.second.shortestDistanceTo(offset) }
                ?.takeIf { it.second.shortestDistanceTo(offset) < 5 }
                ?.first
        } else null

        if (horizontalSnap != null || verticalSnap != null) {
            println("horizontalSnap = ${horizontalSnap}")
            println("verticalSnap = ${verticalSnap}")
            movingBoxVal.snapToAlignments(horizontalSnap, verticalSnap, direction)
        } else {

            val hLines = horizontalDirectMatches.values.ifEmpty {
                when (direction.vertical) {
                    AlignmentMoveDirection.Vertical.None -> emptyList()
                    AlignmentMoveDirection.Vertical.South -> horizontalLines.filter { it.first.y >= offset.y }
                    AlignmentMoveDirection.Vertical.North -> horizontalLines.filter { it.first.y <= offset.y }
                }
                    .sortedBy { it.second.shortestDistanceTo(offset) }
                    .take(2)
            }

            val vLines = verticalDirectMatches.values.ifEmpty {
                when (direction.horizontal) {
                    AlignmentMoveDirection.Horizontal.None -> emptyList()
                    AlignmentMoveDirection.Horizontal.West -> verticalLines.filter { it.first.x >= offset.x }
                    AlignmentMoveDirection.Horizontal.East -> verticalLines.filter { it.first.x <= offset.x }
                }
                    .sortedBy { it.second.shortestDistanceTo(offset) }
                    .take(2)
            }

            scope.launch {
                alignmentLines.emit(vLines + hLines)
            }
        }
    }

    fun clear() {
        scope.launch {
            movingBox.emit(null)
            alignmentLines.emit(emptyList())
        }
    }

    fun updateDirection(change: PointerInputChange, dragAmount: Offset) {
        val sufficientDrag = dragAmount.getDistanceSquared() > 1.0
        val pos = change.position
        val prevPos = change.previousPosition
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
    }

    suspend fun detectDragGesture(pointerInputScope: PointerInputScope) {
        with(pointerInputScope) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    val movingBox = movingBox.value ?: return@detectDragGestures
                    updateDirection(change, dragAmount)
                    if (movingBox is BoundingRect) {
                        movingBox.addX(dragAmount.x)
                        movingBox.addY(dragAmount.y)

                        onPointerChange(
                            offset = movingBox.topLeft.value,
                        )
                    }
                },
                onDragEnd = {
                    clear()
                },
                onDragCancel = {
                    clear()
                }
            )
        }
    }

}