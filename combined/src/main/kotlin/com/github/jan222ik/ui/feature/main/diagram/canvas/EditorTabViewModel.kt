package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.github.jan222ik.canvas.data.DrawPoint
import com.github.jan222ik.canvas.viewport.Viewport
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.uml.MovableAndResizeableComponent
import kotlin.random.Random

class EditorTabViewModel(
    val name: String,
    val type: DiagramType
) {
    val id = Random.nextLong()

    val maxViewport = mutableStateOf(Viewport(0f, 0f, 100f, 100f))
    val viewport = mutableStateOf(Viewport(0f, 0f, 25f, 25f))
    val items = mutableStateOf(emptyList<MovableAndResizeableComponent>())
    var coords by mutableStateOf(Offset.Unspecified)
    var size by mutableStateOf(IntSize.Zero)

    fun addItem(item: MovableAndResizeableComponent) {
        items.value += item
    }

    fun removeItem(item: MovableAndResizeableComponent) {
        items.value -= item
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
}