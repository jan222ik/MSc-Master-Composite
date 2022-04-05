package com.github.jan222ik.ui.uml

import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.menu_tool_bar.ICommand
import mu.KLogging

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