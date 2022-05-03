package com.github.jan222ik.canvas.adjusted

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.canvas.adjusted.scroll.CanvasScrollState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun ScrollableCanvas(boundingBoxes: List<IBoundingShape>) {
    val conditionalClipValue = remember { mutableStateOf(true) }
    val dragStartOffset = remember { mutableStateOf<Offset?>(null) }
    val dragOverOffset = remember { mutableStateOf<Offset?>(null) }
    val minViewportSize = Size(25f, 25f)
    val viewport = remember {
        mutableStateOf(
            Viewport(
                origin = Offset(0f, 0f),
                size = Size(250f, 500f)
            )
        )
    }
    val maxViewportSize = Size(1000f, 1000f)
    val hScrollAdapter = remember(maxViewportSize, viewport.value) {
        ScrollableScrollbarAdapter(CanvasScrollState(
            initial = maxViewportSize.width.minus(viewport.value.origin.x.plus(viewport.value.size.width)).roundToInt(),
            viewport = viewport,
            maxDimensionValue = maxViewportSize.width.minus(viewport.value.size.width).roundToInt(),
            onScroll = {
                viewport.value = viewport.value.applyPan(
                    pan = Offset.Zero.copy(x = it),
                    maxViewportSize = maxViewportSize
                )
            }
        ), vertical = false)
    }
    val vScrollAdapter = remember(maxViewportSize, viewport.value) {
        ScrollableScrollbarAdapter(CanvasScrollState(
            initial = maxViewportSize.height.minus(viewport.value.origin.y.plus(viewport.value.size.height))
                .roundToInt().also { println("Vertical $it") },
            viewport = viewport,
            maxDimensionValue = maxViewportSize.height.minus(viewport.value.size.height).roundToInt(),
            onScroll = {
                viewport.value = viewport.value.applyPan(
                    pan = Offset.Zero.copy(y = it),
                    maxViewportSize = maxViewportSize
                )
            }
        ), vertical = true)
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().weight(1f)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .background(Color.White)
                    .weight(1f)
            ) {
                LaunchedEffect(this.maxWidth, this.maxHeight) {
                    val box = this@BoxWithConstraints
                    viewport.value = viewport.value.copy(
                        size = Size(
                            width = box.maxWidth.value,
                            height = box.maxHeight.value
                        )
                    )
                }
                debugWindow(viewport, maxViewportSize, boundingBoxes, conditionalClipValue)
                println("viewport = ${viewport.value}")
                Canvas(Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            viewport.value = viewport.value.applyPan(pan, maxViewportSize)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                dragStartOffset.value = it
                            },
                            onDrag = { change, _ ->
                                dragOverOffset.value = change.position
                            },
                            onDragEnd = {
                                dragStartOffset.value = null
                                dragOverOffset.value = null
                            },
                            onDragCancel = {
                                dragStartOffset.value = null
                                dragOverOffset.value = null
                            }
                        )
                    }
                ) {
                    conditionalClip(conditionalClipValue.value) {
                        translate(
                            left = -viewport.value.origin.x,
                            top = -viewport.value.origin.y
                        ) {
                            boundingBoxes
                                .filter { it.isVisibleInViewport(viewport.value) }
                                .forEach {
                                    if (it is BoundingRect) {
                                        it.drawWireframe(this)
                                    }
                                }
                        }
                        drawSelectionPlane(start = dragStartOffset.value, current = dragOverOffset.value)
                    }
                }
            }
            VerticalScrollbar(adapter = vScrollAdapter, reverseLayout = true)
        }
        HorizontalScrollbar(adapter = hScrollAdapter, reverseLayout = true)
    }
}

private fun DrawScope.drawSelectionPlane(start: Offset?, current: Offset?) {
    if (start == null || current == null) return
    val topLeft = Offset(
        x = min(start.x, current.x),
        y = min(start.y, current.y)
    )
    val bottomRight = Offset(
        x = max(start.x, current.x),
        y = max(start.y, current.y),
    )
    drawRect(
        color = Color.Blue.copy(alpha = 0.2f),
        topLeft = topLeft,
        size = bottomRight.minus(topLeft).toSize()
    )
}

private class ScrollableScrollbarAdapter(
    private val scrollState: CanvasScrollState,
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

private fun DrawScope.conditionalClip(doClip: Boolean, content: DrawScope.() -> Unit) {
    if (doClip) {
        clipRect {
            content.invoke(this)
        }
    } else {
        content.invoke(this)
    }
}

private fun DrawScope.debugDrawViewport(maxViewportSize: Size, color: Color = Color.Magenta) {
    drawRectOwn(
        size = maxViewportSize,
        topLeft = Offset.Zero,
        color = color
    )
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(maxViewportSize.width, maxViewportSize.height))
    drawLine(color = color, start = Offset(maxViewportSize.width, 0f), end = Offset(0f, maxViewportSize.height))
}

private fun DrawScope.drawRectOwn(size: Size, topLeft: Offset, color: Color = Color.Red) {
    drawRect(color = color, topLeft = topLeft, size = size, style = Stroke())
}

data class Viewport(
    val origin: Offset,
    val size: Size
) {
    fun applyPan(pan: Offset, maxViewportSize: Size): Viewport {
        val nX = origin.x - pan.x
        val nY = origin.y - pan.y
        return this.copy(
            origin = origin.copy(
                x = nX.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.width.minus(size.width)
                ),
                y = nY.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.height.minus(size.height)
                )
            )
        )
    }

    fun moveTo(pos: Offset, maxViewportSize: Size): Viewport {
        return this.copy(
            origin = origin.copy(
                x = pos.x.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.width.minus(size.width)
                ),
                y = pos.y.coerceIn(
                    minimumValue = 0f,
                    maximumValue = maxViewportSize.height.minus(size.height)
                )
            )
        )
    }
}

@Composable
fun debugWindow(
    viewport: MutableState<Viewport>,
    maxViewportSize: Size,
    boundingBoxes: List<IBoundingShape>,
    conditionalClipValue: MutableState<Boolean>
) {
    Window(onCloseRequest = {}, title = "Debug Canvas") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier
                .width(1000.dp)
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures {
                        viewport.value = viewport.value.moveTo(it, maxViewportSize)
                    }
                }
            ) {
                clipRect {
                    drawLine(Color.Black, start = Offset.Zero, end = Offset(x = size.width, y = size.height))

                    translate(
                        left = -maxViewportSize.width.div(2).minus(center.x),
                        top = -maxViewportSize.height.div(2).minus(center.y)
                    ) {
                        debugDrawViewport(maxViewportSize)
                        drawRectOwn(viewport.value.size, viewport.value.origin)
                        boundingBoxes.forEach {
                            if (it is BoundingRect) {
                                val inViewport = it.isVisibleInViewport(viewport.value)
                                it.drawWireframe(this, color = if (inViewport) Color.Cyan else Color.Blue)
                            }
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
                    Text("Viewport:")
                    Text("Origin: ${viewport.value.origin}")
                    Text("Size: ${viewport.value.size}")

                }
            }
        }
    }
}


fun main(args: Array<String>) {
    singleWindowApplication {
        val boundingBoxes = listOf<IBoundingShape>(
            BoundingRect(
                topLeft = Offset.Zero,
                width = 100f,
                height = 50f
            ),
            BoundingRect(
                topLeft = Offset(500f, 200f),
                width = 200f,
                height = 10f
            ),
            BoundingRect(
                topLeft = Offset(600f, 900f),
                width = 90f,
                height = 60f
            ),
            BoundingRect(
                topLeft = Offset(500f, 500f),
                width = 50f,
                height = 50f
            )
        )
        Surface(Modifier.fillMaxSize(), color = Color.LightGray) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(300.dp, 300.dp)) {
                    ScrollableCanvas(boundingBoxes)
                }
            }
        }
    }
}

data class BoundingRect(
    override val topLeft: Offset,
    val width: Float,
    val height: Float,
    override val debugName: String = Random.nextInt().toString()
) : IBoundingShape {
    fun drawWireframe(drawScope: DrawScope, color: Color = Color.Cyan) {
        drawScope.translate(
            left = topLeft.x,
            top = topLeft.y
        ) {
            drawRect(
                color = color,
                style = Stroke(),
                topLeft = Offset.Zero,
                size = Size(width = width, height = height)
            )
            drawLine(
                color = color,
                start = Offset.Zero,
                end = Offset(width, height)
            )
            drawLine(
                color = color,
                start = Offset(0f, height),
                end = Offset(width, 0f)
            )
        }
    }

    override fun isVisibleInViewport(viewport: Viewport): Boolean {
        val aBottomRight = topLeft.plus(Offset(width, height))
        val viewPortBottomRight = viewport.origin.plus(viewport.size.toOffset())
        val aTop = topLeft.y
        val aLeft = topLeft.x
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
}

fun Size.toOffset() = Offset(this.width, this.height)
fun Offset.toSize() = Size(this.x, this.y)
