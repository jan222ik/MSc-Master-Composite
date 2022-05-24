package com.github.jan222ik.ui.uml

import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.uml.DiagramStateHolders.UMLRef.ComposableRef.ClassRef.UMLClassRefFilter
import java.io.File

object PcSystemDiagrams {

    val pWidth = 256f
    val pHeight = 128f
    val d32 = 32f
    val d64 = 64f
    val d128 = 128f
    val d256 = 256f

    val PC_ConfigurationModel = DiagramHolder(
        name = "PC_ConfigurationModel",
        diagramType = DiagramType.PACKAGE,
        location = "PC_System::Configuration",
        content = listOf(
            DiagramStateHolders.UMLRef.ComposableRef.PackageRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d128, d128),
                    width = pWidth,
                    height = pHeight,
                ),
                link = "PC_System::Configuration::Product Architecture::ProductArchitecture"
            ),
            DiagramStateHolders.UMLRef.ComposableRef.PackageRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d128.plus(pWidth).plus(d64), d128),
                    width = pWidth,
                    height = pHeight,
                ),
                link = "PC_System::Configuration::Model Libraries::ModelLibraries"
            ),
            DiagramStateHolders.UMLRef.ComposableRef.PackageRef(
                referencedQualifiedName = "PC_System::Configuration::Interconnections",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d128, d128.plus(pHeight).plus(d64)),
                    width = pWidth,
                    height = pHeight,
                ),
                link = "PC_System::Configuration::Interconnections::Interconnections"
            ),
            DiagramStateHolders.UMLRef.ComposableRef.PackageRef(
                referencedQualifiedName = "PC_System::Configuration::Inputs",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(
                        val1 = d128.plus(pWidth).plus(d64),
                        val2 = d128.plus(pHeight).plus(d64)
                    ),
                    width = pWidth,
                    height = pHeight,
                ),
                link = "PC_System::Configuration::Inputs::Inputs"
            )
        ),
        upwardsDiagramLink = null
    )

    val pcConfigModel = "PC_System::Configuration::PC_ConfigurationModel"
    val ModelLibraries = DiagramHolder(
        name = "ModelLibraries",
        diagramType = DiagramType.BLOCK_DEFINITION,
        upwardsDiagramLink = pcConfigModel,
        location = "PC_System::Configuration::Model Libraries",
        content = trioFormation(
            top = d64,
            left = d64,
            general = "PC_System::Configuration::Model Libraries::HDisk",
            specification1 = "PC_System::Configuration::Model Libraries::MedStoreDisk",
            specification2 = "PC_System::Configuration::Model Libraries::MaxStoreDisk"
        ) + trioFormation(
            top = d64,
            left = d128.times(4),
            general = "PC_System::Configuration::Model Libraries::HDController",
            specification1 = "PC_System::Configuration::Model Libraries::MaxStoreC",
            specification2 = "PC_System::Configuration::Model Libraries::MedStoreC"
        ) + trioFormation(
            top = d128.times(3),
            left = d64,
            general = "PC_System::Configuration::Model Libraries::CPU",
            specification1 = "PC_System::Configuration::Model Libraries::CPUS",
            specification2 = "PC_System::Configuration::Model Libraries::CPUD"
        ) + trioFormation(
            top = d128.times(3),
            left = d128.times(4),
            general = "PC_System::Configuration::Model Libraries::OS",
            specification1 = "PC_System::Configuration::Model Libraries::OSAlpha",
            specification2 = "PC_System::Configuration::Model Libraries::OSBeta"
        ) + trioFormation(
            top = d128.times(6),
            left = d64,
            general = "PC_System::Configuration::Model Libraries::MB",
            specification1 = "PC_System::Configuration::Model Libraries::MBSilver",
            specification2 = "PC_System::Configuration::Model Libraries::MBDiamond"
        ) + trioFormation(
            top = d128.times(6),
            left = d128.times(4),
            general = "PC_System::Configuration::Model Libraries::Screen",
            specification1 = "PC_System::Configuration::Model Libraries::ScreenA",
            specification2 = "PC_System::Configuration::Model Libraries::ScreenC",
            anchorOffsetsAtGeneral = packFloats(0.25f, 0.75f)
        ) + listOf(
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::ScreenB",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d128.times(5), d128.times(7)),
                    width = d128,
                    height = d64
                ),
                filters = emptyList(),
                link = null
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = "PC_System::Configuration::Model Libraries::ScreenB",
                targetReferencedQualifierName = "PC_System::Configuration::Model Libraries::Screen",
                index = 1,
                sourceAnchor = Anchor(
                    side = AnchorSide.N,
                    fromTopLeftOffsetPercentage = 0.5f,
                ),
                targetAnchor = Anchor(
                    side = AnchorSide.S,
                    fromTopLeftOffsetPercentage = 0.5f
                )
            )
        )
    )

    fun trioFormation(
        top: Float,
        left: Float,
        general: String,
        specification1: String,
        specification2: String,
        anchorOffsetsAtGeneral: Long = packFloats(0.33f, 0.66f)
    ): List<DiagramStateHolders.UMLRef> {
        val width = d256
        val height = d128
        val vSpace = d64
        val lowerRowY = top + height + vSpace
        val topRowInsetX = left + width.div(2) + d32
        val targetFirst = unpackFloat1(anchorOffsetsAtGeneral)
        val targetSecond = unpackFloat2(anchorOffsetsAtGeneral)
        return listOf(
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = general,
                shape = BoundingRectState(
                    topLeftPacked = packFloats(topRowInsetX, top),
                    width = width,
                    height = height
                ),
                filters = emptyList(),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = specification1,
                shape = BoundingRectState(
                    topLeftPacked = packFloats(left, lowerRowY),
                    width = width,
                    height = height
                ),
                filters = emptyList(),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = specification2,
                shape = BoundingRectState(
                    topLeftPacked = packFloats(topRowInsetX + width, lowerRowY),
                    width = width,
                    height = height
                ),
                filters = emptyList(),
                link = null
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = specification1,
                targetReferencedQualifierName = general,
                index = 0,
                sourceAnchor = Anchor(
                    side = AnchorSide.N,
                    fromTopLeftOffsetPercentage = 0.5f
                ),
                targetAnchor = Anchor(
                    side = AnchorSide.S,
                    fromTopLeftOffsetPercentage = targetFirst
                )
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = specification2,
                targetReferencedQualifierName = general,
                index = 0,
                sourceAnchor = Anchor(
                    side = AnchorSide.N,
                    fromTopLeftOffsetPercentage = 0.5f
                ),
                targetAnchor = Anchor(
                    side = AnchorSide.S,
                    fromTopLeftOffsetPercentage = targetSecond
                )
            )
        )
    }

    val productArchitecture = DiagramHolder(
        name = "ProductArchitecture",
        upwardsDiagramLink = pcConfigModel,
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "PC_System::Configuration::Product Architecture",
        content = listOf(
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture::Configuration Model",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, listOf(
                            "maxPrice",
                            "usage",
                            "efficiency"
                        )
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture::PC",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, listOf(
                            "price"
                        )
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture::InternetConn",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, listOf(
                            "price",
                            "auxiliary"
                        )
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture::Application",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, listOf(
                            "hdcapacity",
                            "price"
                        )
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Product Architecture::HDUnit",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, listOf(
                            "price"
                        )
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::MB",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::Screen",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::OS",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::HDisk",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::HDController",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "PC_System::Configuration::Model Libraries::CPU",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(d256, d64),
                    width = d256,
                    height = d128
                ),
                filters = listOf(
                    UMLClassRefFilter.Compartment(
                        CompartmentEnum.ATTRIBUTES, emptyList()
                    )
                ),
                link = null
            ),
        )
    )

    val interconnectionsPackage = DiagramHolder(
        name = "Interconnections Overview",
        upwardsDiagramLink = pcConfigModel,
        diagramType = DiagramType.PACKAGE,
        location = "PC_System::Configuration::Interconnections",
        content = listOf()
    )

    val interconnections = DiagramHolder(
        name = "Interconnections",
        upwardsDiagramLink = pcConfigModel,
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "PC_System::Configuration::Interconnections",
        content = listOf()
    )

    val inputs = kotlin.run {
        DiagramHolder(
            name = "Inputs",
            upwardsDiagramLink = pcConfigModel,
            diagramType = DiagramType.BLOCK_DEFINITION,
            location = "PC_System::Configuration::Inputs",
            content = listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::ConfigurationModel",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d256, d64),
                        width = d256,
                        height = d128
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(CompartmentEnum.ATTRIBUTES, emptyList())
                    ),
                    link = null
                ),
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Inputs::Input",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d64, d64 + d256),
                        width = d256,
                        height = d128
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(
                            CompartmentEnum.ATTRIBUTES, listOf(
                                "efficiency",
                                "maxPrice",
                                "usage"
                            )
                        )
                    ),
                    link = null
                ),
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Inputs::Output",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d64 + d256 + d128, d64 + d256),
                        width = d256,
                        height = d128
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(
                            CompartmentEnum.ATTRIBUTES, listOf(
                                "Price"
                            )
                        )
                    ),
                    link = null
                ),
            )
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val diagramsLoader =
            DiagramsLoader(File("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\appworkspace\\testuml.diagrams"))
        diagramsLoader.writeToFile(
            listOf(
                PC_ConfigurationModel,
                ModelLibraries,
                productArchitecture,
                interconnectionsPackage,
                interconnections,
                inputs
            )
        )
        diagramsLoader.loadFromFile().tapInvalid { println(it.e) }
    }
}
