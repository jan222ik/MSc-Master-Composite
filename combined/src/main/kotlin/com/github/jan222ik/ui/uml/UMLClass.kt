package com.github.jan222ik.ui.uml

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.util.HorizontalDivider
import mu.KLogging
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.impl.EnumerationImpl
import org.eclipse.uml2.uml.internal.impl.PrimitiveTypeImpl

@OptIn(ExperimentalFoundationApi::class)
class UMLClass(
    val umlClass: org.eclipse.uml2.uml.Class,
    initBoundingRect: BoundingRectState,
    onNextUIConfig: (MovableAndResizeableComponent, BoundingRectState, BoundingRectState) -> Unit,
    val filter: List<DiagramStateHolders.UMLRef.ComposableRef.ClassRef.UMLClassRefFilter.Compartment>
) : MovableAndResizeableComponent(initBoundingRect, onNextUIConfig) {

    companion object : KLogging()

    lateinit var deleteSelfCommand: RemoveFromDiagramCommand

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler, helper: AlignmentHelper) {
        val tmmClassPath = remember(
            umlClass,
            projectTreeHandler,
            projectTreeHandler.metamodelRoot
        ) {
            projectTreeHandler.metamodelRoot.findModellingElementOrNull(umlClass)
        }
        remember(projectTreeHandler.singleSelectedItem.value, tmmClassPath) {
            selected = tmmClassPath?.let { projectTreeHandler.treeSelection.value.contains(it.target) } ?: false
        }

        Column(
            modifier = Modifier.mouseCombinedClickable(
                onClick = {
                    if (buttons.isPrimaryPressed) {
                        println("Clicked")
                        tmmClassPath?.target?.let {
                            projectTreeHandler.setTreeSelection(listOf(it))
                        }
                    }
                    if (buttons.isSecondaryPressed) {
                        showContextMenu.value = true
                    }
                },
                onDoubleClick = {
                    if (buttons.isPrimaryPressed) {
                        SharedCommands.forceOpenProperties?.invoke()
                        tmmClassPath?.target?.let {
                            projectTreeHandler.setTreeSelection(listOf(it))
                        }
                    }
                    if (buttons.isSecondaryPressed) {
                        showContextMenu.value = true
                    }
                }
            ),
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
                val attributesCompartmentFilter = filter.find { it.name == CompartmentEnum.ATTRIBUTES }
                umlClass.ownedAttributes
                    //.filter { it.aggregation != AggregationKind.NONE_LITERAL }
                    .let {
                        if (attributesCompartmentFilter != null) {
                            it.filter { attributesCompartmentFilter.elementsNames.contains(it.name) }
                        } else it
                    }
                    .let { filteredAttrs ->
                        if (filteredAttrs.isNotEmpty()) {
                            Text(text = "attributes")
                            Column(modifier = Modifier.fillMaxWidth()) {
                                filteredAttrs.forEach { it.displayProp(projectTreeHandler) }
                            }
                        }
                    }
            }
        }

    }

    override fun getMenuContributions(): List<MenuContribution> {
        return listOf(
            DemoMenuContributions.links(hasLink = false),
            MenuContribution.Contentful.MenuItem(
                displayName = "Delete from Diagram",
                command = deleteSelfCommand
            )
        )
    }

    override fun showsElement(element: Element?): Boolean {
        return umlClass == element
    }

    override fun showsElementFromAssoc(element: Element?, lastMemberEnd: Boolean): Boolean {
        val l = mutableListOf<EObject>()
        umlClass.eAllContents().forEachRemaining { l.add(it) }
        return kotlin.run {
            umlClass.associations.any { it.memberEnds.let { if (lastMemberEnd) it.last() else it.first() } == element }
        }
    }


    fun org.eclipse.uml2.uml.Class.appliedStereotypesString(): String =
        this.appliedStereotypes.toStereotypesString().takeUnless { it.equals("") } ?: "≪Block≫"

    fun List<Stereotype>.toStereotypesString(limit: Int = this.size): String {
        return if (this.isEmpty()) "" else this.subList(0, limit)
            .joinToString(prefix = "<<", postfix = ">>", separator = ",") { it.labelOrName() }
    }

    fun org.eclipse.uml2.uml.NamedElement.labelOrName(default: String = "No name"): String = label ?: name ?: default

    fun org.eclipse.uml2.uml.Property.typeNameString(asBrackets: Boolean = true): String {
        val t = this.type
        val s = when (t) {
            is PrimitiveTypeImpl -> {
                t.toString().split("#").lastOrNull()?.removeSuffix(")")
            }
            is EnumerationImpl -> t.name ?: t.label
            else -> "undefined"
        }
        return when {
            asBrackets -> "≪" + (s ?: "undefined") + "≫"
            else -> ": " + (s ?: "undefined")
        }
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
            val tmmProp = remember(
                this,
                projectTreeHandler.metamodelRoot
            ) { projectTreeHandler.metamodelRoot.findModellingElementOrNull(prop) }
            val selected = remember(this, tmmProp, projectTreeHandler.treeSelection.value) {
                println("modellingElementOrNull = ${tmmProp}")
                tmmProp?.let { projectTreeHandler.treeSelection.value.contains(it.target) } ?: false
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        tmmProp?.target?.let {
                            projectTreeHandler.setTreeSelection(listOf(it))
                        }
                    },
                    onDoubleClick = {
                        SharedCommands.forceOpenProperties?.invoke()
                        tmmProp?.target?.let {
                            projectTreeHandler.setTreeSelection(listOf(it))
                        }
                    }
                ).then(
                    if (selected) {
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
                val value: ValueSpecification? = prop.defaultValue
                if (value != null) {
                    val valueStr = when (value) {
                        is LiteralInteger -> value?.value?.toString()
                        is LiteralBoolean -> value?.booleanValue()?.toString()
                        is LiteralReal -> value?.realValue()?.toString()
                        is LiteralString -> value?.stringValue()
                        else -> {
                            null
                        }
                    }
                    val str =
                        prop.applicableStereotypes.toStereotypesString() + " $vis " + prop.labelOrName() + " " + prop.typeNameString(
                            false
                        ) + " = " + valueStr
                    Text(text = str, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    val str =
                        prop.applicableStereotypes.toStereotypesString() + " $vis " + prop.labelOrName() + " " + prop.typeNameString() + " [1]"
                    Text(text = str, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                // TODO Multiplicity
            }
        }
    }
}