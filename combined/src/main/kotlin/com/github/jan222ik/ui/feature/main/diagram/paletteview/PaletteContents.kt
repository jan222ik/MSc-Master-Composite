package com.github.jan222ik.ui.feature.main.diagram.paletteview

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import com.github.jan222ik.ui.components.menu.DrawableIcon
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType

object PaletteContents {

    val categoryGeneralAnnotations = PaletteCategory(
        name = "General Annotations & Structure",
        columns = listOf(
            listOf(
                PaletteOption(name = "Constraint", DrawableIcon.Constraint),
                PaletteOption(name = "Abstraction", DrawableIcon.Abstraction),
                PaletteOption(name = "Comment", DrawableIcon.Comment),
                PaletteOption(name = "Context Link", DrawableIcon.ContextLink),
                PaletteOption(name = "Dependency", DrawableIcon.Dependency),
                PaletteOption(name = "ElementGroup", DrawableIcon.ElementGroup),
                PaletteOption(name = "Link", DrawableIcon.Link),
                PaletteOption(name = "Problem", DrawableIcon.Problem),
                PaletteOption(name = "Rationale", DrawableIcon.Rationale),
                PaletteOption(name = "Realization", DrawableIcon.Realization),
                PaletteOption(name = "Refine", DrawableIcon.Refine),
            ),
            listOf(
                PaletteOption(name = "Model", DrawableIcon.Model),
                PaletteOption(name = "Package", DrawableIcon.Package),
                PaletteOption(name = "PackageImport", DrawableIcon.PackageImport),
                PaletteOption(name = "PrivatePackageImport"),
                PaletteOption(name = "Containment Link")
            )
        )
    )

    val categoryViewport = PaletteCategory(
        name = "Viewport",
        columns = listOf(
            listOf(
                PaletteOption(name = "Conform"),
                PaletteOption(name = "Expose"),
                PaletteOption(name = "Stakeholder"),
                PaletteOption(name = "View"),
                PaletteOption(name = "Viewport"),
            )
        )
    )

    fun categoryBlock(isInternal: Boolean) = PaletteCategory(
        name = "Blocks",
        columns =
            if (!isInternal) listOf(listOf(
                PaletteOption(name = "Block", DrawableIcon.Block),
                PaletteOption(name = "AbstractDefinition"),
                PaletteOption(name = "Actor"),
                PaletteOption(name = "BoundReference"),
                PaletteOption(name = "ConnectorProperty"),
                PaletteOption(name = "Containment Link"),
                PaletteOption(name = "EndPathMultiplicity"),
                PaletteOption(name = "Enumeration", DrawableIcon.Enumeration),
                PaletteOption(name = "Instance Specialization"),
                PaletteOption(name = "ParticipantProperty"),
                PaletteOption(name = "Stereotype"),
                PaletteOption(name = "ValueType")),
            listOf(
                PaletteOption(name = "Generalization", DrawableIcon.Generalization),
                PaletteOption(name = "Association", DrawableIcon.Association),
                PaletteOption(name = "Association (Directed)", DrawableIcon.AssociationDirected),
                PaletteOption(name = "Composite Association (Directed)", DrawableIcon.AssociationComposite),
                PaletteOption(name = "Shared Association (Directed)", DrawableIcon.AssociationShared),
                PaletteOption(name = "Part Association", DrawableIcon.Association),
                PaletteOption(name = "Dependency", DrawableIcon.Dependency),
            )) else listOf(listOf(
                PaletteOption(name = "ActorPart"),
                PaletteOption(name = "Bounding Connector"),
                PaletteOption(name = "BoundReference"),
                PaletteOption(name = "Connector"),
                PaletteOption(name = "Dependency", DrawableIcon.Dependency),
                PaletteOption(name = "Part"),
                PaletteOption(name = "PropertySpecificType"),
                PaletteOption(name = "Reference"),
            ))

    )

    fun categoryPortsAndFlows(isInternal: Boolean) = PaletteCategory(
        name = "Ports and Flows",
        columns = listOf(kotlin.run {
            if (isInternal)
                listOf(
                    PaletteOption(name = "FlowPort"),
                    PaletteOption(name = "FlowProperty"),
                    PaletteOption(name = "Full Port"),
                    PaletteOption(name = "Interface"),
                    PaletteOption(name = "ConjugatedInterfaceBlock"),
                    PaletteOption(name = "InterfaceBlock"),
                    PaletteOption(name = "ItemFlow"),
                    PaletteOption(name = "Port"),
                    PaletteOption(name = "ProxyPort")
                ) else {
                listOf(
                    PaletteOption(name = "FlowPort"),
                    PaletteOption(name = "Full Port"),
                    PaletteOption(name = "ItemFlow"),
                    PaletteOption(name = "Port"),
                    PaletteOption(name = "ProxyPort")
                )
            }
        })
    )

    fun categoryConstraints(isInternal: Boolean) = PaletteCategory(
        name = "Constraints",
        columns = listOf(
            if (isInternal) listOf(
                PaletteOption(name = "Constraint", DrawableIcon.Constraint),
                PaletteOption(name = "ConstraintBlock", DrawableIcon.ConstraintBlock),
                PaletteOption(name = "Parameter", DrawableIcon.Property)
            ) else listOf(
                PaletteOption(name = "Constraint", DrawableIcon.Constraint)
            )
        )
    )

    val packageDiagramCategories = listOf(
        categoryGeneralAnnotations,
        categoryViewport
    )

    val blockDiagramCategories = listOf(
        categoryGeneralAnnotations,
        categoryViewport,
        categoryBlock(isInternal = false),
        categoryPortsAndFlows(isInternal = false),
        categoryConstraints(isInternal = false)
    )

    val internalBlockDiagramCategories = listOf(
        categoryGeneralAnnotations,
        categoryBlock(isInternal = true),
        categoryPortsAndFlows(isInternal = true),
        categoryConstraints(isInternal = true)
    )

    fun getCategoriesForDiagramType(diagramType: DiagramType): List<PaletteCategory> {
        return when (diagramType) {
            DiagramType.PACKAGE -> packageDiagramCategories
            DiagramType.PARAMETRIC -> internalBlockDiagramCategories
            DiagramType.BLOCK_DEFINITION -> blockDiagramCategories
        }
    }
}

data class PaletteCategory(
    val name: String,
    val columns: List<List<PaletteOption>>
)

@OptIn(ExperimentalComposeUiApi::class)
data class PaletteOption(
    val name: String,
    val icon: DrawableIcon? = null
)