package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ui.adjusted.util.translate
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer
import kotlin.math.cos

object ArrowsHeads {
    fun DrawScope.GeneralizationHead(color: Color = Color.Black, arrowBaseWidth: Float = 30f) {
        val height = (arrowBaseWidth * cos(Math.PI / 6)).toFloat()
        val path = Path().apply {
            fillType = PathFillType.NonZero
            moveTo(0f, 0f)
            lineTo(x = -arrowBaseWidth.div(2), y = 0f)
            lineTo(x = 0f, y = height)
            lineTo(x = arrowBaseWidth.div(2), y = 0f)
            this.close()
        }
        rotate(
            degrees = 180f,
            pivot = Offset(
                x = 0f,
                y = height.div(2)
            )
        ) {
            drawPath(path, Color.White)
            drawPath(path, color, style = Stroke())
        }

    }

    fun DrawScope.AssociationDirectedHead(color: Color = Color.Black, arrowBaseWidth: Float = 30f) {
        val height = arrowBaseWidth * cos(Math.PI / 6).toFloat()
        val path = Path().apply {
            moveTo(x = 0f, y = height)
            lineTo(x = 0f, y = 0f)
            moveTo(x = -arrowBaseWidth.div(3), y = arrowBaseWidth.div(4))
            lineTo(x = 0f, y = 0f)
            lineTo(x = arrowBaseWidth.div(3), y = arrowBaseWidth.div(4))
        }
        rotate(
            degrees = 180f,
            pivot = Offset(
                x = arrowBaseWidth.div(3),
                y = maxOf(arrowBaseWidth * cos(Math.PI / 6).toFloat(), height).div(2)
            )
        ) {

        }
        drawPath(path, color, style = Stroke())
    }

    fun DrawScope.AssociationDiamondHead(fill: Boolean, color: Color = Color.Black, arrowBaseWidth: Float = 20f) {
        val height = arrowBaseWidth * cos(Math.PI / 6).toFloat()
        val baseWidthYDiv = 2
        val path = Path().apply {
            moveTo(x = -arrowBaseWidth.div(3), y = arrowBaseWidth.div(baseWidthYDiv))
            lineTo(x = 0f, y = 0f)
            lineTo(x = arrowBaseWidth.div(3), y = arrowBaseWidth.div(baseWidthYDiv))
            moveTo(x = -arrowBaseWidth.div(3), y = arrowBaseWidth.div(baseWidthYDiv))
            lineTo(x = 0f, y = height)
            lineTo(x = arrowBaseWidth.div(3), y = arrowBaseWidth.div(baseWidthYDiv))
        }
        if (!fill) {
            drawPath(path, Color.White, style = Fill)
            drawPath(path, color, style = Stroke())
        } else {
            drawPath(path, color, style = Fill)
        }
    }
}


fun main() {

    singleWindowApplication {
        val rotation = remember { mutableStateOf(0f) }
        LaunchedEffect(Unit) {
            launch {
                fixedRateTimer(period = 50) {
                    rotation.value = rotation.value.inc().rem(360f)
                }

            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(500.dp).background(Color.White)) {
                translate(
                    center
                ) {
                    rotate(
                        degrees = rotation.value,
                        pivot = Offset.Zero
                    ) {
                        ArrowsHeads.apply { AssociationDiamondHead(true) }
                    }
                    drawCircle(Color.Red, radius = 2f, center = Offset.Zero)
                }
            }
        }
    }
}

