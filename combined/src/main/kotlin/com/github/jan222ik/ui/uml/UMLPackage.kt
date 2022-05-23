package com.github.jan222ik.ui.uml

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.MovableBaseUI
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.value.Space
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Stereotype

class UMLPackage(
    val umlPackage: org.eclipse.uml2.uml.Package,
    initBoundingRect: BoundingRectState,
    onNextUIConfig: (MovableAndResizeableComponent, BoundingRectState, BoundingRectState) -> Unit,
) : MovableAndResizeableComponent(
    initBoundingRect = initBoundingRect,
    onNextUIConfig = onNextUIConfig,
    movableBaseUI = MovableBaseUI(
        cardBackground = Color.Transparent,
        useBasecardBorder = false,
        cardElevation = 0.dp
    )
) {
    lateinit var deleteSelfCommand: RemoveFromDiagramCommand

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler) {
        val tmmClassPath = remember(
            umlPackage,
            projectTreeHandler,
            projectTreeHandler.metamodelRoot
        ) {
            projectTreeHandler.metamodelRoot.findModellingElementOrNull(umlPackage)
        }
        remember(projectTreeHandler.singleSelectedItem.value, tmmClassPath) {
            selected = tmmClassPath?.let { projectTreeHandler.treeSelection.value.contains(it.target) } ?: false
        }

        Column(
            modifier = Modifier.mouseCombinedClickable {
                if (buttons.isPrimaryPressed) {
                    println("Clicked package")
                    tmmClassPath?.target?.let {
                        projectTreeHandler.setTreeSelection(listOf(it))
                    }
                }
                if (buttons.isSecondaryPressed) {
                    showContextMenu.value = true
                }
            }
        ) {
            val color = Color.White
            Surface(
                color = color,
                border = BorderStroke(width = 1.dp, color = Color.Black)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Space.dp16, vertical = Space.dp4).width(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(Space.dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource("drawables/uml_icons/Package.gif"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val appliedStereotypesString = umlPackage.appliedStereotypesString()
                        if (appliedStereotypesString.isNotEmpty()) {
                            Text(text = appliedStereotypesString)
                        }
                        Text(text = umlPackage.labelOrName())
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f).offset(y = (-1).dp),
                color = color,
                border = BorderStroke(width = 1.dp, color = Color.Black)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth()
                ) {
                    Text("Package Content")
                }
            }
        }
    }

    override fun getMenuContributions(): List<MenuContribution> {
        return listOf(
            MenuContribution.Contentful.MenuItem(displayName = "Option 1"),
            MenuContribution.Contentful.MenuItem(
                displayName = "Delete from Diagram",
                command = deleteSelfCommand
            )
        )
    }

    override fun showsElement(element: Element?): Boolean {
        return umlPackage == element
    }

    private fun org.eclipse.uml2.uml.Package.appliedStereotypesString(): String =
        this.appliedStereotypes.toStereotypesString()

    private fun List<Stereotype>.toStereotypesString(limit: Int = this.size): String {
        return if (this.isEmpty()) "" else this.subList(0, limit)
            .joinToString(prefix = "<<", postfix = ">>", separator = ",") { it.labelOrName() }
    }

    private fun org.eclipse.uml2.uml.NamedElement.labelOrName(default: String = "No name"): String =
        label ?: name ?: default
}