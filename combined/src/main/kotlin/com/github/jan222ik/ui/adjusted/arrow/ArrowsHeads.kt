package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
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
            moveTo(x = 0f, y = 0f)
            lineTo(x = 0f, y = height)
            moveTo(x = -arrowBaseWidth.div(3), y = arrowBaseWidth.div(4))
            lineTo(x = 0f, y = height)
            lineTo(x = arrowBaseWidth.div(3), y = arrowBaseWidth.div(4))
        }
        rotate(
            degrees = 180f,
            pivot = Offset(
                x = 0f,
                y = arrowBaseWidth.div(4) * cos(Math.PI / 6).toFloat().div(2)
            )
        ) {
            drawPath(path, color, style = Stroke())
        }
    }
}


fun main() {
    val listOf = listOf(
        Arrow(
            listOf(Offset.Zero, Offset(100f, 150f), Offset(200f, 50f)),
            ArrowType.GENERALIZATION
        ),
        Arrow(
            listOf(Offset.Zero, Offset(100f, 150f), Offset(200f, 50f)).map { it.plus(Offset(x = 50f, y = 50f)) },
            ArrowType.ASSOCIATION_DIRECTED
        )
    )
    singleWindowApplication {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(500.dp)) {
                listOf.forEach {
                    with(it) { drawArrow() }
                }
            }
        }
    }
}