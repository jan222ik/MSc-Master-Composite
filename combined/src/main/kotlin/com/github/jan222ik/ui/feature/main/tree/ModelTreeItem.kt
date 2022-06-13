package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging
import org.eclipse.uml2.uml.ValueSpecification
import kotlin.math.roundToInt

@ExperimentalFoundationApi
sealed class ModelTreeItem(
    override val level: Int,
    val tmmModelItem: TMM.ModelTree.Ecore
) : TreeDisplayableItem(level = level) {

    override val icon: @Composable ((modifier: Modifier) -> Unit)?
        get() = null

    override val canExpand: Boolean
        get() = tmmModelItem.ownedElements.isNotEmpty() && tmmModelItem.ownedElements.any { it !is TMM.ModelTree.Ecore.TUNKNOWN }

    override val children: SnapshotStateList<TreeDisplayableItem> =
        emptyList<TreeDisplayableItem>().toMutableStateList()

    companion object : KLogging() {
        fun parseItem(
            level: Int,
            tmmModelItem: TMM.ModelTree.Ecore
        ): TreeDisplayableItem? {
            return when (tmmModelItem) {
                is ValueSpecification -> {
                    logger.debug { "ValueSpec" }
                    ValueItem(
                        level = level,
                        umlValue = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TClass -> {
                    logger.debug { "Class ${tmmModelItem.umlClass.qualifiedName}" }
                    ClassItem(
                        level = level,
                        tmmTClass = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TGeneralisation -> {
                    logger.debug { "Generalization" }
                    GeneralizationItem(
                        level = level,
                        tmmTGeneralisation = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TPackage -> {
                    logger.debug { "Package ${tmmModelItem.umlPackage.uri}" }
                    PackageItem(
                        level = level,
                        tmmTPackage = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TProperty -> {
                    logger.debug { "Property" }
                    PropertyItem(
                        level = level,
                        tmmTProperty = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TPackageImport -> {
                    logger.debug { "PackageImport" }
                    ImportItem(
                        level = level,
                        tmmTPackageImport = tmmModelItem
                    )
                }

                is TMM.ModelTree.Ecore.TAssociation -> AssociationItem(
                    level = level,
                    tmmTAssociation = tmmModelItem
                )

                is TMM.ModelTree.Ecore.TConnector -> ConnectorItem(
                    level = level,
                    tmmTConnector = tmmModelItem
                )

                is TMM.ModelTree.Ecore.TConstraint -> ConstraintItem(
                    level = level,
                    tmmTConstraint = tmmModelItem
                )

                is TMM.ModelTree.Ecore.TEnumeration -> EnumerationItem(
                    level = level,
                    tmmTEnumeration = tmmModelItem
                )

                else -> {
                    logger.debug { "Item can not be converted to a element in the tree. ${tmmModelItem.javaClass}" }
                    null
                }
            }
        }
    }

    @Composable
    override fun tmmChildObserver() {
        remember(tmmModelItem.ownedElements) {
            if (children.isNotEmpty()) {
                children.clear()
                children.addAll(tmmModelItem.ownedElements.mapNotNull { it.toViewTreeElement(level.inc()) })
            }
        }
    }

    fun getElement(): org.eclipse.uml2.uml.Element {
        return tmmModelItem.element
    }

    override fun getTMM(): TMM = tmmModelItem

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)? = null

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children.clear()
                } else {
                    children.clear()
                    children.addAll(tmmModelItem.ownedElements.mapNotNull { it.toViewTreeElement(level.inc()) })
                }
            }
        }

    override val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
        get() = action@{ state, idx, treeContextProvider ->
            treeContextProvider.setContextFor(
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val find = state.layoutInfo.visibleItemsInfo.find { it.key == idx }
                        return IntOffset(
                            x = anchorBounds.width.times(0.75f).roundToInt(),
                            y = find?.let { it.offset.minus(it.size).plus(popupContentSize.height.div(2)) } ?: 0
                        )

                    }
                } to contextMenuContributions()
            )
        }

    fun contextMenuContributions(): List<MenuContribution> {
        return listOf(
            DemoMenuContributions.diagramContributionsFor(this)
        )
    }

    class PackageItem(
        level: Int,
        val tmmTPackage: TMM.ModelTree.Ecore.TPackage
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTPackage
    ) {

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    if (tmmTPackage is TMM.ModelTree.Ecore.TModel) {
                        painterResource("drawables/uml_icons/Model.gif")
                    } else {
                        painterResource("drawables/uml_icons/Package.gif")
                    },
                    contentDescription = null,
                    modifier = it
                )
            }


        override val displayName: @Composable () -> String
            get() = @Composable { tmmTPackage.displayName }
    }

    class ClassItem(
        level: Int,
        val tmmTClass: TMM.ModelTree.Ecore.TClass
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTClass
    ) {
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Block.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }


        override val displayName: @Composable () -> String
            get() = @Composable { tmmTClass.displayName }
    }

    class ImportItem(
        level: Int,
        val tmmTPackageImport: TMM.ModelTree.Ecore.TPackageImport
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTPackageImport
    ) {
        override val displayName: @Composable () -> String
            get() = @Composable { tmmTPackageImport.packageImport.importingNamespace.name }

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/PackageImport.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

    }

    //is TMM.ModelTree.Ecore.TAssociation -> TODO()
    class AssociationItem(
        level: Int,
        val tmmTAssociation: TMM.ModelTree.Ecore.TAssociation
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTAssociation
    ) {

        override val displayName: @Composable () -> String
            get() = @Composable { tmmTAssociation.displayName }

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                // TODO check if composite
                Image(
                    painterResource("drawables/uml_icons/Association.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

    }

    //is TMM.ModelTree.Ecore.TConnector -> TODO()
    class ConnectorItem(
        level: Int,
        val tmmTConnector: TMM.ModelTree.Ecore.TConnector
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTConnector
    ) {

        override val displayName: @Composable () -> String
            get() = @Composable { tmmTConnector.displayName }

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/BindingConnector.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

    }

    //is TMM.ModelTree.Ecore.TConstraint -> TODO()
    class ConstraintItem(
        level: Int,
        val tmmTConstraint: TMM.ModelTree.Ecore.TConstraint
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTConstraint
    ) {

        override val displayName: @Composable () -> String
            get() = @Composable { tmmTConstraint.displayName }

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Constraint.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

    }

    //is TMM.ModelTree.Ecore.TEnumeration -> TODO()
    class EnumerationItem(
        level: Int,
        val tmmTEnumeration: TMM.ModelTree.Ecore.TEnumeration
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTEnumeration
    ) {

        override val displayName: @Composable () -> String
            get() = @Composable { tmmTEnumeration.displayName }

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Enumeration.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

    }

    class PropertyItem(
        level: Int,
        val tmmTProperty: TMM.ModelTree.Ecore.TProperty
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTProperty
    ) {

        override val displayName: @Composable () -> String
            get() = @Composable { tmmTProperty.displayName }
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Property.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }
    }

    class ValueItem(
        level: Int,
        val umlValue: org.eclipse.uml2.uml.ValueSpecification,
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = TODO()
    ) {
        override val displayName: @Composable () -> String
            get() = @Composable { "TODO ValueSpecification" }
    }

    class GeneralizationItem(
        level: Int,
        val tmmTGeneralisation: TMM.ModelTree.Ecore.TGeneralisation
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTGeneralisation
    ) {
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Generalization.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }

        override val displayName: @Composable () -> String
            get() = @Composable { "<Generalization> ${tmmTGeneralisation.generalization.general.name}" }
    }

}

@OptIn(ExperimentalFoundationApi::class)
class DiagramTreeItem(
    level: Int,
    val diagram: TMM.ModelTree.Diagram,
) : TreeDisplayableItem(
    level = level,
) {
    companion object : KLogging()

    override val displayName: @Composable () -> String
        get() = @Composable { diagram.observed.value.diagramName.tfv.text }

    override val canExpand: Boolean
        get() = false

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
        get() = null

    override val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
        get() = { listState, idx, action ->
            logger.debug { "TODO Context menu for ${this@DiagramTreeItem}" }
        }

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            EditorManager.moveToOrOpenDiagram(diagram, CommandStackHandler.INSTANCE)
        }

    override val children: SnapshotStateList<TreeDisplayableItem> =
        emptyList<TreeDisplayableItem>().toMutableStateList()

    override fun getTMM(): TMM = diagram

    override val icon: (@Composable (modifier: Modifier) -> Unit)
        get() = @Composable {
            Image(
                painter = diagram.initDiagram.diagramType.iconAsPainter(),
                contentDescription = null,
                modifier = it
            )
        }

}

