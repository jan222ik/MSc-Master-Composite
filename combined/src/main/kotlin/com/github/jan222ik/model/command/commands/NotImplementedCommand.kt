package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import mu.KLogging

class NotImplementedCommand(
    val name: String
) : ICommand {
    companion object : KLogging()

    override fun isActive(): Boolean = true
    override suspend fun execute(handler: JobHandler) {
        logger.debug { "Execute NotImplementedCommand for \"$name\"" }
    }

    override fun canUndo(): Boolean = true
    override suspend fun undo() {
        logger.debug { "Undo NotImplementedCommand for \"$name\"" }
    }
}