package com.github.jan222ik.ui.components.dnd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.ui.zIndex
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import kotlin.reflect.KFunction


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
                                text = "Test 0", modifier = Modifier.dndDraggable(
                                    handler = dnDHandler,
                                    dataProvider = { "Test 0" },
                                    onDragFinished = { success, snapBackAction ->
                                        if (success) {
                                            snapBackAction.invoke()
                                        }
                                    },
                                    onDragCancel = { snap -> snap.invoke() }
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
                                            color = MaterialTheme.colors.surface.takeUnless { hasDropHover }
                                                ?: Color.Cyan,
                                            modifier = Modifier
                                                .weight(1f)
                                                .dndDropTarget(
                                                    handler = dnDHandler,
                                                    dropActions = object : DnDAction {
                                                        override fun name(): String = "Surface $loop"

                                                        override fun dropEnter(data: Any?) {
                                                            println("Drop Enter $loop data: $data")
                                                            hasDropHover = true
                                                        }

                                                        override fun drop(pos: IntOffset, data: Any?): Boolean {
                                                            println("Drop $loop data: $data")
                                                            hasDropHover = false
                                                            return true
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
                                        .dndDropTarget(
                                            handler = dnDHandler,
                                            dropActions = object : DnDAction {
                                                override fun name() = "Outer"

                                                override fun dropEnter(data: Any?) {
                                                    println("Drop Enter outer data: $data")
                                                }

                                                override fun drop(pos: IntOffset, data: Any?): Boolean {
                                                    println("Drop outer data: $data")
                                                    return true
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
                                                .dndDropTarget(
                                                    handler = dnDHandler,
                                                    dropActions = object : DnDAction {
                                                        override fun name() = "Inner"
                                                        override fun dropEnter(data: Any?) {
                                                            println("Drop Enter inner data: $data")
                                                        }

                                                        override fun drop(pos: IntOffset, data: Any?): Boolean {
                                                            println("Drop inner data: $data")
                                                            return true
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