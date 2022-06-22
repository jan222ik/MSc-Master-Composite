package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler

data class CompoundCommand(
    val commands: List<ICommand>,
    val reverseUndoOrder: Boolean = true
) : ICommand {
    override fun isActive(): Boolean = commands.all { it.isActive() }

    override suspend fun execute(handler: JobHandler) {
        commands.onEach { it.execute(handler) }
    }

    override fun canUndo(): Boolean {
        return commands.all { it.canUndo() }
    }

    override suspend fun undo() {
        val list = if (reverseUndoOrder) commands.reversed() else commands
        list.onEach {
            it.undo()
        }
    }

}