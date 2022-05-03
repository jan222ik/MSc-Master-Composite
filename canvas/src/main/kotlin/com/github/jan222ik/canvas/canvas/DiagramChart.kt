@file:Suppress("FunctionName")

package com.github.jan222ik.canvas.canvas


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.dsl.detectTransformGestures
import com.github.jan222ik.canvas.interaction.IBoundingShape2D
import com.github.jan222ik.canvas.series.ISeriesRenderer
import com.github.jan222ik.canvas.viewport.Viewport
import kotlin.math.roundToInt

@Composable
fun DiagramChart(
    modifier: Modifier,
    viewport: MutableState<Viewport>,
    maxViewport: Viewport = viewport.value,
    minViewportSize: Size = viewport.value.size,
    maxViewportSize: Size = viewport.value.size,
    enableZoom: Boolean = false,
    graphScopeImpl: DiagramChartScopeImpl = DiagramChartScopeImpl(),

    definition: DiagramChartScope.() -> Unit
) {
    definition.invoke(graphScopeImpl)
    val (slotCanvas) = remember { listOf("canvas") }
    val renderedPoints = remember { mutableStateOf(listOf<IBoundingShape2D>()) }
    val activePopupsAt = remember { mutableStateOf<Pair<Offset, IBoundingShape2D?>?>(null) }

    SubcomposeLayout(modifier) { constraints ->
        val constraintsWithoutLowerBound = constraints.copy(minWidth = 0, minHeight = 0)

        val pPopupsWithOffset = subcompose("popups") {
            activePopupsAt.value?.let { (offset: Offset, shape2D: IBoundingShape2D?) ->
                val clickConsumed = graphScopeImpl.onClickPopupLabel?.invoke(offset, shape2D) ?: false
                LaunchedEffect(offset, shape2D) {
                    if (!clickConsumed) {
                        graphScopeImpl.onClick?.invoke(offset, shape2D)
                    }
                }
            }
        }.firstOrNull()?.let {
            it.measure(constraintsWithoutLowerBound) to activePopupsAt.value?.first
        }

        val canvasSize = Size(
            width = constraints.maxWidth.toFloat(),
            height = constraints.maxHeight.toFloat()
        )
        val rendererContext = ChartContext.of(viewport.value, canvasSize)

        val mCanvas = subcompose(slotCanvas) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(viewport) {
                        detectTransformGestures { _, pan, zoom, direction ->
                            //println("Gesture detected: Zoom: $zoom")
                            val current = if (enableZoom) {
                                viewport.value.applyZoom(
                                    zoom = zoom,
                                    direction = direction,
                                    minSize = minViewportSize,
                                    maxSize = maxViewportSize
                                )
                            } else {
                                viewport.value
                            }

                            val dx = -pan.x / rendererContext.scaleX
                            val dy = pan.y / rendererContext.scaleY
                            viewport.value = current.applyPan(dx, dy, maxViewport)
                        }
                    }
                    .pointerInput(renderedPoints) {
                        detectTapGestures { offset ->
                            val shape2D = renderedPoints.value.firstOrNull { it.containtsOffset(offset) }
                            activePopupsAt.value = offset to shape2D
                        }
                    }
            ) {
                clipRect clipScope@{
                    with(
                        ChartDrawScope(
                            drawScope = this@clipScope,
                            context = ChartContext.of(
                                viewport = viewport.value,
                                canvasSize = this.size
                            )
                        )
                    ) {
                        graphScopeImpl.gridRenderer.forEach {
                            with(it) { render() }
                        }
                        renderedPoints.value =
                            graphScopeImpl.seriesMap.map { (series: List<DataPoint>, renderer: ISeriesRenderer) ->
                                with(renderer) { render(series) }
                            }.flatten()
                        graphScopeImpl.linearFunctionRenderer.forEach {
                            with(it) { render() }
                        }
                    }
                }
            }
        }

        val pCanvas = mCanvas.first().measure(
            Constraints.fixed(
                width = canvasSize.width.roundToInt(),
                height = canvasSize.height.roundToInt()
            )
        )

        val mAnchors = subcompose("anchors") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                graphScopeImpl.anchorMap.mapKeys {
                    with(rendererContext) {
                        it.key to Offset(
                            x = it.key.x.toRendererX(),
                            y = it.key.y.toRendererY(),
                        )
                    }
                }.forEach { (offset, content) ->
                    content(AnchorScope(offset))
                }
            }
        }

        val mAnchorsState = subcompose("anchorsState") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                graphScopeImpl.anchorMapState.mapKeys {
                    val state = remember { it.key }
                    with(rendererContext) {
                         DataPoint(state.value.x.value, state.value.y.value) to Offset(
                            x = it.key.value.x.value.toRendererX(),
                            y = it.key.value.y.value.toRendererY(),
                        )
                    }
                }.forEach { (offset, content) ->
                    content(AnchorScope(offset))
                }
            }
        }

        val pAnchors = mAnchors.first().measure(
            Constraints.fixed(
                width = canvasSize.width.roundToInt(),
                height = canvasSize.height.roundToInt()
            )
        )

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            pCanvas.placeRelative(IntOffset.Zero)
            pPopupsWithOffset?.let { (placeable: Placeable, offset: Offset?) ->
                offset?.let { (x, y) ->
                    placeable.placeRelative(x = x.roundToInt(), y = y.roundToInt())
                }
            }
            pAnchors.placeRelative(IntOffset.Zero)
        }
    }
}
