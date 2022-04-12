package com.github.jan222ik.ui.uml

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.util.HorizontalDivider
import org.eclipse.uml2.uml.Stereotype
import org.eclipse.uml2.uml.VisibilityKind

class UMLClass(
    val umlClass: org.eclipse.uml2.uml.Class,
    initUiConfig: DiagramBlockUIConfig,
    onNextUIConfig: (MovableAndResizeableComponent, DiagramBlockUIConfig, DiagramBlockUIConfig) -> Unit,
) : MovableAndResizeableComponent(initUiConfig, onNextUIConfig) {
    lateinit var deleteSelfCommand: RemoveFromDiagramCommand
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun content() {
        val commandStackHandler = LocalCommandStackHandler.current
        Column(
            modifier = Modifier.mouseCombinedClickable {
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
                    umlClass.attributes.forEach { it.displayProp() }
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
    fun org.eclipse.uml2.uml.Property.displayProp() {
        this.let { prop ->
            Row {
                // TODO ICON
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