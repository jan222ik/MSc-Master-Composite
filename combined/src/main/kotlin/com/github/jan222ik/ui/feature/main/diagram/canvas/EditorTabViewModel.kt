package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.Viewport
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.uml.DiagramHolderObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class EditorTabViewModel(
    initialViewport: Viewport = Viewport(),
    val observableDiagram: DiagramHolderObservable,
    val tmmDiagram: TMM.ModelTree.Diagram
) {
    val id = Random.nextLong()

    val viewport = mutableStateOf(initialViewport)
    var coords by mutableStateOf(Offset.Unspecified)

    val name: String
        get() = observableDiagram.diagramName.tfv.text

    val type: DiagramType
        get() = observableDiagram.diagramType

    fun addItem(item: MovableAndResizeableComponent) {
        observableDiagram.elements.add(item)
    }

    fun removeItem(item: MovableAndResizeableComponent) {
        observableDiagram.elements.remove(item)
    }

    fun addArrow(item: Arrow) {
        observableDiagram.arrows.add(item)
    }

    fun removeArrow(item: Arrow) {
        observableDiagram.arrows.remove(item)
    }

    fun getRemoveCommandFor(item: MovableAndResizeableComponent): RemoveFromDiagramCommand {
        return object : RemoveFromDiagramCommand() {
            override suspend fun execute(handler: JobHandler) {
                logger.debug { "Delete item from diagram" }
                withContext(Dispatchers.Main) {
                    removeItem(item)
                }
            }

            override suspend fun undo() {
                logger.debug { "Undo: Delete item from diagram" }
                withContext(Dispatchers.Main) {
                    addItem(item)
                }
            }
        }
    }

    fun getAddCommandFor(item: MovableAndResizeableComponent): AddToDiagramCommand {
        return object : AddToDiagramCommand() {
            override suspend fun execute(handler: JobHandler) {
                logger.debug { "AddToDiagramCommand > Execute" }
                withContext(Dispatchers.Main) {
                    addItem(item)
                }
            }

            override suspend fun undo() {
                logger.debug { "AddToDiagramCommand > Undo" }
                withContext(Dispatchers.Main) {
                    removeItem(item)
                }
            }
        }
    }

    fun getAddCommandForArrow(item: Arrow): AddToDiagramCommand {
        return object : AddToDiagramCommand() {
            override suspend fun execute(handler: JobHandler) {
                logger.debug { "AddToDiagramCommand > Execute > Arrow" }
                addArrow(item)
            }

            override suspend fun undo() {
                logger.debug { "AddToDiagramCommand > Undo > Arrow" }
                removeArrow(item)
            }
        }
    }
}