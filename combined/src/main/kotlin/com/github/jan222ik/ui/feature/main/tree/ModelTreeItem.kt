package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
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
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
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

    override val children: SnapshotStateList<TreeDisplayableItem> = emptyList<TreeDisplayableItem>().toMutableStateList()

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
                else -> {
                    logger.debug { "Item can not be converted to a element in the tree. ${tmmModelItem.javaClass}" }
                    null
                }
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
            logger.debug { "TODO: Secondary Action" }
            treeContextProvider.setContextFor(
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val find = state.layoutInfo.visibleItemsInfo.find { it.index == idx }
                        return IntOffset(
                            x = anchorBounds.width.times(0.75f).roundToInt(),
                            y = find?.let { it.offset.minus(it.size).plus(popupContentSize.height.div(2)) } ?: 0
                        )

                    }
                } to listOf(
                    MenuContribution.Contentful.MenuItem(
                        icon = null,
                        displayName = "Option 1",
                        command = null
                    ),
                    MenuContribution.Separator,
                )
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

        override val displayName: String
            get() = tmmTPackage.umlPackage.name
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

        override val displayName: String
            get() = tmmTClass.umlClass.name
    }

    class ImportItem(
        level: Int,
        val tmmTPackageImport: TMM.ModelTree.Ecore.TPackageImport
    ) : ModelTreeItem(
        level = level,
        tmmModelItem = tmmTPackageImport
    ) {
        override val displayName: String
            get() = tmmTPackageImport.packageImport.importingNamespace.name

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/PackageImport.gif"),
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
        override val displayName: String
            get() = tmmTProperty.property.name
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
        override val displayName: String
            get() = "TODO ValueSpecification"
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

        override val displayName: String
            get() = "<Generalization> ${tmmTGeneralisation.generalization.general.name}"
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

    override val displayName: String
        get() = diagram.initDiagram.name

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

    override val children: SnapshotStateList<TreeDisplayableItem> = emptyList<TreeDisplayableItem>().toMutableStateList()
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

