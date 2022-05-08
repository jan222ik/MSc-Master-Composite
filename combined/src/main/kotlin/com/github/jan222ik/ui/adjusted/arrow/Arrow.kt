package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.translate
import org.eclipse.uml2.uml.DirectedRelationship
import org.eclipse.uml2.uml.Generalization
import javax.annotation.meta.Exhaustive
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class Arrow(
    initOffsetPath: List<Offset>,
    initArrowType: ArrowType,
    val data: DirectedRelationship
) {
    init {
        require(initOffsetPath.size > 1) { "Path requires at least a start and end point. Only one point was provided" }
    }

    val offsetPath = mutableStateOf(initOffsetPath)
    val arrowType = mutableStateOf(initArrowType)

    fun DrawScope.drawArrow() {
        val color = Color.Black
        val windowed = offsetPath.value.windowed(size = 2)
        windowed
            .forEachIndexed { index, offsets ->
                val start = offsets.first()
                val end = offsets.last()
                drawLine(color, start = start, end = end)
                if (index == windowed.lastIndex) {
                    val vecXAxis = Offset(1f, 0f)
                    val vecLine = end.minus(start)
                    val angle = acos(
                        (vecXAxis.x * vecLine.x + vecXAxis.y * vecLine.y)
                            .div(
                                sqrt(vecXAxis.x.pow(2) + vecXAxis.y.pow(2))
                                    .times(
                                        sqrt(vecLine.x.pow(2) + vecLine.y.pow(2))
                                    ).toDouble()
                            )
                    )
                    translate(
                        left = end.x,
                        top = end.y
                    ) {
                        rotateRad(radians = angle.toFloat(), pivot = Offset.Zero) {
                            @Exhaustive
                            when (arrowType.value) {
                                ArrowType.GENERALIZATION -> ArrowsHeads.apply { GeneralizationHead() }
                                ArrowType.ASSOCIATION_DIRECTED -> ArrowsHeads.apply { AssociationDirectedHead() }
                            }
                        }
                    }
                }
            }
    }

    fun applyPath(path: List<Offset>) {
        offsetPath.value = path
    }

    override fun toString(): String {
        return "Arrow(offsetPath=$offsetPath, arrowType=$arrowType)"
    }


}