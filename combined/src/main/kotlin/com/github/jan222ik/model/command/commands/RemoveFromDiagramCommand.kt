package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand
import mu.KLogging

abstract class RemoveFromDiagramCommand : ICommand {
    companion object : KLogging()
    override fun isActive(): Boolean = true
    override fun canUndo(): Boolean = true
}