package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.command.ICommand

abstract class AddToDiagramCommand : ICommand {
    override fun isActive(): Boolean = true
    override fun canUndo(): Boolean = true
}