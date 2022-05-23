package com.github.jan222ik.ui.uml

import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRect
import com.github.jan222ik.ui.adjusted.BoundingRectState

object UMLPackageFactory {
    fun createInstance(
        umlPackage: org.eclipse.uml2.uml.Package,
        initBoundingRect: BoundingRectState,
        onMoveOrResize: (MoveOrResizeCommand) -> Unit,
        deleteCommand: (UMLPackage) -> RemoveFromDiagramCommand
    ): UMLPackage {
        val newObj = UMLPackage(
            umlPackage = umlPackage,
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