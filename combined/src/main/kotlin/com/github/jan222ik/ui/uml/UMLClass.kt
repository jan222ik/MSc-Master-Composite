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
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.NotImplementedCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.adjusted.helper.AlignmentHelper
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.components.menu.DrawableIcon
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.feature.main.tree.FileTree
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
                        DemoMenuContributions.arrowFrom.value?.let { data ->
                            val other = data.start as UMLClass
                            val thisY = this@UMLClass.boundingShape.topLeft.value.y


                            val (initSourceAnchor, initTargetAnchor) = Arrow.findIdealAnchors(
                                other.boundingShape,
                                this@UMLClass.boundingShape
                            )


                            val arrowLowerYShape =
                                other.boundingShape.takeUnless { it.topLeft.value.y > thisY } ?: boundingShape
                            val arrowHigherYShape =
                                other.boundingShape.takeUnless { it.topLeft.value.y < thisY } ?: boundingShape

                            val tmmClass =
                                projectTreeHandler.metamodelRoot.findModellingElementOrNull(umlClass)?.target as TMM.ModelTree.Ecore.TClass
                            val otherTMMClass =
                                projectTreeHandler.metamodelRoot.findModellingElementOrNull(other.umlClass)?.target as TMM.ModelTree.Ecore.TClass
                            val relation = when (data.type) {
                                ArrowType.GENERALIZATION -> otherTMMClass.createGeneralization(umlClass)
                                ArrowType.ASSOCIATION_DIRECTED -> otherTMMClass.createAssociation(
                                    end1isNavigable = false,
                                    end1Aggregation = data.member0ArrowTypeOverride?.toKind()
                                        ?: AggregationKind.NONE_LITERAL,
                                    end1Name = otherTMMClass.name.tfv.text,
                                    end1Lower = 0,
                                    end1Upper = -1,
                                    end1Type = tmmClass.umlClass,
                                    end2isNavigable = false,
                                    end2Aggregation = data.member1ArrowTypeOverride?.toKind()
                                        ?: AggregationKind.NONE_LITERAL,
                                    end2Name = tmmClass.name.tfv.text,
                                    end2Lower = 1,
                                    end2Upper = 1
                                )
                            }
                            val fourPointArrowOffsetPath = Arrow.fourPointArrowOffsetPath(
                                sourceAnchor = initSourceAnchor,
                                targetAnchor = initTargetAnchor,
                                sourceBoundingShape = other.boundingShape,
                                targetBoundingShape = boundingShape
                            )
                            val arrow = Arrow(
                                initArrowType = data.type,
                                initSourceAnchor = initSourceAnchor,
                                initTargetAnchor = initTargetAnchor,
                                initOffsetPath = fourPointArrowOffsetPath.first,
                                initBoundingShape = fourPointArrowOffsetPath.second,
                                data = relation.element as Relationship,
                                initSourceBoundingShape = other.boundingShape,
                                initTargetBoundingShape = boundingShape,
                                member0ArrowTypeOverride = data.member1ArrowTypeOverride,
                                member1ArrowTypeOverride = data.member0ArrowTypeOverride
                            )
                            val addToDiagram = object : AddToDiagramCommand() {
                                override suspend fun execute(handler: JobHandler) {
                                    EditorManager.activeEditorTab.value?.addArrow(arrow)
                                }

                                override suspend fun undo() {
                                    EditorManager.activeEditorTab.value?.removeArrow(arrow)
                                }

                            }
                            CommandStackHandler.INSTANCE.add(addToDiagram)
                            DemoMenuContributions.arrowFrom.value = null
                            DemoMenuContributions.paletteSelection.value = null
                            SharedCommands.forceOpenProperties?.invoke()
                            return@mouseCombinedClickable
                        }
                        if (DemoMenuContributions.paletteSelection.value != null) {
                            val arrowData = when (DemoMenuContributions.paletteSelection.value) {
                                "Generalization" -> DemoMenuContributions.ArrowData(
                                    start = this@UMLClass,
                                    type = ArrowType.GENERALIZATION,
                                    member0ArrowTypeOverride = null,
                                    member1ArrowTypeOverride = null
                                )
                                "Composite Association (Directed)" -> DemoMenuContributions.ArrowData(
                                    start = this@UMLClass,
                                    type = ArrowType.ASSOCIATION_DIRECTED,
                                    member0ArrowTypeOverride = DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.COMPOSITE,
                                    member1ArrowTypeOverride = DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.NONE
                                )
                                else -> {
                                    logger.warn { "Missing Type of palette click on UMLClass" }
                                    null
                                }
                            }
                            arrowData?.let {
                                DemoMenuContributions.arrowFrom.value = arrowData
                            }
                        }
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
                Text(text = (tmmClassPath?.target as TMM.ModelTree.Ecore.TClass?)?.labelOrName() ?: "")
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
            DemoMenuContributions.addProperty(
                tmmClass = FileTree.treeHandler.value?.metamodelRoot?.findModellingElementOrNull(umlClass)?.target as TMM.ModelTree.Ecore.TClass,
                onDismiss = { showContextMenu.value = false }
            ),
            MenuContribution.Contentful.NestedMenuItem(
                icon = null,
                displayName = "Arrow to ...",
                nestedItems = listOf(
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaPainterConstruction(painter = { painterResource("drawables/uml_icons/Generalization.gif") }),
                        displayName = "Generalization",
                        command = object : ICommand {
                            override fun isActive(): Boolean = EditorManager.allowEdit.value

                            override suspend fun execute(handler: JobHandler) {
                                DemoMenuContributions.arrowFrom.value = DemoMenuContributions.ArrowData(
                                    start = this@UMLClass,
                                    type = ArrowType.GENERALIZATION
                                )
                                showContextMenu.value = false
                            }

                            override fun canUndo(): Boolean = EditorManager.allowEdit.value

                            override suspend fun undo() {
                                TODO("Not yet implemented")
                            }

                            override fun pushToStack(): Boolean = false
                        }
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaPainterConstruction(painter = { painterResource("drawables/uml_icons/Association.gif") }),
                        displayName = "Association",
                        command = NotImplementedCommand("Create Association")
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaPainterConstruction(painter = { painterResource("drawables/uml_icons/Association_composite_directed.gif") }),
                        displayName = "Directed Association",
                        command = NotImplementedCommand("Create Directed Association")
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaPainterConstruction(painter = { painterResource("drawables/uml_icons/Association_composite_directed.gif") }),
                        displayName = "Composite Association",
                        command = object : ICommand {
                            override fun isActive(): Boolean = EditorManager.allowEdit.value

                            override suspend fun execute(handler: JobHandler) {
                                DemoMenuContributions.arrowFrom.value = DemoMenuContributions.ArrowData(
                                    start = this@UMLClass,
                                    type = ArrowType.ASSOCIATION_DIRECTED,
                                    member0ArrowTypeOverride = DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.COMPOSITE,
                                    member1ArrowTypeOverride = DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation.NONE
                                )
                                showContextMenu.value = false
                            }

                            override fun canUndo(): Boolean = EditorManager.allowEdit.value

                            override suspend fun undo() {
                                TODO("Not yet implemented")
                            }

                            override fun pushToStack(): Boolean = false
                        }
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaPainterConstruction(painter = { painterResource("drawables/uml_icons/Association_shared_directed.gif") }),
                        displayName = "Shared Association",
                        command = NotImplementedCommand("Create Shared Association")
                    )
                )
            ),
            MenuContribution.Separator,
            DemoMenuContributions.links(hasLink = false),
            MenuContribution.Separator,
            MenuContribution.Contentful.MenuItem(
                displayName = "Delete from Diagram",
                command = deleteSelfCommand
            ),
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

    @Composable
    fun TMM.ModelTree.Ecore.TProperty.labelOrName(): String {
        return name.tfv.text
    }

    @Composable
    fun TMM.ModelTree.Ecore.TClass.labelOrName(): String {
        return name.tfv.text
    }

    @Composable
    fun TMM.ModelTree.Ecore.TProperty.typeNameString(asBrackets: Boolean = true): String {
        val t = this.type
        return when {
            asBrackets -> "≪" + t.tfv.text + "≫"
            else -> ": " + t.tfv.text
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
                val tProperty = tmmProp?.target as TMM.ModelTree.Ecore.TProperty?
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
                        prop.applicableStereotypes.toStereotypesString() + " $vis " + tProperty?.labelOrName() + " " + tProperty?.typeNameString(
                            false
                        ) + " = " + valueStr
                    Text(text = str, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    val str =
                        prop.applicableStereotypes.toStereotypesString() + " $vis " + tProperty?.labelOrName() + " " + tProperty?.typeNameString() + " [1]"
                    Text(text = str, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                // TODO Multiplicity
            }
        }
    }
}