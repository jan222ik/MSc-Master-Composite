@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.playground.dragdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.ui.zIndex
import java.awt.Cursor
import kotlin.math.roundToInt

val LocalDropTargetHandler = compositionLocalOf<DnDHandler> { error("None present") }

val LocalPointerOverrideService = staticCompositionLocalOf<MutableState<PointerIcon?>> { error("Not provided in composition") }

@Composable
fun ProvidePointerIconChangeService(
    defaultPointer: PointerIcon = PointerIcon(Cursor.getDefaultCursor()),
    content: @Composable BoxScope.() -> Unit
) {
    val icon = remember { mutableStateOf<PointerIcon?>(null) }
    CompositionLocalProvider(
        LocalPointerOverrideService provides icon
    ) {
        Box(
            modifier = Modifier
            .fillMaxSize()
            .pointerHoverIcon(
                icon = icon.value ?: defaultPointer,
                overrideDescendants = false
            ),
            content = content
        )
    }
}


fun main() {
    val dnDHandler = DnDHandler()
    singleWindowApplication() {
        ProvidePointerIconChangeService {
            CompositionLocalProvider(
                LocalDropTargetHandler provides dnDHandler
            ) {
                Box(Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        dnDHandler.dropTargets.forEach {
                            drawRect(
                                Color.Red,
                                topLeft = it.first.positionInWindow(),
                                size = it.first.size.toSize(),
                                style = Stroke(width = 2f)
                            )
                        }
                    }) {
                    Row {
                        Column(Modifier.zIndex(1f).background(Color.Magenta)) {
                            Text(
                                "Drag form here! ${
                                    dnDHandler.dropTargets.joinToString(separator = "\n") { it.second.name() + " " + it.first.size }
                                }"
                            )
                            Text(
                                text = "Test 0", modifier = Modifier.dndDragable(
                                    handler = dnDHandler,
                                    dataProvider = { "Test 0" }
                                )
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.Gray)
                        ) {
                            Row {
                                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                                    repeat(3) { loop ->
                                        var hasDropHover by remember { mutableStateOf(false) }
                                        Surface(
                                            color = MaterialTheme.colors.surface.takeUnless { hasDropHover } ?: Color.Cyan,
                                            modifier = Modifier
                                                .weight(1f)
                                                .dropTarget(
                                                    handler = dnDHandler,
                                                    dropActions = object : DnDAction {
                                                        override fun name(): String = "Surface $loop"

                                                        override fun dropEnter(data: Any?) {
                                                            println("Drop Enter $loop data: $data")
                                                            hasDropHover = true
                                                        }

                                                        override fun drop(data: Any?) {
                                                            println("Drop $loop data: $data")
                                                        }

                                                        override fun dropExit() {
                                                            println("Drop Exit $loop")
                                                            hasDropHover = false
                                                        }
                                                    }
                                                )
                                        ) {
                                            Text("Surface $loop")
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.DarkGray)
                                        .dropTarget(
                                            handler = dnDHandler,
                                            dropActions = object : DnDAction {
                                                override fun name() = "Outer"

                                                override fun dropEnter(data: Any?) {
                                                    println("Drop Enter outer data: $data")
                                                }

                                                override fun drop(data: Any?) {
                                                    println("Drop outer data: $data")
                                                }

                                                override fun dropExit() {
                                                    println("Drop Exit outer")
                                                }
                                            }
                                        )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Surface(
                                            color = Color.Magenta,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .dropTarget(
                                                    handler = dnDHandler,
                                                    dropActions = object : DnDAction {
                                                        override fun name() = "Inner"
                                                        override fun dropEnter(data: Any?) {
                                                            println("Drop Enter inner data: $data")
                                                        }

                                                        override fun drop(data: Any?) {
                                                            println("Drop inner data: $data")
                                                        }

                                                        override fun dropExit() {
                                                            println("Drop Exit inner")
                                                        }
                                                    }
                                                )
                                        ) {
                                            Text("Surface inner", modifier = Modifier.padding(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun Modifier.dropTarget(
    handler: DnDHandler,
    dropActions: DnDAction
): Modifier {
    var state by remember { mutableStateOf<LayoutCoordinates?>(null) }
    return this.then(Modifier.onGloballyPositioned {
        if (state != it) {
            val list = handler.dropTargets.toMutableList()
            state?.to(dropActions)?.let(list::remove)
            list += it to dropActions
            handler.dropTargets = list
            state = it
        }
    })
}

@Composable
fun Modifier.dndDragable(
    handler: DnDHandler,
    dataProvider: () -> Any?
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
                    handler.drop(data)
                }
            )
        })
}

class DnDHandler() {
    var dropTargets by mutableStateOf<List<Pair<LayoutCoordinates, DnDAction>>>(emptyList())
    var activeTarget by mutableStateOf<Pair<LayoutCoordinates, DnDAction>?>(null)

    fun updateActiveTarget(offset: IntOffset, data: Any?) {
        val filter = dropTargets.filter { (coord, _) ->
            val pos = coord.positionInWindow()
            (pos.x..(pos.x + coord.size.width)).contains(offset.x.toFloat()) &&
                    (pos.y..(pos.y + coord.size.height)).contains(offset.y.toFloat())
        }
        val nActive = filter.maxByOrNull {
            it.first.positionInWindow().getDistanceSquared()
        }
        if (nActive != activeTarget) {
            activeTarget?.second?.dropExit()
            activeTarget = nActive
            activeTarget?.second?.dropEnter(data)
        }

    }


    fun drop(data: Any?) {
        activeTarget?.second?.drop(data)
        activeTarget = null
    }
}

interface DnDAction {
    fun name(): String
    fun dropEnter(data: Any?) {}
    fun drop(data: Any?) {}
    fun dropExit() {}
}

/*
                var dropped by remember { mutableStateOf("") }
                window.dropTarget = object : DropTarget() {

                    @Synchronized
                    override fun drop(dtde: DropTargetDropEvent?) {
                        dtde?.location
                        dtde?.acceptDrop(DnDConstants.ACTION_REFERENCE)
                        val transferData = dtde?.transferable?.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                        dropped = transferData.joinToString { (it as File).absolutePath }
                    }

                    @Synchronized
                    override fun dragOver(dtde: DropTargetDragEvent?) {
                        super.dragOver(dtde)
                        println("Over")
                    }
                }
 */