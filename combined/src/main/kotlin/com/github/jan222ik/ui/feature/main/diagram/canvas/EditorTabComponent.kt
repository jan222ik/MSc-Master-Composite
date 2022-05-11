package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.packFloats
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.CompoundCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.ScrollableCanvas
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.components.dnd.DnDAction
import com.github.jan222ik.ui.components.dnd.dndDropTarget
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.uml.Anchor
import com.github.jan222ik.ui.uml.AnchorSide
import com.github.jan222ik.ui.uml.UMLClass
import com.github.jan222ik.ui.uml.UMLClassFactory
import mu.KLogger
import mu.KLogging
import mu.NamedKLogging
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Generalization

@Composable
fun EditorTabComponent(state: EditorTabViewModel, projectTreeHandler: ProjectTreeHandler) {
    val logger = NamedKLogging("com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent").logger
    val commandStackHandler = LocalCommandStackHandler.current

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned {
        state.coords = it.positionInWindow()
    }.clipToBounds()) {
        val dropHandler = LocalDropTargetHandler.current
        val dndActions = DNDEditorActions(
            state, logger, commandStackHandler, projectTreeHandler
        )
        /*
        DisposableEffect(dndActions, dropHandler) {
            logger.debug { "dropActions = ${dndActions.name}" }
            onDispose {
                dropHandler.dropTargets.value.toMutableList().filterNot { it.second == dndActions }
                logger.debug { "dispose dropActions = ${dndActions.name}" }
            }
        }
         */
        ScrollableCanvas(
            viewport = state.viewport,
            canvasThenModifier = Modifier.dndDropTarget(
                handler = dropHandler,
                dropActions = dndActions
            ),
            elements = state.observableDiagram.elements.value,
            arrows = state.observableDiagram.arrows.value,
            projectTreeHandler = projectTreeHandler
        )
        NavigateDiagramUPButton(
            modifier = Modifier.align(Alignment.TopStart)
        )
        /*
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
         */
    }
}

class DNDEditorActions(
    val state: EditorTabViewModel,
    val logger: KLogger,
    val commandStackHandler: CommandStackHandler,
    val projectTreeHandler: ProjectTreeHandler
) : DnDAction {
    override val name: String = "Canvas ${state.name} with id: ${state.id}"
    override fun drop(pos: IntOffset, data: Any?): Boolean {
        return if (data is Class) {
            logger.debug { "Accept drop for state name: ${state.id}" }
            val posInComponent = pos.toOffset().minus(state.coords)
            val dataPoint = state.viewport.value.origin + posInComponent
            val newObj = DNDCreation.dropClass(data = data, dataPoint = dataPoint, commandStackHandler, state)
            val addCommand = state.getAddCommandFor(newObj)
            commandStackHandler.add(addCommand)
            true
        } else if (data is Generalization) {
            logger.debug { "Accept drop for state name: ${state.id} Item data: -> $data" }
            val general = state.observableDiagram.elements.value.firstOrNull { it.showsElement(data.general) }
            val special = state.observableDiagram.elements.value.firstOrNull { it.showsElement(data.specific) }
            if (special != null && general != null) {
                val initSourceAnchor = Anchor(AnchorSide.N, 0.5f)
                val initTargetAnchor = Anchor(AnchorSide.S, 0.5f)
                val arrow = Arrow(
                    initOffsetPath = Arrow.fourPointArrowOffsetPath(
                        sourceBoundingShape = special.boundingShape,
                        targetBoundingShape = general.boundingShape,
                        sourceAnchor = initSourceAnchor,
                        targetAnchor = initTargetAnchor
                    ),
                    initArrowType = ArrowType.GENERALIZATION,
                    data = data,
                    initSourceAnchor = initSourceAnchor,
                    initTargetAnchor = initSourceAnchor,
                    initSourceBoundingShape = special.boundingShape,
                    initTargetBoundingShape = general.boundingShape
                )
                val addCommand = state.getAddCommandForArrow(arrow)
                println("arrow = ${arrow}")
                commandStackHandler.add(addCommand)
                true
            } else false
        } else false
    }
}

object DNDCreation : KLogging() {
    fun dropClass(
        data: Class, dataPoint: Offset, commandStackHandler: CommandStackHandler, state: EditorTabViewModel
    ): UMLClass {
        return UMLClassFactory.createInstance(
            umlClass = data,
            initBoundingRect = BoundingRectState(
                topLeftPacked = packFloats(dataPoint.x, dataPoint.y), width = 255f, height = 300f
            ),
            onMoveOrResize = { moveResizeCommand ->
                logger.debug { "Update Arrows after UMLClass movement" }
                val updateArrowPathCommands = state.observableDiagram.updateArrows(moveResizeCommand, data)
                if (updateArrowPathCommands.isEmpty()) {
                    commandStackHandler.add(moveResizeCommand)
                } else {
                    commandStackHandler.add(CompoundCommand(updateArrowPathCommands + moveResizeCommand))
                }
            },
            deleteCommand = state::getRemoveCommandFor
        )
    }
}