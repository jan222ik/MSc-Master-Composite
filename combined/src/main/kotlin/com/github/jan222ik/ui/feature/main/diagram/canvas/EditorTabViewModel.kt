package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.Viewport
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import kotlin.random.Random

class EditorTabViewModel(
    val name: String,
    val type: DiagramType,
    initialViewport: Viewport = Viewport(),
    initialItems: List<MovableAndResizeableComponent> = emptyList(),
    initialArrows: List<Arrow> = emptyList()
) {
    val id = Random.nextLong()

    val viewport = mutableStateOf(initialViewport)
    val items = mutableStateOf(initialItems)
    val arrows = mutableStateOf(initialArrows)
    var coords by mutableStateOf(Offset.Unspecified)

    fun addItem(item: MovableAndResizeableComponent) {
        items.value += item
    }

    fun removeItem(item: MovableAndResizeableComponent) {
        items.value -= item
    }

    fun addArrow(item: Arrow) {
        arrows.value += item
    }

    fun removeArrow(item: Arrow) {
        arrows.value -= item
    }

    fun getRemoveCommandFor(item: MovableAndResizeableComponent): RemoveFromDiagramCommand {
        return object : RemoveFromDiagramCommand() {
            override suspend fun execute(handler: JobHandler) {
                logger.debug { "Delete item from diagram" }
                removeItem(item)
            }

            override suspend fun undo() {
                logger.debug { "Undo: Delete item from diagram" }
                addItem(item)
            }
        }
    }

    fun getAddCommandFor(item: MovableAndResizeableComponent): AddToDiagramCommand {
        return object : AddToDiagramCommand() {
            override suspend fun execute(handler: JobHandler) {
                logger.debug { "AddToDiagramCommand > Execute" }
                addItem(item)
            }

            override suspend fun undo() {
                logger.debug { "AddToDiagramCommand > Undo" }
                removeItem(item)
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

    fun toState() : State {
        return State(
            name = name,
            type = type,
            viewport = viewport.value,
            items = items.value,
            arrows = arrows.value
        )
    }

    companion object {
        fun fromState(state: State): EditorTabViewModel {
            return EditorTabViewModel(
                name = state.name,
                type = state.type,
                initialViewport = state.viewport,
                initialItems = state.items,
                initialArrows = state.arrows
            )
        }
    }

    data class State(
        val name: String,
        val type: DiagramType,
        val viewport: Viewport,
        val items: List<MovableAndResizeableComponent>,
        val arrows: List<Arrow>
    )
}