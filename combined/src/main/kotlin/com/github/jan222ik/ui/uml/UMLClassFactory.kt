package com.github.jan222ik.ui.uml

import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import org.eclipse.uml2.uml.Class

object UMLClassFactory {
    fun createInstance(
        umlClass: Class,
        initBoundingRect: BoundingRectState,
        onMoveOrResize: (MoveOrResizeCommand) -> Unit,
        deleteCommand: (UMLClass) -> RemoveFromDiagramCommand,
        filters: List<DiagramStateHolders.UMLRef.ComposableRef.ClassRef.UMLClassRefFilter.Compartment>
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
            },
            filter = filters
        ).apply {
            deleteSelfCommand = deleteCommand.invoke(this)
        }
        return newObj
    }

    fun createNestableInstance(
        umlClass: Class,
        initBoundingRect: BoundingRectState,
        onMoveOrResize: (MoveOrResizeCommand) -> Unit,
        deleteCommand: (NestableUMLClass) -> RemoveFromDiagramCommand,
        nestedContent: Pair<List<MovableAndResizeableComponent>, List<Arrow>>
    ) : NestableUMLClass {
        val newObj = NestableUMLClass(
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
            },
            nestedContent = nestedContent
        ).apply {
            deleteSelfCommand = deleteCommand.invoke(this)
        }
        return newObj
    }
}