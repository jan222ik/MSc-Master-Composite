package com.github.jan222ik.ui.adjusted.arrow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.nativeCanvas
import com.github.jan222ik.model.TMM
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.CompoundBoundingShape
import com.github.jan222ik.ui.adjusted.IBoundingShape
import com.github.jan222ik.ui.adjusted.util.translate
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.uml.Anchor
import com.github.jan222ik.ui.uml.AnchorSide
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
    initBoundingShape: CompoundBoundingShape,
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
        ): Pair<List<Offset>, CompoundBoundingShape> {
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
            val isVerticalDirect = halfHeightTween < 3f
            println("sourceAnchor = [${sourceAnchor}], targetAnchor = [${targetAnchor}], sourceBoundingShape = [${sourceBoundingShape}], targetBoundingShape = [${targetBoundingShape}]")
            println("isVerticalDirect = ${isVerticalDirect}")
            println("halfHeightTween = ${halfHeightTween}")
            println("start = ${start}, target = ${target}")
            val halfHorizontal = min(start.x, target.x) + (abs(start.x - target.x)).div(2)
            println("halfHorizontal = ${halfHorizontal}")
            val inlineStart = Offset(x = start.x.takeUnless { isVerticalDirect } ?: halfHorizontal, y = start.y - halfHeightTween)
            val inlineTarget = Offset(x = target.x.takeUnless { isVerticalDirect } ?: halfHorizontal, y = target.y + halfHeightTween)

            val horizontalTopLeftRef = when {
                inlineStart.x < inlineTarget.x -> inlineStart
                else -> inlineTarget
            }


            val padding = 5f
            val compoundBoundingShape = CompoundBoundingShape(
                children = listOf(
                    BoundingRect(
                        initTopLeft = target.copy(x = target.x.minus(padding)),
                        initWidth = padding.times(2),
                        initHeight = halfHeightTween.plus(padding)
                    ),
                    BoundingRect(
                        initTopLeft = horizontalTopLeftRef.copy(x = horizontalTopLeftRef.x.minus(padding), y = horizontalTopLeftRef.y.minus(padding)),
                        initWidth = abs(start.x - target.x).plus(padding.times(2)),
                        initHeight = padding.times(2)
                    ),
                    BoundingRect(
                        initTopLeft = inlineStart.copy(x = inlineStart.x.minus(padding), y = inlineStart.y.minus(padding)),
                        initWidth = padding.times(2),
                        initHeight = halfHeightTween.plus(padding)
                    )
                )
            )

            val path = listOf<Offset>(start, inlineStart, inlineTarget, target)
            println("path = ${path}")
            return path to compoundBoundingShape
        }

        fun findIdealAnchors(
            firstBoundingShape: IBoundingShape,
            secondBoundingShape: IBoundingShape,
        ) : Pair<Anchor, Anchor> {
            val s = Anchor(AnchorSide.S, 0.5f)
            val e = Anchor(AnchorSide.E, 0.5f)
            val w = Anchor(AnchorSide.W, 0.5f)
            val n = Anchor(AnchorSide.N, 0.5f)
            val shapes = listOf(firstBoundingShape, secondBoundingShape)
            val higherShape = shapes.minByOrNull { it.topLeft.value.y }!!
            val lowerShape = shapes.maxByOrNull { it.topLeft.value.y }!!

            val leftestShape = shapes.minByOrNull { it.topLeft.value.x }!!
            val rightestShape = shapes.maxByOrNull { it.topLeft.value.x }!!

            val higherShapeBottomLeft = higherShape.topLeft.value + Offset(x = 0f, y = higherShape.height.value)

            val verticalDiff = higherShapeBottomLeft.y - lowerShape.topLeft.value.y
            val hasVerticalOverlap = verticalDiff >= 0

            val lefterShapeTopRight = higherShape.topLeft.value + Offset(x = 0f, y = higherShape.height.value)
            val horizontalDiff = lefterShapeTopRight.x -rightestShape.topLeft.value.x
            val hasHorizontalOverlap = horizontalDiff <= 0

            println("hasVerticalOverlap = ${hasVerticalOverlap}, hasHorizontalOverlap = ${hasHorizontalOverlap}")

            if (!hasVerticalOverlap) {
                if (hasHorizontalOverlap) {
                    // the objects are over each other in the diagram
                    return Pair(
                        first = if (higherShape == firstBoundingShape) s else n,
                        second = if (lowerShape == secondBoundingShape) n else s
                    )
                } else {
                    return if (leftestShape == higherShape) {
                        Pair(
                            first = if (leftestShape == firstBoundingShape) s else w,
                            second = if (rightestShape == secondBoundingShape) w else s
                        )
                    } else {
                        // aka. rightestShape == higherShape
                        Pair(
                            first = if (rightestShape == firstBoundingShape) s else e,
                            second = if (leftestShape == secondBoundingShape) e else s
                        )
                    }
                }
            } else {
                // The lower shape is higher than the bottom of the higher shape
                if (verticalDiff > shapes.minOf { it.height.value }.times(0.5f)) {
                    return Pair(
                        first = if (leftestShape == firstBoundingShape) e else w,
                        second = if (rightestShape == secondBoundingShape) w else e
                    )
                }
                return if (leftestShape == higherShape) {
                    Pair(
                        first = if (leftestShape == firstBoundingShape) s else w,
                        second = if (rightestShape == secondBoundingShape) w else s
                    )
                } else {
                    // aka. rightestShape == higherShape
                    Pair(
                        first = if (rightestShape == firstBoundingShape) s else e,
                        second = if (leftestShape == secondBoundingShape) e else s
                    )
                }
            }
        }
    }

    init {
        require(initOffsetPath.size > 1) { "Path requires at least a start and end point. Only one point was provided" }
    }

    val offsetPath = mutableStateOf(initOffsetPath)
    val arrowType = mutableStateOf(initArrowType)
    val sourceAnchor = mutableStateOf(initSourceAnchor)
    val targetAnchor = mutableStateOf(initTargetAnchor)
    val boundingShape = mutableStateOf(initBoundingShape)


    fun DrawScope.drawArrow(drawDebugPoints: Boolean = true) {
        val color = Color.Black
        val windowed = offsetPath.value.windowed(size = 2)
        val debugColors = listOf(Color.Magenta, Color.Green, Color.Yellow)
        windowed
            .forEachIndexed { index, offsets ->
                val start = offsets.first()
                val end = offsets.last()
                drawLine(color, start = start, end = end)
                if (drawDebugPoints) {
                    if (index == 0) {
                        drawCircle(Color.Red, radius = 2f, center = start)
                    }
                    drawCircle(debugColors[index.rem(debugColors.size)], radius = 8f.minus(index.times(2)), center = end)
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
        val target = FileTree.treeHandler.value?.metamodelRoot?.findModellingElementOrNull(this)?.target as TMM.ModelTree.Ecore.TAssociation
        val multiplicityState = if (idx == 0) target.memberEnd0MultiplicityString else target.memberEnd1MultiplicityString
        return TextLine.make(multiplicityState.tfv.text, Font())
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

    fun applyPath(path: Pair<List<Offset>, CompoundBoundingShape>) {
        offsetPath.value = path.first
        boundingShape.value = path.second
    }

    override fun toString(): String {
        return "Arrow(offsetPath=$offsetPath, arrowType=$arrowType)"
    }

}