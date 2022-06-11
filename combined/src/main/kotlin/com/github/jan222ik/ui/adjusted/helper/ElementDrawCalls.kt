package com.github.jan222ik.ui.adjusted.helper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ui.adjusted.AlignmentLine
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.IBoundingShape
import com.github.jan222ik.ui.adjusted.helper.ElementDrawCalls.drawAlignmentLine
import com.github.jan222ik.ui.adjusted.util.translate

object ElementDrawCalls {
    private val helperLineColor = Color(0xFFF68167)
    private val alignedLineColor = Color.Green

    fun DrawScope.drawDimensionIndicationCross(
        isAligned: Boolean = false,
        color: Color = helperLineColor,
        size: Float = 7.5f
    ) {
        val brush = SolidColor(if (isAligned) alignedLineColor else color)
        val halfSize = size.div(2)
        rotate(45f, pivot = Offset.Zero) {
            drawLine(
                brush = brush,
                start = Offset(x = -halfSize, y = 0f),
                end = Offset(x = halfSize, y = 0f)
            )
            drawLine(
                brush = brush,
                start = Offset(x = 0f, y = -halfSize),
                end = Offset(x = 0f, y = halfSize)
            )
        }
    }

    fun DrawScope.drawHelperAlignmentLine(
        isAligned: Boolean,
        start: Offset,
        end: Offset,
        color: Color = helperLineColor
    ) {
        drawLine(
            brush = SolidColor(if (isAligned) alignedLineColor else color),
            start = start,
            end = end
        )
    }

    fun DrawScope.drawAlignmentLine(pair: Pair<AlignmentLine, IBoundingShape>, movingBox: IBoundingShape) {
        val alignmentLine = pair.first
        val shape = pair.second
        val isInlineWith = alignmentLine.isInlineWith(movingBox, tolerance = 5)
        when (alignmentLine) {
            is AlignmentLine.Horizontal -> {
                if (shape.topLeft.value.x < movingBox.topLeft.value.x) {
                    val start = Offset(x = shape.topLeft.value.x, y = alignmentLine.y.toFloat())
                    drawHelperAlignmentLine(
                        start = start,
                        end = start.copy(x = movingBox.topLeft.value.x + movingBox.width.value),
                        isAligned = isInlineWith
                    )
                } else {
                    val start = Offset(x = movingBox.topLeft.value.x, y = alignmentLine.y.toFloat())
                    drawHelperAlignmentLine(
                        start = start,
                        end = start.copy(x = shape.topLeft.value.x + shape.width.value),
                        isAligned = isInlineWith
                    )
                }

                if (isInlineWith) {
                    translate(
                        shape.topLeft.value.plus(Offset(x = 0f, y = alignmentLine.y.toFloat() - shape.topLeft.value.y))
                    ) {
                        drawDimensionIndicationCross()
                    }
                    translate(
                        shape.topLeft.value.plus(
                            Offset(
                                x = shape.width.value,
                                y = alignmentLine.y.toFloat() - shape.topLeft.value.y
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }

                    translate(
                        movingBox.topLeft.value.plus(
                            Offset(
                                x = 0f,
                                y = alignmentLine.y.toFloat() - movingBox.topLeft.value.y
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }
                    translate(
                        movingBox.topLeft.value.plus(
                            Offset(
                                x = movingBox.width.value,
                                y = alignmentLine.y.toFloat() - movingBox.topLeft.value.y
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }
                }
            }
            is AlignmentLine.Vertical -> {
                if (shape.topLeft.value.y < movingBox.topLeft.value.y) {
                    val start = Offset(y = shape.topLeft.value.y, x = alignmentLine.x.toFloat())
                    drawHelperAlignmentLine(
                        start = start,
                        end = start.copy(y = movingBox.topLeft.value.y + movingBox.height.value),
                        isAligned = isInlineWith
                    )
                } else {
                    val start = Offset(y = movingBox.topLeft.value.y, x = alignmentLine.x.toFloat())
                    drawHelperAlignmentLine(
                        start = start,
                        end = start.copy(y = shape.topLeft.value.y + shape.height.value),
                        isAligned = isInlineWith
                    )
                }

                if (isInlineWith) {
                    translate(
                        shape.topLeft.value.plus(Offset(y = 0f, x = alignmentLine.x.toFloat() - shape.topLeft.value.x))
                    ) {
                        drawDimensionIndicationCross()
                    }
                    translate(
                        shape.topLeft.value.plus(
                            Offset(
                                y = shape.height.value,
                                x = alignmentLine.x.toFloat() - shape.topLeft.value.x
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }

                    translate(
                        movingBox.topLeft.value.plus(
                            Offset(
                                y = 0f,
                                x = alignmentLine.x.toFloat() - movingBox.topLeft.value.x
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }
                    translate(
                        movingBox.topLeft.value.plus(
                            Offset(
                                y = movingBox.height.value,
                                x = alignmentLine.x.toFloat() - movingBox.topLeft.value.x
                            )
                        )
                    ) {
                        drawDimensionIndicationCross()
                    }
                }
            }
        }

    }


}


fun main(args: Array<String>) {
    singleWindowApplication {
        val initBoxes = listOf(
            BoundingRect(
                initTopLeft = Offset(100f, 200f),
                initWidth = 50f,
                initHeight = 40f
            ),
            BoundingRect(
                initTopLeft = Offset(500f, 200f),
                initWidth = 30f,
                initHeight = 60f
            ),
            BoundingRect(
                initTopLeft = Offset(300f, 300f),
                initWidth = 100f,
                initHeight = 70f
            )
        )
        val movingBox = BoundingRect(
            initTopLeft = Offset.Zero,
            initHeight = 50f,
            initWidth = 50f
        )
        val boxes = remember(initBoxes) { mutableStateOf(initBoxes) }
        val scope = rememberCoroutineScope()
        val helper = remember(scope, boxes.value) { AlignmentHelper(scope, boxes.value) }
        val lines = helper.alignmentLines.collectAsState()
        LaunchedEffect(movingBox) {
            helper.movingBox.value = movingBox
        }
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    helper.detectDragGesture(this)
                }) {
                boxes.value.forEach {
                    it.drawWireframe(
                        this,
                        showText = false,
                        showBox = true,
                        fill = false
                    )
                }
                movingBox.drawWireframe(this, showText = false, showBox = true, fill = false)

                lines.value.forEach {
                    drawAlignmentLine(it, movingBox)
                }
            }
            //val pos = remember(movingBox.topLeft.value.x) { movingBox.topLeft.value.x }
            //Text(text = "${pos}", modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

