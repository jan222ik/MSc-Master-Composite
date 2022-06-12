package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.adjusted.helper.ElementDrawCalls.drawAlignmentLine
import com.github.jan222ik.ui.adjusted.scroll.CanvasScrollState
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import kotlinx.coroutines.flow.MutableStateFlow
import org.eclipse.uml2.uml.Element
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ScrollableCanvasDefaults {
    const val viewportSizeMaxWidth = 10_000f
    const val viewportSizeMaxHeight = 10_000f
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScrollableCanvas(
    id: Long,
    viewportState: MutableState<Viewport>,
    canvasThenModifier: Modifier,
    elements: List<ICanvasComposable>,
    arrows: List<Arrow>,
    projectTreeHandler: ProjectTreeHandler
) {
    val viewport = remember(viewportState) { viewportState }
    // Const
    val maxViewportSize = Size(
        width = ScrollableCanvasDefaults.viewportSizeMaxWidth,
        height = ScrollableCanvasDefaults.viewportSizeMaxHeight
    )

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
    val hScrollAdapter = remember(elements, maxViewportSize, viewport.value) {
        val maxWidth =
            min(elements.map { it.boundingShape.topLeft.value.x + it.boundingShape.width.value }.maxOfOrNull { it }.let { it ?: 0f } + 64f,
                maxViewportSize.width)
        ScrollableScrollbarAdapter(
            scrollState = CanvasScrollState(
                initial = maxWidth.minus(viewport.value.origin.x.plus(viewport.value.size.width))
                    .roundToInt(),
                maxDimensionValue = maxWidth.minus(viewport.value.size.width).roundToInt(),
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
        val maxHeight =
            min(elements.map { it.boundingShape.topLeft.value.y + it.boundingShape.height.value }.maxOfOrNull { it }.let { it ?: 0f } + 64f,
                maxViewportSize.height)

        ScrollableScrollbarAdapter(
            scrollState = CanvasScrollState(
                initial = maxHeight.minus(viewport.value.origin.y.plus(viewport.value.size.height))
                    .roundToInt(),
                maxDimensionValue = maxHeight.minus(viewport.value.size.height).roundToInt(),
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
                LaunchedEffect(id, this.maxWidth, this.maxHeight) {
                    val box = this@BoxWithConstraints
                    viewport.value = viewport.value.copy(
                        size = Size(
                            width = box.maxWidth.value,
                            height = box.maxHeight.value
                        )
                    )
                }
                DebugCanvas.debugWindow(
                    viewport = viewport,
                    maxViewportSize = maxViewportSize,
                    elements = elements,
                    arrows = arrows,
                    selectedBoundingBoxes = selectedBoundingBoxes,
                    dragOverRect = dragOverRect,
                )
                val scope = rememberCoroutineScope()
                val boxes = remember(elements) { MutableStateFlow(elements.map { it.boundingShape }) }
                val helper = remember(scope, boxes) { AlignmentHelper(scope, boxes.value) }
                val lines = helper.alignmentLines.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .addIf(DebugCanvas.conditionalClipValue.value, Modifier.clipToBounds())
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
                            conditionalClip(DebugCanvas.conditionalClipValue.value) {
                                translate(
                                    left = -viewport.value.origin.x,
                                    top = -viewport.value.origin.y
                                ) {

                                    elements
                                        .filter { it.boundingShape.isVisibleInViewport(viewport.value) }
                                        .map { it.boundingShape }
                                        .forEach {
                                            if (it is BoundingRect) {
                                                it.drawWireframe(
                                                    drawScope = this,
                                                    fill = selectedBoundingBoxes.value.contains(it),
                                                    showBox = DebugCanvas.showWireframes.value,
                                                    showText = DebugCanvas.showWireframeName.value
                                                )
                                            }
                                        }

                                    arrows
                                        .filter {
                                            it.offsetPath.value.any {
                                                BoundingRect(
                                                    initTopLeft = it,
                                                    0f,
                                                    0f
                                                ).isVisibleInViewport(viewport.value)
                                            }
                                        }
                                        .forEach {
                                            with(it) { drawArrow(drawDebugPoints = DebugCanvas.showPathOffsetPoints.value) }
                                        }
                                }
                            }
                            drawContent()
                            conditionalClip(DebugCanvas.conditionalClipValue.value) {
                                if (dragStartOffset.value != null) {
                                    drawSelectionPlane(rect = dragOverRect.value)
                                }


                                helper.movingBox.value?.let { box ->
                                    lines.value.forEach {
                                        drawAlignmentLine(it, box)
                                    }
                                }


                            }
                        }
                        .then(canvasThenModifier)
                ) {
                    if (!DebugCanvas.hideElements.value) {
                        elements
                            //.filter { it.boundingShape.isVisibleInViewport(viewport.value) }
                            .forEach {
                                val topLeft =
                                    remember(it.boundingShape.topLeft.value) { it.boundingShape.topLeft.value }
                                it.render(
                                    projectTreeHandler = projectTreeHandler,
                                    offset = topLeft.minus(viewport.value.origin),
                                    helper = helper
                                )
                            }
                    }
                }
            }
            VerticalScrollbar(adapter = vScrollAdapter, reverseLayout = true)
        }
        HorizontalScrollbar(adapter = hScrollAdapter, reverseLayout = true)
    }
}

fun Modifier.addIf(condition: Boolean, other: Modifier) = if (condition) this.then(other) else this

fun DrawScope.drawSelectionPlane(rect: Pair<Offset, Size>?) {
    if (rect == null) return
    drawRect(
        color = Color.Blue.copy(alpha = 0.2f),
        topLeft = rect.first,
        size = rect.second
    )
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

fun DrawScope.debugDrawViewport(maxViewportSize: Size, color: Color = Color.Magenta) {
    drawRectOwn(
        size = maxViewportSize,
        topLeft = Offset.Zero,
        color = color
    )
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(maxViewportSize.width, maxViewportSize.height))
    drawLine(color = color, start = Offset(maxViewportSize.width, 0f), end = Offset(0f, maxViewportSize.height))
}

fun DrawScope.drawRectOwn(size: Size, topLeft: Offset, color: Color = Color.Red) {
    drawRect(color = color, topLeft = topLeft, size = size, style = Stroke())
}

class DemoComposable(
    boundingShape: BoundingRectState
) : MovableAndResizeableComponent(
    initBoundingRect = boundingShape,
    onNextUIConfig = { p, o, n ->

    }
) {
    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler, helper: AlignmentHelper) {
        Text("Test ${boundingShape.debugName}", modifier = Modifier.padding(4.dp))
    }

    override fun getMenuContributions(): List<MenuContribution> {
        return emptyList()
    }

    override fun showsElement(element: Element?): Boolean = false


}

/*
fun main(args: Array<String>) {
    singleWindowApplication {
        val boundingBoxes = listOf<BoundingRect>(
            BoundingRect(
                initTopLeft = Offset.Zero,
                initWidth = 100f,
                initHeight = 50f
            ),
            BoundingRect(
                initTopLeft = Offset(500f, 200f),
                initWidth = 200f,
                initHeight = 10f
            ),
            BoundingRect(
                initTopLeft = Offset(600f, 900f),
                initWidth = 90f,
                initHeight = 60f
            ),
            BoundingRect(
                initTopLeft = Offset(500f, 500f),
                initWidth = 50f,
                initHeight = 50f
            )
        )
        val elements: List<ICanvasComposable> = boundingBoxes.map {
            DemoComposable(boundingShape = it.toState())
        }
        val arrows = listOf(
            Arrow(
                listOf(Offset.Zero, Offset(100f, 150f), Offset(200f, 50f)),
                ArrowType.GENERALIZATION,
                data = GeneralizationSetImpl()
            ),
            Arrow(
                listOf(Offset.Zero, Offset(100f, 150f), Offset(200f, 50f)).map { it.plus(Offset(x = 50f, y = 50f)) },
                ArrowType.ASSOCIATION_DIRECTED
            )
        )
        Surface(Modifier.fillMaxSize(), color = Color.LightGray) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(1000.dp, 1000.dp)) {
                    ScrollableCanvas(
                        canvasThenModifier = Modifier,
                        elements = elements,
                        arrows = arrows,
                        projectTreeHandler = ProjectTreeHandler(false)
                    )
                }
            }
        }
    }
}
 */

fun Size.toOffset() = Offset(this.width, this.height)
fun Offset.toSize() = Size(this.x, this.y)
