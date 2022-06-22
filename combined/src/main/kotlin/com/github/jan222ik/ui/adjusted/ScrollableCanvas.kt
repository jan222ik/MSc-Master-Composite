package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.model.TMM
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.adjusted.helper.ElementDrawCalls.drawAlignmentLine
import com.github.jan222ik.ui.adjusted.scroll.CanvasScrollState
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.components.menu.MenuItemList
import com.github.jan222ik.ui.feature.LocalJobHandler
import com.github.jan222ik.ui.feature.LocalWindowScope
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.keyevent.detectTapMouseGestures
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.KeyHelpers
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import org.eclipse.uml2.uml.Element
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ScrollableCanvasDefaults {
    const val viewportSizeMaxWidth = 10_000f
    const val viewportSizeMaxHeight = 10_000f
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ScrollableCanvas(
    id: Long,
    viewportState: MutableState<Viewport>,
    canvasThenModifier: Modifier,
    elements: List<ICanvasComposable>,
    arrows: List<Arrow>,
    projectTreeHandler: ProjectTreeHandler,
    tmmDiagram: TMM.ModelTree.Diagram,
    helper: AlignmentHelper,
    createBlockFromDrop: (Offset) -> Unit
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
            min(elements.map { it.boundingShape.topLeft.value.x + it.boundingShape.width.value }.maxOfOrNull { it }
                .let { it ?: 0f } + 64f,
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
            min(elements.map { it.boundingShape.topLeft.value.y + it.boundingShape.height.value }.maxOfOrNull { it }
                .let { it ?: 0f } + 64f,
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
                val lines = helper.alignmentLines.collectAsState()
                val showContextMenu = remember { mutableStateOf<Offset?>(null) }
                val windowScope = LocalWindowScope.current
                val layoutPos = remember { mutableStateOf(Offset.Zero) }
                val boxForArrow = remember { mutableStateOf<Arrow?>(null) }

                fun handleClick(pointerEvent: PointerEvent, offset: Offset) {
                    boxForArrow.value = null
                    if (pointerEvent.buttons.isPrimaryPressed) {
                        if (DemoMenuContributions.paletteSelection.value != null) {
                            if (DemoMenuContributions.paletteSelection.value == "Block") {
                                createBlockFromDrop(pointerEvent.changes.last().position)
                            }
                        }
                        val clickedArrow = arrows.firstOrNull { it.boundingShape.value.containtsOffset(offset) }
                        if (clickedArrow != null) {
                            boxForArrow.value = clickedArrow
                            val tmmArrow =
                                projectTreeHandler.metamodelRoot?.findModellingElementOrNull(clickedArrow.data)?.target
                            tmmArrow?.let {
                                SharedCommands.forceOpenProperties?.invoke()
                                FileTree.treeHandler.value?.setTreeSelection(listOf(tmmArrow))
                            }
                        } else {
                            SharedCommands.forceOpenProperties?.invoke()
                            FileTree.treeHandler.value?.setTreeSelection(listOf(tmmDiagram))
                        }
                    } else {
                        if (pointerEvent.buttons.isSecondaryPressed) {
                            showContextMenu.value = pointerEvent.changes.last().position
                        }
                    }
                }

                if (showContextMenu.value != null) {
                    Popup(
                        onDismissRequest = { showContextMenu.value = null },
                        onPreviewKeyEvent = {
                            KeyHelpers.onKeyDown(it) {
                                consumeOnKey(Key.Escape) {
                                    showContextMenu.value = null
                                }
                            }
                        },
                        focusable = true,
                        popupPositionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                return (showContextMenu.value?.round()?.let { it.copy(y = it.y - 10) }
                                    ?: IntOffset.Zero).plus(anchorBounds.topLeft)
                            }

                        }
                    ) {
                        Card(
                            border = BorderStroke(1.dp, EditorColors.dividerGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                            ) {
                                MenuItemList(
                                    jobHandler = LocalJobHandler.current, width = 300.dp,
                                    items = listOf(
                                        DemoMenuContributions.diagramNewElementMenuConfig(
                                            tmmRoot = tmmDiagram,
                                            inDiagramOffset = showContextMenu.value,
                                            state = EditorManager.activeEditorTab.value,
                                            onDismiss = {
                                                showContextMenu.value = null
                                            }
                                        )
                                    ),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .addIf(DebugCanvas.conditionalClipValue.value, Modifier.clipToBounds())
                        .onPlaced { layoutPos.value = it.positionInWindow() }
                        .pointerInput(Unit) {
                            detectTapMouseGestures(
                                onDoubleTap = ::handleClick,
                                onLongPress = ::handleClick,
                                onTap = ::handleClick
                            )
                        }
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
                                    lines.value.filter { it.first is AlignmentLine.Horizontal }.let { horizontal ->
                                        (horizontal as List<Pair<AlignmentLine.Horizontal, IBoundingShape>>)
                                            .groupBy { it.first.y }
                                            .let {
                                                println("map = ${it}, box = ${box.topLeft.value.y}")
                                                val key = box.topLeft.value.y + box.height.value
                                                it.entries
                                                    .firstOrNull { it.key in key.minus(3f).rangeTo(key.plus(3)) }
                                                    ?.let {
                                                        val a = it.value
                                                        if (a.isNotEmpty()) {
                                                            a
                                                                .map { it.second }
                                                                .plus(box)
                                                                .sortedBy { it.topLeft.value.y }
                                                                .windowed(size = 2) {
                                                                    println("it = ${it}")
                                                                    val lastX = it.last().topLeft.value.x
                                                                    val distanceTween = abs(
                                                                        it.first()
                                                                            .let { it.topLeft.value.x + it.width.value } - lastX)
                                                                    val textLine =
                                                                        TextLine.make(
                                                                            "${distanceTween.toInt()}",
                                                                            Font()
                                                                        )
                                                                    this.drawContext.canvas.nativeCanvas.drawTextLine(
                                                                        textLine,
                                                                        lastX.minus(distanceTween.div(2)),
                                                                        it.first().topLeft.value.y + it.first().height.value - 5f,
                                                                        Paint().asFrameworkPaint()
                                                                            .setARGB(0xFF, 0xF6, 0x81, 0x67)
                                                                    )

                                                                }
                                                        }
                                                    }
                                            }
                                    }
                                    lines.value.filter { it.first is AlignmentLine.Vertical }.let { vertical ->
                                        (vertical as List<Pair<AlignmentLine.Vertical, IBoundingShape>>)
                                            .groupBy { it.first.x }
                                            .let {
                                                println("map = ${it}, box = ${box.topLeft.value.y}")
                                                val key = box.topLeft.value.x + box.width.value
                                                it.entries
                                                    .firstOrNull { it.key in key.minus(3f).rangeTo(key.plus(3)) }
                                                    ?.let {
                                                        val a = it.value
                                                        if (a.isNotEmpty()) {
                                                            a
                                                                .map { it.second }
                                                                .plus(box)
                                                                .sortedBy { it.topLeft.value.x }
                                                                .windowed(size = 2) {
                                                                    println("it = ${it}")
                                                                    val lastY = it.last().topLeft.value.y
                                                                    val distanceTween = abs(
                                                                        it.first()
                                                                            .let { it.topLeft.value.y + it.height.value } - lastY)
                                                                    val textLine =
                                                                        TextLine.make(
                                                                            "${distanceTween.toInt()}",
                                                                            Font()
                                                                        )
                                                                    this.drawContext.canvas.nativeCanvas.drawTextLine(
                                                                        textLine,
                                                                        it.first().topLeft.value.x + it.first().width.value + 10f,
                                                                        lastY.minus(distanceTween.div(2)),
                                                                        Paint().asFrameworkPaint()
                                                                            .setARGB(0xFF, 0xF6, 0x81, 0x67)
                                                                    )

                                                                }
                                                        }
                                                    }
                                            }
                                    }
                                }

                                DemoMenuContributions.arrowFrom.value?.let {
                                    val mousePosition = windowScope.window.mousePosition
                                    drawLine(
                                        color = Color.Red,
                                        start = it.start.boundingShape.topLeft.value,
                                        end = Offset(
                                            mousePosition.x.toFloat(),
                                            mousePosition.y.toFloat()
                                        ).minus(layoutPos.value)
                                    )
                                }

                                boxForArrow.value?.let {
                                    it.boundingShape.value.children.forEach {
                                        it.drawWireframe(
                                            this@drawWithContent,
                                            fill = false,
                                            color = EditorColors.focusActive,
                                            showBox = true,
                                            showText = false,
                                            drawX = false
                                        )
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
