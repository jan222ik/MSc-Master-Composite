package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
}

/*
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
                    center.x,
                    center.y
                ) {
                    rotate(
                        degrees = rotation.value,
                        pivot = Offset.Zero
                    ) {
                        ArrowsHeads.apply { AssociationDirectedHead() }
                    }
                    drawCircle(Color.Red, radius = 2f, center = Offset.Zero)
                }
            }
        }
    }
}

 */