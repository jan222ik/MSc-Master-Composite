package com.github.jan222ik.canvas

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.canvas.canvas.Chart
import com.github.jan222ik.canvas.grid.intGridRenderer
import com.github.jan222ik.canvas.math.linearFunctionPointProvider
import com.github.jan222ik.canvas.math.linearFunctionRenderer
import com.github.jan222ik.canvas.viewport.Viewport

class ViewPortCanvas {

    companion object {
        const val VIEWPORT_MAX_HEIGHT = 1000f
        const val VIEWPORT_MAX_WIDTH = 1000f
    }

    val maxViewport = Viewport(minX = 0f, minY = 0f, maxX = VIEWPORT_MAX_WIDTH, maxY = VIEWPORT_MAX_HEIGHT)
    private val minViewport = Viewport(
        minX = 0f, minY = 0f,
        maxX = VIEWPORT_MAX_WIDTH.div(10), maxY = VIEWPORT_MAX_HEIGHT.div(10)
    )

    val viewport = mutableStateOf(
        Viewport(
            minX = 5f,
            minY = 5f,
            maxX = VIEWPORT_MAX_WIDTH.div(5),
            maxY = VIEWPORT_MAX_HEIGHT.div(5)
        )
    )


    @Composable
    fun render() {
        println("viewport = ${viewport.value}")

        ViewportChartLayout {
            Chart(
                modifier = Modifier.fillMaxSize().background(Color.White),
                viewport = viewport,
                maxViewport = maxViewport,
                minViewportSize = minViewport.size,
                enableZoom = false
            ) {
                grid(
                    intGridRenderer(
                        stepAbscissa = 10,
                        stepOrdinate = 5
                    )
                )
                repeat(100) { repeat ->
                    linearFunction(linearFunctionRenderer(linearFunctionPointProvider = linearFunctionPointProvider { 0.5f * it + repeat.toFloat() }))
                }
            }
        }
    }

    @Composable
    fun ViewportChartLayout(
        chart: @Composable () -> Unit
    ) {
        val verticalAdapter = remember {
            object : ScrollbarAdapter {
                override val scrollOffset: Float
                    get() = viewport.value.minY

                override fun maxScrollOffset(containerSize: Int): Float {
                    return maxViewport.maxY - viewport.value.width
                }

                override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
                    println("containerSize = ${containerSize}")
                    println("scrollOffset = ${scrollOffset}")
                    val dy = scrollOffset // .div(viewport.value.height)
                    viewport.value = viewport.value.applyPan(0f, dy, maxViewport)
                }

            }
        }
        val horizontalAdapter = remember {
            object : ScrollbarAdapter {
                override val scrollOffset: Float
                    get() = viewport.value.minX

                override fun maxScrollOffset(containerSize: Int): Float {
                    return maxViewport.maxX - (viewport.value.height)
                }

                override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
                    println("containerSize = ${containerSize}")
                    println("scrollOffset = ${scrollOffset}")
                    val dx = scrollOffset //.div(viewport.value.width)
                    viewport.value = viewport.value.applyPan(
                        dx, 0f, maxViewport
                    )
                }

            }
        }
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                VerticalScrollbar(adapter = verticalAdapter)
                HorizontalScrollbar(adapter = horizontalAdapter)
                chart.invoke()
            }
        ) { measureables, constraints ->
            val pVScroll = measureables[0].measure(Constraints.fixedHeight(constraints.maxHeight))
            val pHScroll = measureables[1].measure(Constraints.fixedWidth(constraints.maxWidth))
            val pCanvas = measureables[2].measure(
                Constraints(
                    maxWidth = constraints.maxWidth.minus(pVScroll.width),
                    maxHeight = constraints.maxHeight.minus(pHScroll.height)
                )
            )
            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight
            ) {
                pCanvas.placeRelative(IntOffset.Zero)
                pHScroll.place(x = 0, y = constraints.maxHeight.minus(pHScroll.height))
                pVScroll.place(x = constraints.maxWidth.minus(pVScroll.width), y = 0)
            }
        }
    }
}

fun main(args: Array<String>) {
    singleWindowApplication {
        Surface(color = Color.Gray, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
            ) {
                Box(modifier = Modifier.size(500.dp).border(width = 1.dp, color = Color.Black)) {
                    val vpc = remember { ViewPortCanvas() }
                    vpc.render()
                }
            }
        }
    }
}