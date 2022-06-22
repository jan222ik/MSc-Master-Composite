package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.CreateDiagramCommand
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.diagram.canvas.DNDCreation
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.tree.ModelTreeItem
import com.github.jan222ik.ui.uml.DiagramStateHolders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

object DemoMenuContributions {
    fun links(hasLink: Boolean): MenuContribution.Contentful {
        return if (hasLink) {
            MenuContribution.Contentful.NestedMenuItem(
                icon = null,
                displayName = "Link",
                nestedItems = listOf(
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaImgVector(Icons.Default.Edit),
                        displayName = "Edit Link"
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaImgVector(Icons.Default.Remove),
                        displayName = "Delete Link"
                    )
                )
            )
        } else {
            MenuContribution.Contentful.MenuItem(
                icon = DrawableIcon.viaImgVector(Icons.Default.Add),
                displayName = "Create Link"
            )
        }
    }



    @OptIn(ExperimentalFoundationApi::class)
    fun diagramContributionsFor(item: ModelTreeItem, onDismiss: () -> Unit): MenuContribution.Contentful {
        val isPackage = item is ModelTreeItem.PackageItem
        val isBlock = item is ModelTreeItem.ClassItem
        fun DiagramType.menuContribution(): MenuContribution.Contentful.MenuItem {
            return MenuContribution.Contentful.MenuItem(
                icon = DrawableIcon.viaPainterConstruction { iconAsPainter() },
                displayName = name,
                command = CreateDiagramCommand(
                    rootTMM = item.tmmModelItem,
                    diagramType = this,
                    onDismiss = onDismiss
                )
            )
        }

        return MenuContribution.Contentful.NestedMenuItem(
            icon = null,
            displayName = "New diagram",
            nestedItems = kotlin.run {
                if (isPackage) {
                    listOf(
                        DiagramType.PACKAGE.menuContribution(),
                        DiagramType.BLOCK_DEFINITION.menuContribution()
                    )
                } else if (isBlock) {
                    listOf(
                        DiagramType.PARAMETRIC.menuContribution()
                    )
                } else emptyList()
            }
        )
    }

    val counter = AtomicInteger(0)
    fun createUMLClassInTMMDiagram(tmmDiagram: TMM.ModelTree.Diagram, inDiagramOffset: Offset?, state: EditorTabViewModel?): TMM.ModelTree.Ecore.TClass {
        val commandStackHandler = CommandStackHandler.INSTANCE
        val newTmm = tmmDiagram.closestPackage().createOwnedClass(
            name = "Block${counter.getAndIncrement()}",
            isAbstract = false
        )
        if (inDiagramOffset != null && state != null) {
            val posInComponent = inDiagramOffset.minus(state.coords)
            val dataPoint = state.viewport.value.origin + posInComponent
            val newObj = DNDCreation.dropClass(
                data = newTmm.umlClass,
                dataPoint = dataPoint, commandStackHandler, state
            )
            val addCommand = state.getAddCommandFor(newObj)
            commandStackHandler.add(addCommand)
        }
        return newTmm
    }

    fun diagramNewElementMenuConfig(tmmRoot: TMM.ModelTree.Diagram, inDiagramOffset: Offset?, state: EditorTabViewModel?, onDismiss: () -> Unit, ): MenuContribution.Contentful.NestedMenuItem {
        val items =
            listOf(MenuContribution.Contentful.MenuItem(
                icon = DrawableIcon.Block,
                displayName = "Block",
                command = object : ICommand {

                    lateinit var uml: TMM.ModelTree.Ecore.TClass

                    override fun isActive(): Boolean = EditorManager.allowEdit.value

                    override suspend fun execute(handler: JobHandler) {
                        withContext(Dispatchers.Main) {
                            uml = createUMLClassInTMMDiagram(tmmRoot, inDiagramOffset, state)
                            onDismiss.invoke()
                        }
                    }

                    override fun canUndo(): Boolean = false

                    override suspend fun undo() {}

                }
            ))

        return MenuContribution.Contentful.NestedMenuItem(
            icon = DrawableIcon.viaImgVector(Icons.Default.Add),
            displayName = "Add child",
            nestedItems = items
        )
    }

    fun addProperty(tmmClass: TMM.ModelTree.Ecore.TClass, onDismiss: () -> Unit): MenuContribution {
        return MenuContribution.Contentful.MenuItem(
            icon = DrawableIcon.Property,
            displayName = "Add property",
            command = object : ICommand {
                override fun isActive(): Boolean = true

                override suspend fun execute(handler: JobHandler) {
                    val createOwnedProperty = tmmClass.createOwnedProperty()
                    onDismiss.invoke()
                }

                override fun canUndo(): Boolean = false

                override suspend fun undo() {

                }

                override fun pushToStack(): Boolean = false
            }
        )
    }

    val arrowFrom = mutableStateOf<ArrowData?>(null)
    val paletteSelection = mutableStateOf<String?>(null)

    data class ArrowData(
        val start: MovableAndResizeableComponent,
        val type: ArrowType,
        val member0ArrowTypeOverride: DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation? = null,
        val member1ArrowTypeOverride: DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation? = null
    )
}