package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
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
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.tree.FileTree
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
fun EditorTabComponent(stateOut: EditorTabViewModel, projectTreeHandler: ProjectTreeHandler) {
    val state = remember(stateOut.id) { stateOut }
    val logger = remember { NamedKLogging("com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent").logger }
    val commandStackHandler = LocalCommandStackHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                state.coords = it.positionInWindow()
            }
            .clipToBounds()
    ) {
        val dropHandler = LocalDropTargetHandler.current
        val dndActions = DNDEditorActions(
            state, logger, commandStackHandler, projectTreeHandler
        )

        val observableDiagram = remember(state, state.id, state.observableDiagram) { state.observableDiagram }
        val arrows = remember(observableDiagram) { observableDiagram.arrows.value }
        val elements = remember(observableDiagram) { observableDiagram.elements.value }
        state.id.let { id ->
            ScrollableCanvas(
                id = id,
                viewportState = state.viewport,
                canvasThenModifier = Modifier.dndDropTarget(
                    handler = dropHandler,
                    dropActions = dndActions
                ),
                elements = elements,
                arrows = arrows,
                projectTreeHandler = projectTreeHandler
            )
        }
        NavigateDiagramUPButton(
            modifier = Modifier.align(Alignment.TopStart),
            text = observableDiagram.upwardsDiagramLink ?: "Not configured",
            enabled = observableDiagram.upwardsDiagramLink != null,
            onClick = {
                logger.debug { "TODO: Navigate to '${observableDiagram.upwardsDiagramLink}'" }
                observableDiagram.upwardsDiagramLink?.let { location ->
                    FileTree.treeHandler.value?.metamodelRoot?.findDiagramElementByLocation(location)?.target?.let {
                        EditorManager.moveToOrOpenDiagram(
                            tmmDiagram = it,
                            commandStackHandler = commandStackHandler
                        )
                    }
                }
            }
        )
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