package com.github.jan222ik.ui.uml

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.feature.main.tree.ModelTreeItem
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.util.HorizontalDivider
import mu.KLogging
import org.eclipse.uml2.uml.Stereotype
import org.eclipse.uml2.uml.VisibilityKind

@OptIn(ExperimentalFoundationApi::class)
class UMLClass(
    val umlClass: org.eclipse.uml2.uml.Class,
    initUiConfig: DiagramBlockUIConfig,
    onNextUIConfig: (MovableAndResizeableComponent, DiagramBlockUIConfig, DiagramBlockUIConfig) -> Unit,
) : MovableAndResizeableComponent(initUiConfig, onNextUIConfig) {

    companion object : KLogging()

    lateinit var deleteSelfCommand: RemoveFromDiagramCommand

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler) {
        remember(projectTreeHandler.singleSelectedItem) {
            selected = projectTreeHandler.singleSelectedItem?.let {
                if (it is ModelTreeItem) {
                    it.getElement() == umlClass
                } else null
            } ?: false
        }

        val commandStackHandler = LocalCommandStackHandler.current
        Column(
            modifier = Modifier.mouseCombinedClickable {
                if (buttons.isPrimaryPressed) {
                    println("Clicked")
                    projectTreeHandler.setTreeSelectionByElements?.invoke(listOf(umlClass))
                }
                if (buttons.isSecondaryPressed) {
                    // TODO show context menu for item
                    commandStackHandler.add(deleteSelfCommand)
                }
            },
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = umlClass.appliedStereotypesString())
                Text(text = umlClass.labelOrName())
            }
            HorizontalDivider(Modifier.fillMaxWidth())
            Column(
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "attributes")
                Column(modifier = Modifier.fillMaxWidth()) {
                    umlClass.attributes.forEach { it.displayProp(projectTreeHandler) }
                }
            }
        }

    }

    fun org.eclipse.uml2.uml.Class.appliedStereotypesString(): String =
        this.appliedStereotypes.toStereotypesString()

    fun List<Stereotype>.toStereotypesString(limit: Int = this.size): String {
        return if (this.isEmpty()) "" else this.subList(0, limit)
            .joinToString(prefix = "<<", postfix = ">>", separator = ",") { it.labelOrName() }
    }

    fun org.eclipse.uml2.uml.NamedElement.labelOrName(default: String = "No name"): String = label ?: name ?: default

    fun org.eclipse.uml2.uml.Property.typeNameString(): String {
        return "<" + (this.type?.labelOrName("undefined")) + ">"
    }

    fun org.eclipse.uml2.uml.NamedElement.visibilityChar(): Char {
        return when (this.visibility) {
            VisibilityKind.PUBLIC_LITERAL -> '+'
            VisibilityKind.PRIVATE_LITERAL -> '-'
            VisibilityKind.PROTECTED_LITERAL -> '#'
            VisibilityKind.PACKAGE_LITERAL -> '~'
            else -> ' '
        }
    }

    @Composable
    fun org.eclipse.uml2.uml.Property.displayProp(projectTreeHandler: ProjectTreeHandler) {
        this.let { prop ->
            val selected = remember(projectTreeHandler.singleSelectedItem) {
                projectTreeHandler.singleSelectedItem?.let {
                    if (it is ModelTreeItem) {
                        it.getElement() == prop
                    } else null
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    projectTreeHandler.setTreeSelectionByElements?.invoke(listOf(prop))
                }.then(
                    if (selected == true) {
                        Modifier.border(width = 1.dp, color = MaterialTheme.colors.primary)
                    } else Modifier
                )
            ) {
                Image(
                    painterResource("drawables/uml_icons/Property.gif"),
                    contentDescription = null,
                    modifier = Modifier.width(16.dp)
                )
                val vis = prop.visibilityChar()
                val str =
                    prop.applicableStereotypes.toStereotypesString() + " $vis " + prop.labelOrName() + " " + prop.typeNameString() + " [1]"
                Text(text = str, maxLines = 1, overflow = TextOverflow.Ellipsis)
                // TODO Visibility indication
                // TODO type
                // TODO Multiplicity
            }
        }
    }
}