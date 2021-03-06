package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.components.menu.MenuItemList
import com.github.jan222ik.ui.feature.LocalJobHandler
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.util.KeyHelpers
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import kotlinx.coroutines.launch
import mu.KLogging
import org.eclipse.uml2.uml.Element
import java.awt.Cursor
import kotlin.math.roundToInt

abstract class MovableAndResizeableComponent(
    initBoundingRect: BoundingRectState,
    val onNextUIConfig: (self: MovableAndResizeableComponent, old: BoundingRectState, new: BoundingRectState) -> Unit,
    val movableBaseUI: MovableBaseUI = MovableBaseUI()
) : ICanvasComposable {
    override val boundingShape: BoundingRect =
        BoundingRect(initBoundingRect.topLeft, initBoundingRect.width, initBoundingRect.height)

    companion object : KLogging() {
        val resizeAreaExpandSize = 5.dp
        val minSize = DpSize(50.dp, 50.dp)
    }

    internal var selected by mutableStateOf(false)
    internal var hover by mutableStateOf(false)
    internal var preMoveOrResize by mutableStateOf<BoundingRectState>(initBoundingRect)
    val showContextMenu = mutableStateOf(false)


    private fun Modifier.cursorForHorizontalResize(isHorizontal: Boolean): Modifier =
        pointerHoverIcon(PointerIcon(Cursor(if (isHorizontal) Cursor.E_RESIZE_CURSOR else Cursor.S_RESIZE_CURSOR)))

    private fun Modifier.cursorForDiagonalResize(se2nw: Boolean): Modifier =
        pointerHoverIcon(PointerIcon(Cursor(if (se2nw) Cursor.SE_RESIZE_CURSOR else Cursor.SW_RESIZE_CURSOR)))

    @Composable
    internal abstract fun content(projectTreeHandler: ProjectTreeHandler, helper: AlignmentHelper)

    internal abstract fun getMenuContributions(): List<MenuContribution>

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun render(projectTreeHandler: ProjectTreeHandler, offset: Offset, helper: AlignmentHelper) {
        Box(
            modifier = Modifier
                .defaultMinSize(minSize.width, minSize.height)
                .size(
                    width = boundingShape.width.value.dp.plus(resizeAreaExpandSize.times(2)),
                    height = boundingShape.height.value.dp.plus(resizeAreaExpandSize.times(2))
                )
                .offset {
                    IntOffset(
                        x = offset.x.minus(resizeAreaExpandSize.value).roundToInt(),
                        y = offset.y.minus(resizeAreaExpandSize.value).roundToInt()
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val scope = rememberCoroutineScope()
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(resizeAreaExpandSize)
                    .addIf(
                        condition = movableBaseUI.useBasecardBorder, other = Modifier.border(
                            BorderStroke(
                                width = 1.dp,
                                color = when {
                                    selected -> Color.Blue
                                    else -> Color.Black
                                }
                            )
                        )
                    )
                    .addIf(
                        condition = !movableBaseUI.useBasecardBorder && selected,
                        other = Modifier.border(
                            BorderStroke(width = 1.dp, color = Color.Blue)
                        )
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                if (EditorManager.allowEdit.value) {
                                    scope.launch {
                                        helper.movingBox.emit(boundingShape)
                                    }
                                    preMoveOrResize = boundingShape.toState()
                                }
                            },
                            onDragEnd = {
                                if (EditorManager.allowEdit.value) {
                                    onNextUIConfig(
                                        this@MovableAndResizeableComponent,
                                        preMoveOrResize,
                                        boundingShape.toState()
                                    )
                                }
                                helper.clear()
                            },
                            onDragCancel = {
                                if (EditorManager.allowEdit.value) {
                                    onNextUIConfig(
                                        this@MovableAndResizeableComponent,
                                        preMoveOrResize,
                                        boundingShape.toState()
                                    )
                                }
                                helper.clear()
                            }
                        ) { change, dragAmount ->
                            if (EditorManager.allowEdit.value) {
                                change.consumeAllChanges()
                                helper.updateDirection(change, dragAmount)
                                boundingShape.move(dragAmount)
                                selected = true
                                helper.onPointerChange(boundingShape.topLeft.value)
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Enter) { hover = true }
                    .onPointerEvent(PointerEventType.Exit) { hover = false },
                shape = RectangleShape,
                elevation = movableBaseUI.cardElevation,
                backgroundColor = movableBaseUI.cardBackground.takeUnless { it == Color.Unspecified }
                    ?: MaterialTheme.colors.surface
            ) {
                if (showContextMenu.value) {
                    Popup(
                        onDismissRequest = { showContextMenu.value = false },
                        onPreviewKeyEvent = {
                            KeyHelpers.onKeyDown(it) {
                                consumeOnKey(Key.Escape) {
                                    showContextMenu.value = false
                                }
                            }
                        },
                        focusable = true
                    ) {
                        Card(
                            border = BorderStroke(1.dp, EditorColors.dividerGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                            ) {
                                MenuItemList(
                                    items = getMenuContributions(), jobHandler = LocalJobHandler.current, width = 300.dp
                                )
                            }
                        }
                    }
                }
                content(projectTreeHandler, helper)
            }
            if (EditorManager.allowEdit.value) {
                fun onDragStart() {
                    scope.launch {
                        helper.movingBox.emit(boundingShape)
                    }
                }

                fun onDragEnd() {
                    helper.clear()
                }
                ResizeHandle(
                    alignment = Alignment.TopCenter,
                    isHorizontal = false,
                    onDrag = { _, drag ->
                        boundingShape.addHeight(-drag); boundingShape.addY(drag)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                ResizeHandle(
                    alignment = Alignment.BottomEnd,
                    isHorizontal = false,
                    onDrag = { _, drag ->
                        boundingShape.addHeight(drag)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                ResizeHandle(
                    alignment = Alignment.CenterStart,
                    isHorizontal = true,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(-drag); boundingShape.addX(drag)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                ResizeHandle(
                    alignment = Alignment.CenterEnd,
                    isHorizontal = true,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(drag)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                DiagonalResizeHandle(
                    alignment = Alignment.TopStart,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(-drag.x)
                        boundingShape.addX(drag.x)
                        boundingShape.addHeight(-drag.y)
                        boundingShape.addY(drag.y)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                DiagonalResizeHandle(
                    alignment = Alignment.TopEnd,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(drag.x)
                        boundingShape.addHeight(-drag.y)
                        boundingShape.addY(drag.y)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                DiagonalResizeHandle(
                    alignment = Alignment.BottomStart,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(-drag.x)
                        boundingShape.addX(drag.x)
                        boundingShape.addHeight(drag.y)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
                DiagonalResizeHandle(
                    alignment = Alignment.BottomEnd,
                    onDrag = { _, drag ->
                        boundingShape.addWidth(drag.x)
                        boundingShape.addHeight(drag.y)
                        helper.onPointerChange(boundingShape.topLeft.value)
                    },
                    onDragStart = ::onDragStart,
                    onDragEnd = ::onDragEnd
                )
            }
        }
    }

    @Composable
    fun BoxScope.ResizeHandle(
        alignment: Alignment,
        isHorizontal: Boolean,
        onDragStart: () -> Unit,
        onDragEnd: () -> Unit,
        onDrag: (change: PointerInputChange, dragAmount: Float) -> Unit
    ) {
        val sizeModifier =
            if (isHorizontal) {
                Modifier.fillMaxHeight().width(resizeAreaExpandSize)
            } else {
                Modifier.fillMaxWidth().height(resizeAreaExpandSize)
            }
        Box(
            Modifier.align(alignment).then(sizeModifier)
                .pointerInput(Unit) {
                    if (isHorizontal) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = onDrag,
                            onDragStart = {
                                preMoveOrResize = boundingShape.toState()
                                onDragStart.invoke()
                            },
                            onDragEnd = {
                                onDragEnd.invoke()
                                onNextUIConfig(
                                    this@MovableAndResizeableComponent,
                                    preMoveOrResize,
                                    boundingShape.toState()
                                )
                            }
                        )
                    } else {
                        detectVerticalDragGestures(
                            onVerticalDrag = onDrag,
                            onDragStart = {
                                preMoveOrResize = boundingShape.toState()
                                onDragStart.invoke()
                            },
                            onDragEnd = {
                                onDragEnd.invoke()
                                onNextUIConfig(
                                    this@MovableAndResizeableComponent,
                                    preMoveOrResize,
                                    boundingShape.toState()
                                )
                            }
                        )
                    }
                }
                .cursorForHorizontalResize(isHorizontal)
        )
    }

    @Composable
    fun BoxScope.DiagonalResizeHandle(
        alignment: Alignment,
        onDragStart: () -> Unit,
        onDragEnd: () -> Unit,
        onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
    ) {
        Box(
            Modifier.align(alignment)
                .size(resizeAreaExpandSize)
                .pointerInput(Unit) {
                    detectDragGestures(onDrag = onDrag, onDragStart = { onDragStart.invoke() }, onDragEnd = onDragEnd)
                }
                .cursorForDiagonalResize(alignment == Alignment.TopStart || alignment == Alignment.BottomEnd)
        )
    }

    fun useConfig(config: BoundingRectState) {
        boundingShape.topLeft.value = config.topLeft
        boundingShape.width.value = config.width
        boundingShape.height.value = config.height
    }

    abstract fun showsElement(element: Element?): Boolean
    open fun showsElementFromAssoc(element: Element?, lastMemberEnd: Boolean): Boolean = false

}

data class MovableBaseUI(
    val cardBackground: Color = Color.Unspecified,
    val useBasecardBorder: Boolean = true,
    val cardElevation: Dp = Space.dp16
)

