package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.arrow.ArrowsHeads.AssociationDiamondHead
import com.github.jan222ik.ui.adjusted.util.translate
import com.github.jan222ik.ui.uml.Anchor
import com.github.jan222ik.ui.uml.DiagramStateHolders
import org.eclipse.uml2.uml.Association
import org.eclipse.uml2.uml.Relationship
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import javax.annotation.meta.Exhaustive
import kotlin.math.*

class Arrow(
    initArrowType: ArrowType,
    initSourceAnchor: Anchor,
    initTargetAnchor: Anchor,
    initOffsetPath: List<Offset>,
    val data: Relationship,
    val initSourceBoundingShape: BoundingRect,
    val initTargetBoundingShape: BoundingRect,
    val member0ArrowTypeOverride: DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation?,
    val member1ArrowTypeOverride: DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation?
) {
    companion object {
        fun fourPointArrowOffsetPath(
            sourceAnchor: Anchor,
            targetAnchor: Anchor,
            sourceBoundingShape: BoundingRect,
            targetBoundingShape: BoundingRect
        ): List<Offset> {
            val start = with(sourceBoundingShape) {
                val sW = width.value
                val sH = height.value
                topLeft.value.plus(sourceAnchor.toOffset(sW, sH))
            }
            val target = with(targetBoundingShape) {
                val sW = width.value
                val sH = height.value
                topLeft.value.plus(targetAnchor.toOffset(sW, sH))
            }

            val halfHeightTween = abs(max(start.y, target.y) - min(start.y, target.y)).div(2)
            val inlineStart = Offset(x = start.x, y = start.y - halfHeightTween)
            val inlineTarget = Offset(x = target.x, y = target.y + halfHeightTween)
            return listOf<Offset>(start, inlineStart, inlineTarget, target)
        }
    }

    init {
        require(initOffsetPath.size > 1) { "Path requires at least a start and end point. Only one point was provided" }
    }

    val offsetPath = mutableStateOf(initOffsetPath)
    val arrowType = mutableStateOf(initArrowType)
    val sourceAnchor = mutableStateOf(initSourceAnchor)
    val targetAnchor = mutableStateOf(initTargetAnchor)

    fun DrawScope.drawArrow(drawDebugPoints: Boolean = true) {
        val color = Color.Black
        val windowed = offsetPath.value.windowed(size = 2)
        windowed
            .forEachIndexed { index, offsets ->
                val start = offsets.first()
                val end = offsets.last()
                drawLine(color, start = start, end = end)
                if (drawDebugPoints) {
                    if (index == 0) {
                        drawCircle(Color.Red, radius = 2f, center = start)
                    }
                    drawCircle(Color.Red, radius = 2f, center = end)
                }
                if (index == 0) {
                    if (member0ArrowTypeOverride != null || member1ArrowTypeOverride != DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.NONE) {
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
                            offset = start
                        ) {
                            rotateRad(
                                radians = -angle.toFloat(),
                                pivot = Offset.Zero
                            ) {
                                rotate(90f + 180f, pivot = Offset.Zero) {
                                    @Exhaustive
                                    when (arrowType.value) {
                                        ArrowType.GENERALIZATION -> Unit
                                        ArrowType.ASSOCIATION_DIRECTED -> {
                                            arrowHeadForOverride(member1ArrowTypeOverride)
                                        }
                                    }
                                }
                            }
                            if (data is Association) {
                                val textLine = data.makeMultiplicityTextLine(1)
                                textLine?.let {
                                    this.drawContext.canvas.nativeCanvas.drawTextLine(
                                        it,
                                        10f,
                                        -5f,
                                        Paint().asFrameworkPaint()
                                    )
                                }
                            }
                        }
                    }
                }
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
                        offset = end
                    ) {
                        rotateRad(
                            radians = -angle.toFloat(),
                            pivot = Offset.Zero
                        ) {
                            rotate(90f, pivot = Offset.Zero) {
                                @Exhaustive
                                when (arrowType.value) {
                                    ArrowType.GENERALIZATION -> ArrowsHeads.apply { GeneralizationHead() }
                                    ArrowType.ASSOCIATION_DIRECTED -> {
                                        arrowHeadForOverride(member0ArrowTypeOverride)
                                    }
                                }
                            }
                        }
                        if (data is Association) {
                            val textLine = data.makeMultiplicityTextLine(0)
                            textLine?.let {
                                this.drawContext.canvas.nativeCanvas.drawTextLine(
                                    it,
                                    10f,
                                    5f + it.height,
                                    Paint().asFrameworkPaint()
                                )
                            }
                        }
                    }
                }
            }
    }

    fun Association.makeMultiplicityTextLine(idx: Int) : TextLine? {
        val end = memberEnds[idx]
        if (end.lower == 1 && end.upper == 1) {
            return null
        }
        val upperText = end.upper.takeUnless { it == -1 }?.toString() ?: "*"
        return TextLine.make(end.let { "${it.lower}..$upperText" }, Font())
    }

    fun DrawScope.arrowHeadForOverride(it: DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation?) {
        when (it) {
            null -> ArrowsHeads.apply { AssociationDirectedHead() }
            DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.COMPOSITE -> ArrowsHeads.apply {
                AssociationDiamondHead(
                    fill = true
                )
            }
            DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.SHARED -> ArrowsHeads.apply {
                AssociationDiamondHead(
                    fill = false
                )
            }

            DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.NONE -> ArrowsHeads.apply { AssociationDirectedHead() }
        }
    }

    fun applyPath(path: List<Offset>) {
        offsetPath.value = path
    }

    override fun toString(): String {
        return "Arrow(offsetPath=$offsetPath, arrowType=$arrowType)"
    }

}