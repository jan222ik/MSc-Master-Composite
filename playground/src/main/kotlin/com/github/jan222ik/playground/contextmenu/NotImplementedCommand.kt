package com.github.jan222ik.playground.contextmenu

import mu.KLogging

class NotImplementedCommand(
    val name: String
) : ICommand {
    companion object : KLogging()

    override fun isActive(): Boolean = true
    override suspend fun execute() {
        logger.debug { "Execute NotImplementedCommand for \"$name\"" }
    }

    override fun canUndo(): Boolean = true
    override suspend fun undo() {
        logger.debug { "Undo NotImplementedCommand for \"$name\"" }
    }
}