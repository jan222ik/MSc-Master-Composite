package com.github.jan222ik.ui.uml

import androidx.compose.ui.util.packFloats
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import java.io.File

fun main() {
    val overview = DiagramHolder(
        name = "Overview",
        diagramType = DiagramType.PACKAGE,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ComposableRef.PackageRef(
                referencedQualifiedName = "testuml",
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 280f,
                    height = 100f,
                ),
                link = null
            )
        ),
        upwardsDiagramLink = null
    )

    val diagram1 = DiagramHolder(
        name = "First diagram",
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "testuml::TestClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 280f,
                    height = 300f,
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "testuml::TestClassWithProperties",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 800f),
                    width = 280f,
                    height = 300f,
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = "testuml::TestClassWithProperties",
                targetReferencedQualifierName = "testuml::TestClass",
                index = 0,
                sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.5f),
                targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.5f)
            )
        ),
        upwardsDiagramLink = "testuml::Overview"
    )

    val diagram2 = DiagramHolder(
        name = "second diagram",
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "testuml::TestClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 280f,
                    height = 200f,
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "testuml::TestClassWithProperties",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 800f),
                    width = 280f,
                    height = 200f,
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ComposableRef.ClassRef(
                referencedQualifiedName = "testuml::AnotherClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(700f, 500f),
                    width = 280f,
                    height = 200f,
                ),
                link = null
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = "testuml::TestClassWithProperties",
                targetReferencedQualifierName = "testuml::TestClass",
                index = 0,
                sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.5f),
                targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.5f)
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = "testuml::AnotherClass",
                targetReferencedQualifierName = "testuml::TestClass",
                index = 0,
                sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.25f),
                targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.25f)
            )
        ),
        upwardsDiagramLink = "testuml::First diagram"
    )


    val diagramsLoader =
        DiagramsLoader(File("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\appworkspace\\testuml.diagrams"))
    diagramsLoader.writeToFile(listOf(overview, diagram1, diagram2))
    diagramsLoader.loadFromFile().tap { println(it) }.tapInvalid { println(it.e) }
}