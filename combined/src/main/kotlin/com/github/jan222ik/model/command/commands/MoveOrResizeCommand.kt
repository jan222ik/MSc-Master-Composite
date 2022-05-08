package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler

class MoveOrResizeCommand(
    val target: MovableAndResizeableComponent,
    val before: BoundingRectState,
    val after: BoundingRectState
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