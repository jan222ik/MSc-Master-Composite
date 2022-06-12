package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.notifications.Notification
import com.github.jan222ik.model.notifications.Notifications
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import mu.KLogging

class NotImplementedCommand(
    val name: String
) : ICommand {
    companion object : KLogging()

    override fun isActive(): Boolean = true
    override suspend fun execute(handler: JobHandler) {
        logger.debug { "Execute NotImplementedCommand for \"$name\"" }
        Notifications.addNotification(
            notification = Notification(
                title = "Not Implemented",
                message = "The action for \"$name\" is not implemented in this version of the prototype.",
                decayTimeMilli = 3000
            )
        )
    }

    override fun canUndo(): Boolean = true
    override suspend fun undo() {
        logger.debug { "Undo NotImplementedCommand for \"$name\"" }
    }
}