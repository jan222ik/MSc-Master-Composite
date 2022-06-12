package com.github.jan222ik.ui.feature.main.diagram.paletteview

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType

object PaletteContents {

    val categoryGeneralAnnotations = PaletteCategory(
        name = "General Annntations",
        options = listOf(
            PaletteOption(name = "Abstraction", resourcePath = "drawables/uml_icons/Abstraction.gif"),
            PaletteOption(name = "Comment"),
            PaletteOption(name = "Constraint"),
            PaletteOption(name = "Context Link"),
            PaletteOption(name = "Dependency"),
            PaletteOption(name = "ElementGroup"),
            PaletteOption(name = "Link"),
            PaletteOption(name = "Problem"),
            PaletteOption(name = "Rationale"),
            PaletteOption(name = "Realization"),
            PaletteOption(name = "Refine"),
        )
    )

    val categoryGeneralStructure = PaletteCategory(
        name = "General Structure",
        options = listOf(
            PaletteOption(name = "Containment Link"),
            PaletteOption(name = "Model"),
            PaletteOption(name = "Package"),
            PaletteOption(name = "PackageImport"),
            PaletteOption(name = "PrivatePackageImport"),
        )
    )

    val categoryViewport = PaletteCategory(
        name = "Viewport",
        options = listOf(
            PaletteOption(name = "Conform"),
            PaletteOption(name = "Expose"),
            PaletteOption(name = "Stakeholder"),
            PaletteOption(name = "View"),
            PaletteOption(name = "Viewport"),
        )
    )

    fun categoryBlock(isInternal: Boolean) = PaletteCategory(
        name = "Blocks",
        options = if (!isInternal) listOf(
            PaletteOption(name = "AbstractDefinition"),
            PaletteOption(name = "Actor"),
            PaletteOption(name = "Association (Directed)"),
            PaletteOption(name = "Block"),
            PaletteOption(name = "BoundReference"),
            PaletteOption(name = "ConnectorProperty"),
            PaletteOption(name = "Containment Link"),
            PaletteOption(name = "Dependency"),
            PaletteOption(name = "EndPathMultiplicity"),
            PaletteOption(name = "Enumeration"),
            PaletteOption(name = "Generalization"),
            PaletteOption(name = "Instance Specialization"),
            PaletteOption(name = "ParticipantProperty"),
            PaletteOption(name = "Stereotype"),
            PaletteOption(name = "ValueType"),
        ) else listOf(
            PaletteOption(name = "ActorPart"),
            PaletteOption(name = "Bounding Connector"),
            PaletteOption(name = "BoundReference"),
            PaletteOption(name = "Connector"),
            PaletteOption(name = "Dependency"),
            PaletteOption(name = "Part"),
            PaletteOption(name = "PropertySpecificType"),
            PaletteOption(name = "Reference"),
        )
    )

    fun categoryPortsAndFlows(isInternal: Boolean) = PaletteCategory(
        name = "Ports and Flows",
        options = kotlin.run {
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
        }
    )

    fun categoryConstraints(isInternal: Boolean) = PaletteCategory(
        name = "Constraints",
        options = if (isInternal) listOf(
            PaletteOption(name = "Constraint"),
            PaletteOption(name = "ConstraintBlock"),
            PaletteOption(name = "Parameter")
        ) else listOf(
            PaletteOption(name = "Constraint")
        )
    )

    val packageDiagramCategories = listOf(
        categoryGeneralAnnotations,
        categoryGeneralStructure,
        categoryViewport
    )

    val blockDiagramCategories = listOf(
        categoryGeneralAnnotations,
        categoryGeneralStructure,
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
    val options: List<PaletteOption>
)

@OptIn(ExperimentalComposeUiApi::class)
data class PaletteOption(
    val name: String,
    val resourcePath: String? = null
) {
    val imageBitmapNotRemembered: ImageBitmap?
        get() = resourcePath?.let { useResource(resourcePath, ::loadImageBitmap) }
}