package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.feature.main.menu_tool_bar.mapPair

object DebugCanvas {
    @Composable
    fun debugWindow(
        viewport: MutableState<Viewport>,
        maxViewportSize: Size,
        elements: List<ICanvasComposable>,
        arrows: List<Arrow>,
        selectedBoundingBoxes: MutableState<List<IBoundingShape>>,
        conditionalClipValue: MutableState<Boolean>,
        dragOverRect: MutableState<Pair<Offset, Size>?>,
        showWireframeOnly: MutableState<Boolean>,
        showWireframeName: MutableState<Boolean>,
        showPathOffsetPoints: MutableState<Boolean>
    ) {
        Window(onCloseRequest = {}, title = "Debug Canvas") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(Modifier
                    .width(1000.dp)
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            viewport.value = viewport.value.moveTo(it.div(0.1f), maxViewportSize)
                        }
                    }
                ) {
                    clipRect {
                        scale(0.1f) {
                            drawLine(Color.Black, start = Offset.Zero, end = Offset(x = size.width, y = size.height))
                            translate(
                                left = -maxViewportSize.width.div(2).minus(center.x),
                                top = -maxViewportSize.height.div(2).minus(center.y)
                            ) {
                                debugDrawViewport(maxViewportSize)
                                drawRectOwn(viewport.value.size, viewport.value.origin)
                                elements.map { it.boundingShape }.forEach {
                                    if (it is BoundingRect) {
                                        val inViewport = it.isVisibleInViewport(viewport.value)
                                        val inSelection = selectedBoundingBoxes.value.contains(it)
                                        it.drawWireframe(
                                            drawScope = this,
                                            color = if (inViewport) Color.Cyan else Color.Blue,
                                            fill = inSelection,
                                            showText = showWireframeName.value
                                        )
                                    }
                                }
                                arrows.forEach {
                                    it.apply { drawArrow() }
                                }
                                drawSelectionPlane(rect = dragOverRect.value?.mapPair(
                                    mapFirst = { it.plus(viewport.value.origin) },
                                    mapSecond = { it }
                                ))
                            }
                        }

                    }
                }
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Clip: ")
                            Switch(
                                checked = conditionalClipValue.value,
                                onCheckedChange = { conditionalClipValue.value = it },
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wireframe Only: ")
                            Switch(
                                checked = showWireframeOnly.value,
                                onCheckedChange = { showWireframeOnly.value = it },
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wireframe Names: ")
                            Switch(
                                checked = showWireframeName.value,
                                onCheckedChange = { showWireframeName.value = it },
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Arrow Show Offset Points: ")
                            Switch(
                                checked = showPathOffsetPoints.value,
                                onCheckedChange = { showPathOffsetPoints.value = it },
                            )
                        }
                        Text("Viewport:")
                        Text("Origin: ${viewport.value.origin}")
                        Text("Size: ${viewport.value.size}")
                        Text("Selected:")
                        selectedBoundingBoxes.value.forEach {
                            Text(text = it.debugName)
                        }
                        Text("Elements:")
                        elements.forEach {
                            Text(text = it.toString())
                        }
                        Text("Arrows:")
                        arrows.forEach {
                            Text(text = it.toString())
                        }
                    }
                }
            }
        }
    }
}