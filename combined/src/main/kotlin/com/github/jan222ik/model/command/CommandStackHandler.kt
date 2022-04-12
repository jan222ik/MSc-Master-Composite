package com.github.jan222ik.model.command

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KLogging

class CommandStackHandler(
    val stackSize: Int = 60,
    val scope: CoroutineScope
) {
    companion object : KLogging()

    private val jobHandler = JobHandler(scope)
     val commandStack = mutableStateOf(emptyList<ICommand>())
     val commandStackIdx = mutableStateOf(commandStack.value.lastIndex)

    var hasUndo by mutableStateOf(commandStackIdx.value != -1 && commandStack.value.isNotEmpty())
    var hasRedo by mutableStateOf(commandStack.value.isNotEmpty() && commandStackIdx.value != commandStack.value.lastIndex)


    fun add(command: ICommand) {
        val start = 0.takeUnless { commandStack.value.size == stackSize } ?: 1
        commandStack.value = commandStack.value.subList(start, commandStackIdx.value.inc()) + command
        commandStackIdx.value = commandStack.value.lastIndex
        scope.launch { command.execute(jobHandler) }
        refresh()
    }

    fun undo() {
        if (hasUndo) {
            val idx = commandStackIdx.value
            logger.debug { "Undo for command at $idx" }
            val command = commandStack.value[idx]
            if (command.canUndo()) {
                scope.launch { command.undo() }
                commandStackIdx.value = idx.dec()
                refresh()
            }
        }
    }

    fun redo() {
        if (hasRedo) {
            val idx = commandStackIdx.value.inc()
            val command = commandStack.value[idx]
            logger.debug { "Redo for command at $idx" }
            if (command.isActive()) {
                scope.launch { command.execute(jobHandler) }
                commandStackIdx.value = idx
                refresh()
            }
        }
    }

    private fun refresh() {
        hasUndo = commandStackIdx.value != -1 && commandStack.value.isNotEmpty()
        hasRedo = commandStack.value.isNotEmpty() && commandStackIdx.value != commandStack.value.lastIndex
    }
}