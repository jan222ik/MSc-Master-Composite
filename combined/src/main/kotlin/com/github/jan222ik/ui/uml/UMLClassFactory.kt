package com.github.jan222ik.ui.uml

import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRect

object UMLClassFactory {
    fun createInstance(
        umlClass: org.eclipse.uml2.uml.Class,
        initBoundingRect: BoundingRect.State,
        commandStackHandler: CommandStackHandler,
        deleteCommand: (UMLClass) -> RemoveFromDiagramCommand
    ): UMLClass {
        val newObj = UMLClass(
            umlClass = umlClass,
            initBoundingRect = initBoundingRect,
            onNextUIConfig = { self, old, new ->
                UMLClass.logger.debug { "NEW UI CONFIG" }
                commandStackHandler.add(
                    MoveOrResizeCommand(
                        target = self,
                        before = old,
                        after = new
                    )
                )
            }
        ).apply {
            deleteSelfCommand = deleteCommand.invoke(this)
        }
        return newObj
    }
}