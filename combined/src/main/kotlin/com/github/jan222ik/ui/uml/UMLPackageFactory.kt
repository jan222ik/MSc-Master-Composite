package com.github.jan222ik.ui.uml

import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import org.eclipse.uml2.uml.Package

object UMLPackageFactory {
    fun createInstance(
        umlPackage: Package,
        initBoundingRect: BoundingRectState,
        onMoveOrResize: (MoveOrResizeCommand) -> Unit,
        deleteCommand: (UMLPackage) -> RemoveFromDiagramCommand,
        packageRef: DiagramStateHolders.UMLRef.ComposableRef.PackageRef
    ): UMLPackage {
        val newObj = UMLPackage(
            packageRef = packageRef,
            umlPackage = umlPackage,
            initBoundingRect = initBoundingRect
        ) { self, old, new ->
            UMLClass.logger.debug { "NEW UI CONFIG" }
            onMoveOrResize(
                MoveOrResizeCommand(
                    target = self,
                    before = old,
                    after = new
                )
            )
        }.apply {
            deleteSelfCommand = deleteCommand.invoke(this)
        }
        return newObj
    }
}