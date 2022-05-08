package com.github.jan222ik.ui.uml

import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.BoundingRectState

object UMLClassFactory {
    fun createInstance(
        umlClass: org.eclipse.uml2.uml.Class,
        initBoundingRect: BoundingRectState,
        onMoveOrResize: (MoveOrResizeCommand) -> Unit,
        deleteCommand: (UMLClass) -> RemoveFromDiagramCommand
    ): UMLClass {
        val newObj = UMLClass(
            umlClass = umlClass,
            initBoundingRect = initBoundingRect,
            onNextUIConfig = { self, old, new ->
                UMLClass.logger.debug { "NEW UI CONFIG" }
                onMoveOrResize(
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