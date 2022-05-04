package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ui.adjusted.scroll.CanvasScrollState
import com.github.jan222ik.ui.feature.main.menu_tool_bar.mapPair
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScrollableCanvas(elements: List<ICanvasComposable>) {
    // Debug
    val conditionalClipValue = remember { mutableStateOf(true) }

    // Const
    val maxViewportSize = Size(10000f, 10000f)

    // Viewport
    val viewport = remember { mutableStateOf(Viewport()) }

    // Select by dragging
    val dragStartOffset = remember { mutableStateOf<Offset?>(null) }
    val dragOverRect = remember { mutableStateOf<Pair<Offset, Size>?>(null) }
    val selectedBoundingBoxes = remember { mutableStateOf<List<IBoundingShape>>(emptyList()) }
    LaunchedEffect(dragOverRect.value) {
        // State needed for some update, otherwise (only remember) data is stale
        selectedBoundingBoxes.value = dragOverRect.value?.let {
            val tmpSelectionAsViewport = Viewport(origin = it.first.plus(viewport.value.origin), size = it.second)
            elements.map { it.boundingShape }.filter { it.isVisibleInViewport(tmpSelectionAsViewport) }
        } ?: emptyList()
    }

    // Scrollbars
    val hScrollAdapter = remember(maxViewportSize, viewport.value) {
        ScrollableScrollbarAdapter(
            scrollState = CanvasScrollState(
                initial = maxViewportSize.width.minus(viewport.value.origin.x.plus(viewport.value.size.width))
                    .roundToInt(),
                maxDimensionValue = maxViewportSize.width.minus(viewport.value.size.width).roundToInt(),
                onScroll = {
                    viewport.value = viewport.value.applyPan(
                        pan = Offset.Zero.copy(x = it),
                        maxViewportSize = maxViewportSize
                    )
                }
            ),
            vertical = false
        )
    }
    val vScrollAdapter = remember(maxViewportSize, viewport.value) {
        ScrollableScrollbarAdapter(
            scrollState = CanvasScrollState(
                initial = maxViewportSize.height.minus(viewport.value.origin.y.plus(viewport.value.size.height))
                    .roundToInt(),
                maxDimensionValue = maxViewportSize.height.minus(viewport.value.size.height).roundToInt(),
                onScroll = {
                    viewport.value = viewport.value.applyPan(
                        pan = Offset.Zero.copy(y = it),
                        maxViewportSize = maxViewportSize
                    )
                }
            ), vertical = true)
    }

    // UI
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
                debugWindow(
                    viewport = viewport,
                    maxViewportSize = maxViewportSize,
                    elements = elements,
                    selectedBoundingBoxes = selectedBoundingBoxes,
                    conditionalClipValue = conditionalClipValue,
                    dragOverRect = dragOverRect
                )


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (conditionalClipValue.value) { Modifier.clipToBounds() } else Modifier)
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
                                    dragStartOffset.value?.let { start ->
                                        val current = change.position
                                        val topLeft = Offset(
                                            x = min(start.x, current.x),
                                            y = min(start.y, current.y)
                                        )
                                        val bottomRight = Offset(
                                            x = max(start.x, current.x),
                                            y = max(start.y, current.y),
                                        )
                                        val size = bottomRight.minus(topLeft).toSize()
                                        dragOverRect.value = topLeft to size
                                    }
                                },
                                onDragEnd = {
                                    dragStartOffset.value = null
                                    dragOverRect.value = null
                                    // TODO on selection finished callback with selectedBoundingBoxes
                                },
                                onDragCancel = {
                                    dragStartOffset.value = null
                                    dragOverRect.value = null
                                }
                            )
                        }
                        .scrollable(state = vScrollAdapter.scrollState, orientation = Orientation.Vertical)
                        .scrollable(state = hScrollAdapter.scrollState, orientation = Orientation.Horizontal)
                        .drawWithContent {
                            conditionalClip(conditionalClipValue.value) {
                                translate(
                                    left = -viewport.value.origin.x,
                                    top = -viewport.value.origin.y
                                ) {
                                    elements
                                        .filter { it.boundingShape.isVisibleInViewport(viewport.value) }
                                        .map { it.boundingShape }
                                        .forEach {
                                            if (it is BoundingRect) {
                                                it.drawWireframe(drawScope = this, fill = false)
                                            }
                                        }
                                }
                                if (dragStartOffset.value != null) {
                                    drawSelectionPlane(rect = dragOverRect.value)
                                }
                            }
                            drawContent()
                        }
                ) {
                    elements
                        .filter { it.boundingShape.isVisibleInViewport(viewport.value) }
                        .forEach {
                            it.render(it.boundingShape.topLeft.minus(viewport.value.origin))
                        }
                }
            }
            VerticalScrollbar(adapter = vScrollAdapter, reverseLayout = true)
        }
        HorizontalScrollbar(adapter = hScrollAdapter, reverseLayout = true)
    }
}

private fun DrawScope.drawSelectionPlane(rect: Pair<Offset, Size>?) {
    if (rect == null) return
    drawRect(
        color = Color.Blue.copy(alpha = 0.2f),
        topLeft = rect.first,
        size = rect.second
    )
}

private class ScrollableScrollbarAdapter(
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
    val origin: Offset = Offset.Zero,
    val size: Size = Size.Zero
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
    elements: List<ICanvasComposable>,
    selectedBoundingBoxes: MutableState<List<IBoundingShape>>,
    conditionalClipValue: MutableState<Boolean>,
    dragOverRect: MutableState<Pair<Offset, Size>?>
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
                                        fill = inSelection
                                    )
                                }
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
                    Text("Viewport:")
                    Text("Origin: ${viewport.value.origin}")
                    Text("Size: ${viewport.value.size}")
                    Text("Selected:")
                    selectedBoundingBoxes.value.forEach {
                        Text(text = it.debugName)
                    }

                }
            }
        }
    }
}

interface ICanvasComposable {
    val boundingShape: IBoundingShape

    @Composable
    fun render(offset: Offset)
}

class DemoComposable(
    override val boundingShape: IBoundingShape
) : ICanvasComposable {
    @Composable
    override fun render(offset: Offset) {
        Card(modifier = Modifier.offset(offset.x.dp, offset.y.dp)) {
            Text("Test ${boundingShape.debugName}", modifier = Modifier.padding(4.dp))
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
        val elements: List<ICanvasComposable> = boundingBoxes.map {
            DemoComposable(boundingShape = it)
        }
        Surface(Modifier.fillMaxSize(), color = Color.LightGray) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(1000.dp, 1000.dp)) {
                    ScrollableCanvas(elements)
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
    fun drawWireframe(drawScope: DrawScope, color: Color = Color.Cyan, fill: Boolean) {
        drawScope.translate(
            left = topLeft.x,
            top = topLeft.y
        ) {
            drawRect(
                color = color,
                style = if (fill) Fill else Stroke(),
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
