package com.github.jan222ik.ui.components.dnd

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import java.awt.Cursor
import kotlin.math.roundToInt

@Composable
fun Modifier.dndDraggable(
    handler: DnDHandler,
    dataProvider: () -> Any?,
    onDragFinished: (Boolean, () -> Unit) -> Unit
): Modifier {
    var offset by remember { mutableStateOf(IntOffset.Zero) }
    var pos by remember { mutableStateOf(IntOffset.Zero) }
    var data by remember { mutableStateOf<Any?>(null) }

    return this.then(Modifier
        .onGloballyPositioned {
            val wPos = it.positionInWindow()
            pos = IntOffset(wPos.x.roundToInt(), wPos.y.roundToInt())

        }
        .pointerHoverIcon(icon = PointerIcon(Cursor(Cursor.HAND_CURSOR)))
        .offset { offset }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    data = dataProvider.invoke()
                },
                onDrag = { change, dragAmount ->
                    offset = IntOffset(
                        x = offset.x + dragAmount.x.roundToInt(),
                        y = offset.y + dragAmount.y.roundToInt()
                    )

                    handler.updateActiveTarget(offset + pos, data)

                },
                onDragEnd = {
                    handler.drop(data)?.let {
                        onDragFinished(it) { offset = IntOffset.Zero }
                    }
                }
            )
        })
}