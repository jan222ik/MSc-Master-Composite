package com.github.jan222ik.ui.uml

import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.uml.DiagramStateHolders.UMLRef.ArrowRef.AssocRef.Aggregation
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

    private const val pcConfigModel = "PC_System::Configuration::PC_ConfigurationModel"
    val ModelLibraries = kotlin.run {
        val xCol0 = d64
        val xCol1 = d128.times(5)
        val yRow0 = d64
        val yRow1 = d128.times(3) + d64
        val yRow2 = d128.times(6) + d128
        DiagramHolder(
            name = "ModelLibraries",
            diagramType = DiagramType.BLOCK_DEFINITION,
            upwardsDiagramLink = pcConfigModel,
            location = "PC_System::Configuration::Model Libraries",
            content = trioFormation(
                top = yRow0,
                left = xCol0,
                general = "PC_System::Configuration::Model Libraries::HDisk",
                specification1 = "PC_System::Configuration::Model Libraries::MedStoreDisk",
                specification2 = "PC_System::Configuration::Model Libraries::MaxStoreDisk"
            ) + trioFormation(
                top = yRow0,
                left = xCol1,
                general = "PC_System::Configuration::Model Libraries::HDController",
                specification1 = "PC_System::Configuration::Model Libraries::MaxStoreC",
                specification2 = "PC_System::Configuration::Model Libraries::MedStoreC"
            ) + trioFormation(
                top = yRow1,
                left = xCol0,
                general = "PC_System::Configuration::Model Libraries::CPU",
                specification1 = "PC_System::Configuration::Model Libraries::CPUS",
                specification2 = "PC_System::Configuration::Model Libraries::CPUD"
            ) + trioFormation(
                top = yRow1,
                left = xCol1,
                general = "PC_System::Configuration::Model Libraries::OS",
                specification1 = "PC_System::Configuration::Model Libraries::OSAlpha",
                specification2 = "PC_System::Configuration::Model Libraries::OSBeta"
            ) + trioFormation(
                top = yRow2,
                left = xCol0,
                general = "PC_System::Configuration::Model Libraries::MB",
                specification1 = "PC_System::Configuration::Model Libraries::MBSilver",
                specification2 = "PC_System::Configuration::Model Libraries::MBDiamond"
            ) + kotlin.run {
                val spaceTween: Float = d128 + d64.times(2)
                val width: Float = d256 - d32
                val height: Float = d128
                val vSpace: Float = d64

                trioFormation(
                    top = yRow2,
                    left = xCol1,
                    general = "PC_System::Configuration::Model Libraries::Screen",
                    specification1 = "PC_System::Configuration::Model Libraries::ScreenA",
                    specification2 = "PC_System::Configuration::Model Libraries::ScreenC",
                    anchorOffsetsAtGeneral = packFloats(0.25f, 0.75f),
                    spaceTween = spaceTween,
                    width = width,
                    height = height,
                    vSpace = vSpace
                ) + listOf(
                    DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                        referencedQualifiedName = "PC_System::Configuration::Model Libraries::ScreenB",
                        shape = BoundingRectState(
                            topLeftPacked = packFloats(
                                xCol1 + width + spaceTween.minus(width).div(2),
                                yRow2 + height + vSpace
                            ),
                            width = width,
                            height = height
                        ),
                        filters = emptyList(),
                        link = null
                    ),
                    DiagramStateHolders.UMLRef.ArrowRef.GeneralRef(
                        memberEndName0 = "PC_System::Configuration::Model Libraries::ScreenB",
                        memberEndName1 = "PC_System::Configuration::Model Libraries::Screen",
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
            }
        )
    }


    fun trioFormation(
        top: Float,
        left: Float,
        general: String,
        specification1: String,
        specification2: String,
        anchorOffsetsAtGeneral: Long = packFloats(0.33f, 0.66f),
        spaceTween: Float = d64,
        width: Float = d256 - d32,
        height: Float = d128,
        vSpace: Float = d64
    ): List<DiagramStateHolders.UMLRef> {
        val lowerRowY = top + height + vSpace
        val topRowInsetX = left + width.div(2) + spaceTween.div(2)
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
                    topLeftPacked = packFloats(topRowInsetX + width.div(2) + spaceTween.div(2), lowerRowY),
                    width = width,
                    height = height
                ),
                filters = emptyList(),
                link = null
            ),
            DiagramStateHolders.UMLRef.ArrowRef.GeneralRef(
                memberEndName0 = specification1,
                memberEndName1 = general,
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
            DiagramStateHolders.UMLRef.ArrowRef.GeneralRef(
                memberEndName0 = specification2,
                memberEndName1 = general,
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

    val productArchitecture = kotlin.run {
        val topLevelY = d64
        val topLevelHeight = d128
        val secondLevelY = topLevelY + topLevelHeight + d64
        val secondLevelHeight = d128
        val thirdLevelY = secondLevelY + secondLevelHeight + d64
        val thirdLevelHeight = d128
        val thirdLevelWidth = d256 - d64
        val lowestLevelY = thirdLevelY + thirdLevelHeight + d64
        val lowestLevelHeight = d128
        var idx = 0
        fun nextThirdRowOffset(): Float {
            val offset = d64.times(idx) + thirdLevelWidth.times(idx)
            idx += 1
            return offset
        }

        var cpuX: Float
        var hdUnitX: Float
        val thirdLevel = kotlin.run {
            listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::InternetConn",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(nextThirdRowOffset(), thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
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
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::HDUnit",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(nextThirdRowOffset().also { hdUnitX = it }, thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
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
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "hdunit",
                    memberEndName1 = "hdisk",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "hdunit",
                    memberEndName1 = "hdcontroller",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::Application",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(nextThirdRowOffset(), thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
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
                    referencedQualifiedName = "PC_System::Configuration::Model Libraries::MB",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(nextThirdRowOffset().also { cpuX = it }, thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(
                            CompartmentEnum.ATTRIBUTES, emptyList()
                        )
                    ),
                    link = null
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "mb",
                    memberEndName1 = "cpu",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Model Libraries::Screen",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(nextThirdRowOffset(), thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
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
                        topLeftPacked = packFloats(nextThirdRowOffset(), thirdLevelY),
                        width = thirdLevelWidth,
                        height = thirdLevelHeight
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(
                            CompartmentEnum.ATTRIBUTES, emptyList()
                        )
                    ),
                    link = null
                )
            )
        }
        val lowestLevelWidth = thirdLevelWidth
        val lowestLevel = kotlin.run {
            listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Model Libraries::HDisk",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(hdUnitX.minus(thirdLevelWidth.div(2)).minus(d32), lowestLevelY),
                        width = lowestLevelWidth,
                        height = lowestLevelHeight
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
                        topLeftPacked = packFloats(hdUnitX.plus(thirdLevelWidth.div(2)).plus(d32), lowestLevelY),
                        width = lowestLevelWidth,
                        height = lowestLevelHeight
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
                        topLeftPacked = packFloats(cpuX, lowestLevelY),
                        width = lowestLevelWidth,
                        height = lowestLevelHeight
                    ),
                    filters = listOf(
                        UMLClassRefFilter.Compartment(
                            CompartmentEnum.ATTRIBUTES, emptyList()
                        )
                    ),
                    link = null
                )
            )
        }

        val topLevel = kotlin.run {
            listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::ConfigurationModel",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d256, topLevelY),
                        width = d256,
                        height = topLevelHeight
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
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "configurationmodel",
                    memberEndName1 = "pc",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                )
            )
        }
        val secondLevel = kotlin.run {
            listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                    referencedQualifiedName = "PC_System::Configuration::Product Architecture::PC",
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d256, secondLevelY),
                        width = d256,
                        height = secondLevelHeight
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
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "internetconn",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "hdunit",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "mb",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "screen",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "os",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "pc",
                    memberEndName1 = "application",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, 0.5f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                )
            )
        }
        DiagramHolder(
            name = "ProductArchitecture",
            upwardsDiagramLink = pcConfigModel,
            diagramType = DiagramType.BLOCK_DEFINITION,
            location = "PC_System::Configuration::Product Architecture",
            content = topLevel + secondLevel + thirdLevel + lowestLevel
        )
    }

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
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "input",
                    memberEndName1 = "configurationmodel",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.33f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                ),
                DiagramStateHolders.UMLRef.ArrowRef.AssocRef(
                    memberEndName0 = "output",
                    memberEndName1 = "configurationmodel",
                    index = 0,
                    sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.5f),
                    targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.66f),
                    memberEnd0Aggregation = Aggregation.COMPOSITE,
                    memberEnd1Aggregation = Aggregation.NONE
                )
            )
        )
    }

    val pcTotalPrice = kotlin.run {
        val block = "PC_System::Configuration::Product Architecture::PC"
        DiagramHolder(
            name = "PCtotalPrice",
            upwardsDiagramLink = null,
            diagramType = DiagramType.PARAMETRIC,
            location = block,
            content = listOf(
                DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedClassRef(
                    referencedQualifiedName = block,
                    shape = BoundingRectState(
                        topLeftPacked = packFloats(d32, d32),
                        width = d256.times(10),
                        height = d128.times(6)
                    ),
                    link = null,
                    nestedContent = listOf(
                        DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedPropertyRef(
                            referencedQualifiedName = "$block::price",
                            shape = BoundingRectState(
                                topLeftPacked = packFloats(0f, 0f),
                                width = d256,
                                height = d64
                            ),
                            link = null,
                            nestedContent = listOf(),
                            isPortSized = false
                        ),
                        DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedPropertyRef(
                            referencedQualifiedName = "$block::internetCon",
                            shape = BoundingRectState(
                                topLeftPacked = packFloats(0f, d64 + d32),
                                width = d256,
                                height = d64
                            ),
                            link = null,
                            nestedContent = listOf(
                                DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedPropertyRef(
                                    referencedQualifiedName = "$block::internetCon::price",
                                    shape = BoundingRectState(
                                        topLeftPacked = packFloats(0f, 0f),
                                        width = d256,
                                        height = d64
                                    ),
                                    link = null,
                                    nestedContent = listOf(

                                    ),
                                    isPortSized = true
                                )
                            ),
                            isPortSized = false
                        )
                    )
                )
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
                inputs,
                pcTotalPrice
            )
        )
        diagramsLoader.loadFromFile().tapInvalid { println(it.e) }
    }
}

