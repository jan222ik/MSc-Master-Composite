package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.canvas.ChartContext
import com.github.jan222ik.canvas.canvas.DiagramChart
import com.github.jan222ik.canvas.canvas.DiagramChartScopeImpl
import com.github.jan222ik.canvas.data.DrawPoint
import com.github.jan222ik.canvas.dsl.ChartScopeImpl
import com.github.jan222ik.canvas.grid.intGridRenderer
import com.github.jan222ik.canvas.math.linearFunctionPointProvider
import com.github.jan222ik.canvas.math.linearFunctionRenderer
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.ui.components.dnd.DnDAction
import com.github.jan222ik.ui.components.dnd.dndDropTarget
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.ui.uml.UMLClassFactory
import mu.KLogger
import mu.NamedKLogging
import org.eclipse.uml2.uml.Class
import kotlin.math.roundToInt

@Composable
fun EditorTabComponent(state: EditorTabViewModel, projectTreeHandler: ProjectTreeHandler) {
    val logger = NamedKLogging("com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent").logger
    val commandStackHandler = LocalCommandStackHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                state.coords = it.positionInWindow()
                state.size = it.size
            }
            .clipToBounds()
    ) {
        val dropHandler = LocalDropTargetHandler.current
        val diagramChartImpl = DiagramChartScopeImpl()
        val dndActions = DNDEditorActions(
            state, logger, commandStackHandler, diagramChartImpl
        )
        DisposableEffect(dndActions, dropHandler) {
            logger.debug { "dropActions = ${dndActions.name}" }
            onDispose {
                dropHandler.dropTargets.value.toMutableList().filterNot { it.second == dndActions }
                logger.debug { "dispose dropActions = ${dndActions.name}" }
            }
        }
        DiagramChart(
            modifier = Modifier
                .fillMaxSize()
                .dndDropTarget(
                    handler = dropHandler,
                    dropActions = dndActions
                ),
            viewport = state.viewport,
            maxViewport = state.maxViewport.value,
            enableZoom = true,
            graphScopeImpl = diagramChartImpl
        ) {
            grid(intGridRenderer(stepAbscissa = 20, stepOrdinate = 20))
            linearFunction(
                linearFunctionRenderer(
                    lineDrawer = simpleAxisLineDrawer(brush = SolidColor(Color.White)),
                    linearFunctionPointProvider = linearFunctionPointProvider { it }
                )
            )
            state.items.value.forEach { component ->
                component.uiConfig.let {
                    anchoredComposable(it) {
                        Box(modifier = Modifier.offset { IntOffset(
                            this@anchoredComposable.offset.second.x.roundToInt(),
                            this@anchoredComposable.offset.second.y.roundToInt()
                        ) })
                        component.render(projectTreeHandler)
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Column {
                Text(
                    text = "Id:${state.id}"
                )
                Text(
                    text = state.viewport.value.let { "(${it.minX} ${it.minY})(${it.maxX} ${it.maxY})" },
                )
                Text(
                    text = "Items: ${state.items.value.size}"
                )
            }
        }
    }
}

class DNDEditorActions(
    val state: EditorTabViewModel,
    val logger: KLogger,
    val commandStackHandler: CommandStackHandler,
    val chartScopeImpl: ChartScopeImpl
) : DnDAction {
    override val name: String = "Canvas ${state.name} with id: ${state.id}"
    override fun drop(pos: IntOffset, data: Any?): Boolean {
        return if (data is Class) {
            logger.debug { "Accept drop for state name: ${state.id}" }
            val posX = pos.x.minus(state.coords.x).dp
            val posY = pos.y.minus(state.coords.y).dp
            println("posX = ${posX}, posY = ${posY}")
            val asDataPoint = DrawPoint(x = posX.value, y = posY.value).asDataPoint(
                ChartContext.of(
                    viewport = state.viewport.value,
                    canvasSize = state.size.toSize()
                )
            )

            val newObj = UMLClassFactory.createInstance(
                umlClass = data,
                initUiConfig = DiagramBlockUIConfig(
                    x = asDataPoint.x.dp,
                    y = asDataPoint.y.dp,
                    width = 255.dp,
                    height = 300.dp
                ).also { println("DiagramBlockUIConfig: $it") },
                commandStackHandler = commandStackHandler,
                deleteCommand = state::getRemoveCommandFor
            )
            val addCommand = state.getAddCommandFor(newObj)
            commandStackHandler.add(addCommand)
            true
        } else false
    }
}