package com.github.jan222ik.model.command.commands

import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.uml.DiagramHolder
import org.eclipse.uml2.uml.NamedElement

class CreateDiagramCommand(
    val rootTMM: TMM.ModelTree.Ecore,
    val diagramType: DiagramType
) : ICommand {

    val diagram = TMM.ModelTree.Diagram(initDiagram = DiagramHolder(
        name = "New diagram",
        diagramType = diagramType,
        upwardsDiagramLink = null, // TODO find closest package diagram
        location = rootTMM.element.let { if (it is NamedElement) it.name else "undefined" },
        content = emptyList() // TODO create class shape for parametric
    )).also { it.parent = rootTMM }

    override fun isActive(): Boolean = EditorManager.allowEdit.value

    override suspend fun execute(handler: JobHandler) {
        rootTMM.ownedElements.add(diagram)
        EditorManager.moveToOrOpenDiagram(tmmDiagram = diagram, CommandStackHandler.INSTANCE)
    }

    override fun canUndo(): Boolean = true

    override suspend fun undo() {
        EditorManager.closeEditorForDiagram(diagram)
        rootTMM.ownedElements.remove(diagram)
    }
}