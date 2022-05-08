package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.uml.DiagramHolder
import mu.KLogging
import org.eclipse.emf.common.notify.Notifier
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Package
import java.io.InvalidClassException
import javax.annotation.meta.Exhaustive
import kotlin.math.roundToInt

@ExperimentalFoundationApi
sealed class ModelTreeItem(
    override val level: Int,
    override val displayName: String,
    override val canExpand: Boolean,
    val allDiagrams: List<DiagramHolder>
) : TreeDisplayableItem(level = level) {

    override val icon: @Composable ((modifier: Modifier) -> Unit)?
        get() = null

    companion object : KLogging() {
        fun parseItem(
            level: Int,
            element: Notifier,
            allDiagrams: List<DiagramHolder>
        ): TreeDisplayableItem? {
            return when (element) {
                is org.eclipse.uml2.uml.Package -> {
                    logger.debug { "Package ${element.uri}" }

                    PackageItem(
                        level = level,
                        displayName = "Package" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlPackage = element,
                        allDiagrams = allDiagrams
                    )
                }
                is org.eclipse.uml2.uml.Class -> {
                    logger.debug { "Class ${element.qualifiedName}" }
                    ClassItem(
                        level = level,
                        displayName = element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlClass = element,
                        allDiagrams = allDiagrams
                    )
                }
                is org.eclipse.uml2.uml.PackageImport -> {
                    logger.debug { "PackageImport" }
                    ImportItem(
                        level = level,
                        displayName = element.importingNamespace.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlImport = element,
                        allDiagrams = allDiagrams
                    )
                }
                is org.eclipse.uml2.uml.Property -> {
                    logger.debug { "Property" }
                    PropertyItem(
                        level = level,
                        displayName = element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlProperty = element,
                        allDiagrams = allDiagrams
                    )
                }
                is org.eclipse.uml2.uml.ValueSpecification -> {
                    logger.debug { "ValueSpec" }
                    ValueItem(
                        level = level,
                        displayName = "Val:" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlValue = element,
                        allDiagrams = allDiagrams
                    )
                }
                is org.eclipse.uml2.uml.Generalization -> {
                    logger.debug { "Generalization" }
                    GeneralizationItem(
                        level = level,
                        displayName = element.general.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlGeneralization = element,
                        allDiagrams = allDiagrams
                    )
                }
                else -> {
                    logger.debug { "Item can not be converted to a element in the tree. ${element.javaClass}" }
                    null
                }
            }
        }
    }

    abstract fun getElement(): org.eclipse.uml2.uml.Element

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)? = null

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children = emptyList()
                } else {
                    FileTree.eContentsToModelTreeItem(getElement(), this@ModelTreeItem)
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
        displayName: String,
        canExpand: Boolean,
        val umlPackage: Package,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams = allDiagrams
    ) {
        override fun getElement() = umlPackage

        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    if (umlPackage is Model) {
                        painterResource("drawables/uml_icons/Model.gif")
                    } else {
                        painterResource("drawables/uml_icons/Package.gif")
                    },
                    contentDescription = null,
                    modifier = it
                )
            }
    }

    class ClassItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlClass: org.eclipse.uml2.uml.Class,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams
    ) {
        override fun getElement() = umlClass
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Block.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }
    }

    class ImportItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlImport: org.eclipse.uml2.uml.PackageImport,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams
    ) {
        override fun getElement() = umlImport

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
        displayName: String,
        canExpand: Boolean,
        val umlProperty: org.eclipse.uml2.uml.Property,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams
    ) {
        override fun getElement() = umlProperty

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
        displayName: String,
        canExpand: Boolean,
        val umlValue: org.eclipse.uml2.uml.ValueSpecification,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams
    ) {
        override fun getElement() = umlValue

    }

    class GeneralizationItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlGeneralization: org.eclipse.uml2.uml.Generalization,
        allDiagrams: List<DiagramHolder>
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand,
        allDiagrams
    ) {
        override fun getElement() = umlGeneralization
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painterResource("drawables/uml_icons/Generalization.gif"),
                    contentDescription = null,
                    modifier = it
                )
            }
    }

    class Diagram(
        level: Int,
        val diagram: DiagramHolder
    ) : ModelTreeItem(
        level = level,
        displayName = diagram.name,
        canExpand = false,
        allDiagrams = emptyList()
    ) {
        override val onDoublePrimaryAction: MouseClickScope.() -> Unit
            get() = {
                EditorManager.moveToOrOpenDiagram(diagram)
            }
        override fun getElement(): Element { throw InvalidClassException("Diagram has no uml element") }
        override val icon: (@Composable (modifier: Modifier) -> Unit)
            get() = @Composable {
                Image(
                    painter = kotlin.run {
                        @Exhaustive
                        when (diagram.diagramType) {
                            DiagramType.PACKAGE -> painterResource("drawables/uml_icons/Diagram_SysML_Package.gif")
                            DiagramType.PARAMETRIC -> painterResource("drawables/uml_icons/Diagram_Parametric.png")
                            DiagramType.BLOCK_DEFINITION -> painterResource("drawables/uml_icons/Diagram_BlockDefinition.gif")
                        }
                    },
                    contentDescription = null,
                    modifier = it
                )

            }

    }
}

