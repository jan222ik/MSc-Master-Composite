package com.github.jan222ik.ui.uml

import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand

object UMLClassFactory {
    fun createInstance(
        umlClass: org.eclipse.uml2.uml.Class,
        initUiConfig: DiagramBlockUIConfig,
        commandStackHandler: CommandStackHandler,
        deleteCommand: (UMLClass) -> RemoveFromDiagramCommand
    ): UMLClass {
        val newObj = UMLClass(
            umlClass = umlClass,
            initUiConfig = initUiConfig,
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