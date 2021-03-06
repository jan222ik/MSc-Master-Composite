package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.packFloats
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.CompoundCommand
import com.github.jan222ik.model.notifications.Notification
import com.github.jan222ik.model.notifications.Notifications
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.ScrollableCanvas
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.components.dnd.DnDAction
import com.github.jan222ik.ui.components.dnd.dndDropTarget
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.uml.Anchor
import com.github.jan222ik.ui.uml.AnchorSide
import com.github.jan222ik.ui.uml.UMLClass
import com.github.jan222ik.ui.uml.UMLClassFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KLogger
import mu.KLogging
import mu.NamedKLogging
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Generalization

@Composable
fun EditorTabComponent(stateOut: EditorTabViewModel, projectTreeHandler: ProjectTreeHandler) {
    val state = remember(stateOut.id) { stateOut }
    val logger =
        remember { NamedKLogging("com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent").logger }
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
        val dndActions = remember(state.id) {  DNDEditorActions(
            state, logger, commandStackHandler, projectTreeHandler
        )}

        val scope = rememberCoroutineScope()
        val observableDiagram = remember(state, state.id, state.observableDiagram) { state.observableDiagram }
        val arrows = observableDiagram.arrows
        val elements = observableDiagram.elements
        val helper = remember(scope) { AlignmentHelper(scope) }

        remember(state, state.id, observableDiagram.elements.count()) {
            println("Update elements")
            scope.launch {
                helper.boundingBoxes.emit(observableDiagram.elements.map { it.boundingShape })
            }
        }
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
                projectTreeHandler = projectTreeHandler,
                tmmDiagram = state.tmmDiagram,
                helper = helper,
                createBlockFromDrop = {
                    dndActions.drop(pos = it.round(), data = "Block")
                }
            )
        }
        NavigateDiagramUPButton(
            modifier = Modifier.align(Alignment.TopStart),
            text = observableDiagram.upwardsDiagramLink?.split("::")?.lastOrNull(),
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
            },
            icon = {
                observableDiagram.upwardsDiagramLink?.let { location ->
                    FileTree.treeHandler.value?.metamodelRoot?.findDiagramElementByLocation(location)?.target?.let {
                        Icon(painter = it.initDiagram.diagramType.iconAsPainter(), contentDescription = null)
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
            val general = state.observableDiagram.elements.firstOrNull {
                it.showsElement(data.general) || it.showsElementFromAssoc(
                    element = data.general,
                    false
                )
            }
            val special = state.observableDiagram.elements.firstOrNull {
                it.showsElement(data.specific) || it.showsElementFromAssoc(
                    element = data.specific,
                    true
                )
            }
            if (special != null && general != null) {
                val (initSourceAnchor, initTargetAnchor) = Arrow.findIdealAnchors(special.boundingShape, general.boundingShape)
                //val initSourceAnchor = Anchor(AnchorSide.N, 0.5f)
                //val initTargetAnchor = Anchor(AnchorSide.S, 0.5f)
                val fourPointArrowOffsetPath = Arrow.fourPointArrowOffsetPath(
                    sourceBoundingShape = special.boundingShape,
                    targetBoundingShape = general.boundingShape,
                    sourceAnchor = initSourceAnchor,
                    targetAnchor = initTargetAnchor
                )
                val arrow = Arrow(
                    initArrowType = ArrowType.GENERALIZATION,
                    initSourceAnchor = initSourceAnchor,
                    initTargetAnchor = initTargetAnchor,
                    initOffsetPath = fourPointArrowOffsetPath.first,
                    initBoundingShape = fourPointArrowOffsetPath.second,
                    data = data,
                    initSourceBoundingShape = special.boundingShape,
                    initTargetBoundingShape = general.boundingShape,
                    member0ArrowTypeOverride = null,
                    member1ArrowTypeOverride = null
                )
                val addCommand = state.getAddCommandForArrow(arrow)
                println("arrow = ${arrow}")
                commandStackHandler.add(addCommand)
                true
            } else false
        } else if (data is String) {
            when (data) {
                "Block" -> {
                    DemoMenuContributions.createUMLClassInTMMDiagram(state.tmmDiagram, pos.toOffset(), state)
                    true
                }

                else -> {
                    CoroutineScope(SupervisorJob()).launch {
                        Notifications.addNotification(Notification("Not Implemented", "The drop action of create '$data' is not implemented yet.", 3000L))
                    }
                    logger.warn { "Unknown drag and drop of string data $data" }
                    false
                }
            }

        } else {
            logger.warn { "Unknown drag and drop of data $data" }
            false
        }
    }
}

object DNDCreation : KLogging() {
    fun dropClass(
        data: Class, dataPoint: Offset, commandStackHandler: CommandStackHandler, state: EditorTabViewModel
    ): UMLClass {
        return UMLClassFactory.createInstance(
            umlClass = data,
            initBoundingRect = BoundingRectState(
                topLeftPacked = packFloats(dataPoint.x, dataPoint.y), width = 255f, height = 200f
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
            deleteCommand = state::getRemoveCommandFor,
            filters = emptyList() // TODO change if filters are implemented
        )
    }
}