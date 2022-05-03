package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.ui.uml.MovableAndResizeableComponent

class MoveOrResizeCommand(
    val target: MovableAndResizeableComponent,
    val before: DiagramBlockUIConfig,
    val after: DiagramBlockUIConfig
) : ICommand {
    override fun isActive(): Boolean = true
    override suspend fun execute(handler: JobHandler) {
        target.useConfig(after)
    }

    override fun canUndo(): Boolean = true

    override suspend fun undo() {
        target.useConfig(before)
    }

}