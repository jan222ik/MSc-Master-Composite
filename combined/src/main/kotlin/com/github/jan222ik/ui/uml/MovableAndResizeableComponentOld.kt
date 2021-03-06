package com.github.jan222ik.ui.uml

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import mu.KLogging
import java.awt.Cursor
import kotlin.math.roundToInt

abstract class MovableAndResizeableComponentOld(
    initUiConfig: DiagramBlockUIConfig,
    val onNextUIConfig: (self: MovableAndResizeableComponentOld, old: DiagramBlockUIConfig, new: DiagramBlockUIConfig) -> Unit
) {
    val uiConfig = mutableStateOf(initUiConfig)

    companion object : KLogging() {
        val resizeAreaExpandSize = 5.dp
        val minSize = DpSize(50.dp, 50.dp)
    }

    internal var selected by mutableStateOf(false)
    private var resizeW by mutableStateOf(0f)
    private var resizeH by mutableStateOf(0f)

    private var moveOffsetX by mutableStateOf(0f)
    private var moveOffsetY by mutableStateOf(0f)

    internal var hover by mutableStateOf(false)


    private fun Modifier.cursorForHorizontalResize(isHorizontal: Boolean): Modifier =
        pointerHoverIcon(PointerIcon(Cursor(if (isHorizontal) Cursor.E_RESIZE_CURSOR else Cursor.S_RESIZE_CURSOR)))

    private fun Modifier.cursorForDiagonalResize(se2nw: Boolean): Modifier =
        pointerHoverIcon(PointerIcon(Cursor(if (se2nw) Cursor.SE_RESIZE_CURSOR else Cursor.SW_RESIZE_CURSOR)))

    @Composable
    internal abstract fun content(projectTreeHandler: ProjectTreeHandler)

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun render(projectTreeHandler: ProjectTreeHandler) {
        Box(
            modifier = Modifier
                .defaultMinSize(minSize.width, minSize.height)
                .size(
                    width = uiConfig.value.width.plus(resizeW.dp).plus(resizeAreaExpandSize.times(2)),
                    height = uiConfig.value.height.plus(resizeH.dp).plus(resizeAreaExpandSize.times(2))
                )
                .offset {
                    IntOffset(
                        x = uiConfig.value.x.value.plus(moveOffsetX).roundToInt(),
                        y = uiConfig.value.y.value.plus(moveOffsetY).roundToInt()
                    )
                }
            //.background(Color.Magenta)
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(resizeAreaExpandSize)
                    .border(
                        width = 1.dp,
                        color = when {
                            selected -> Color.Blue
                            else -> Color.Black
                        }
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                onNextUIConfig(
                                    this@MovableAndResizeableComponentOld,
                                    uiConfig.value,
                                    DiagramBlockUIConfig(
                                        x = uiConfig.value.x + moveOffsetX.dp,
                                        y = uiConfig.value.y + moveOffsetY.dp,
                                        width = uiConfig.value.width + resizeW.dp,
                                        height = uiConfig.value.height + resizeH.dp
                                    )
                                )
                            }
                        ) { change, dragAmount ->
                            change.consumeAllChanges()
                            moveOffsetX += dragAmount.x
                            moveOffsetY += dragAmount.y
                            selected = true
                        }
                    }
                    .onPointerEvent(PointerEventType.Enter) { hover = true }
                    .onPointerEvent(PointerEventType.Exit) { hover = false },
                shape = RectangleShape,
                elevation = 16.dp
            ) {
                content(projectTreeHandler)
            }
            ResizeHandle(
                alignment = Alignment.TopCenter,
                isHorizontal = false,
                onDrag = { _, drag -> resizeH += -drag; moveOffsetY += drag })
            ResizeHandle(alignment = Alignment.BottomEnd, isHorizontal = false, onDrag = { _, drag -> resizeH += drag })
            ResizeHandle(
                alignment = Alignment.CenterStart,
                isHorizontal = true,
                onDrag = { _, drag -> resizeW += -drag; moveOffsetX += drag })
            ResizeHandle(alignment = Alignment.CenterEnd, isHorizontal = true, onDrag = { _, drag -> resizeW += drag })
            DiagonalResizeHandle(
                alignment = Alignment.TopStart,
                onDrag = { _, drag ->
                    resizeW += -drag.x
                    moveOffsetX += drag.x
                    resizeH += -drag.y
                    moveOffsetY += drag.y
                }
            )
            DiagonalResizeHandle(
                alignment = Alignment.TopEnd,
                onDrag = { _, drag ->
                    resizeW += drag.x
                    resizeH += -drag.y
                    moveOffsetY += drag.y
                }
            )
            DiagonalResizeHandle(
                alignment = Alignment.BottomStart,
                onDrag = { _, drag ->
                    resizeW += -drag.x
                    moveOffsetX += drag.x
                    resizeH += drag.y
                }
            )
            DiagonalResizeHandle(
                alignment = Alignment.BottomEnd,
                onDrag = { _, drag ->
                    resizeW += drag.x
                    resizeH += drag.y
                }
            )
        }
    }

    @Composable
    fun BoxScope.ResizeHandle(
        alignment: Alignment,
        isHorizontal: Boolean,
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
                            onDragEnd = {
                                onNextUIConfig(
                                    this@MovableAndResizeableComponentOld,
                                    uiConfig.value,
                                    DiagramBlockUIConfig(
                                        x = uiConfig.value.x + moveOffsetX.dp,
                                        y = uiConfig.value.y + moveOffsetY.dp,
                                        width = uiConfig.value.width + resizeW.dp,
                                        height = uiConfig.value.height + resizeH.dp
                                    )
                                )
                            }
                        )
                    } else {
                        detectVerticalDragGestures(
                            onVerticalDrag = onDrag,
                            onDragEnd = {
                                onNextUIConfig(
                                    this@MovableAndResizeableComponentOld,
                                    uiConfig.value,
                                    DiagramBlockUIConfig(
                                        x = uiConfig.value.x + moveOffsetX.dp,
                                        y = uiConfig.value.y + moveOffsetY.dp,
                                        width = uiConfig.value.width + resizeW.dp,
                                        height = uiConfig.value.height + resizeH.dp
                                    )
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
        onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
    ) {
        Box(
            Modifier.align(alignment)
                .size(resizeAreaExpandSize)
                .pointerInput(Unit) {
                    detectDragGestures(onDrag = onDrag)
                }
                .cursorForDiagonalResize(alignment == Alignment.TopStart || alignment == Alignment.BottomEnd)
        )
    }

    fun useConfig(config: DiagramBlockUIConfig) {
        uiConfig.value = config
        moveOffsetX = 0f
        moveOffsetY = 0f
        resizeH = 0f
        resizeW = 0f
    }
}


