package com.github.jan222ik.ui.uml

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.packFloats
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.CompoundCommand
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.model.command.commands.UpdateArrowPathCommand
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.feature.main.diagram.canvas.DNDCreation
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import org.eclipse.uml2.uml.*
import java.io.File

data class DiagramHolder(
    val name: String,
    val upwardsDiagramLink: String?,
    val diagramType: DiagramType,
    val location: String,
    val content: List<DiagramStateHolders.UMLRef>
) {
    fun toObservable(
        umlRoot: TMM.FS.UmlProjectFile,
        commandStackHandler: CommandStackHandler
    ): DiagramHolderObservable {
        return DiagramHolderObservable(
            initName = name,
            diagramType = diagramType,
            upwardsDiagramLink = upwardsDiagramLink,
            location = location,
            initContent = content,
            umlRoot = umlRoot,
            commandStackHandler = commandStackHandler
        )
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = DiagramStateHolders.UMLRef::class, name = "UMLRef"),
)
sealed class DiagramStateHolders {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = UMLRef.ClassRef::class, name = "UMLRef.Class"),
        JsonSubTypes.Type(value = UMLRef.ArrowRef::class, name = "UMLRef.Arrow"),
    )
    sealed class UMLRef() : DiagramStateHolders() {
        class ArrowRef(
            val sourceReferencedQualifierName: String,
            val targetReferencedQualifierName: String,
            val index: Int,
            val sourceAnchor: Anchor,
            val targetAnchor: Anchor
        ) : UMLRef()

        class ClassRef(
            val referencedQualifiedName: String,
            val shape: BoundingRectState,
            val filters: List<UMLClassRefFilter>
        ) : UMLRef() {
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
            @JsonSubTypes(
                JsonSubTypes.Type(
                    value = ClassRef.UMLClassRefFilter.Compartment::class,
                    name = "UMLClassRefFilter.Compartment"
                ),
            )
            sealed class UMLClassRefFilter {
                data class Compartment(val name: String, val elementsNames: List<String>) : UMLClassRefFilter()
            }
        }
    }

}

class DiagramHolderObservable(
    initName: String,
    val diagramType: DiagramType,
    val location: String,
    initContent: List<DiagramStateHolders.UMLRef>,
    umlRoot: TMM.FS.UmlProjectFile,
    commandStackHandler: CommandStackHandler,
    val upwardsDiagramLink: String?
) {
    val diagramName = ValidatedTextState(initial = initName, NonTransformer())

    val arrows: MutableState<List<Arrow>> = mutableStateOf(emptyList())
    val elements: MutableState<List<MovableAndResizeableComponent>> = mutableStateOf(emptyList())

    init {
        val (arrowRef, elementRef) = initContent.partition { it is DiagramStateHolders.UMLRef.ArrowRef }
        val tmmAsList = umlRoot.toList().filterIsInstance<TMM.ModelTree.Ecore>()
        val initElements = elementRef
            .map { umlRef ->
                umlRef as DiagramStateHolders.UMLRef.ClassRef to
                        tmmAsList
                            .find { it.element is NamedElement && it.element.qualifiedName == umlRef.referencedQualifiedName }
            }
            .mapNotNull {
                it.second
                val c = it.second?.element
                if (c != null && c is Class) {
                    UMLClassFactory.createInstance(
                        umlClass = c,
                        initBoundingRect = it.first.shape,
                        onMoveOrResize = { moveResizeCommand ->
                            DNDCreation.logger.debug { "Update Arrows after UMLClass movement" }
                            val updateArrowPathCommands = updateArrows(moveResizeCommand, c)
                            if (updateArrowPathCommands.isEmpty()) {
                                commandStackHandler.add(moveResizeCommand)
                            } else {
                                commandStackHandler.add(CompoundCommand(updateArrowPathCommands + moveResizeCommand))
                            }
                        },
                        deleteCommand = { toDelete ->
                            val command = object : RemoveFromDiagramCommand() {
                                override suspend fun execute(handler: JobHandler) {
                                    elements.value = elements.value - toDelete
                                }

                                override suspend fun undo() {
                                    elements.value = elements.value + toDelete
                                }
                            }
                            command
                        }
                    )
                } else null
            }
        elements.value = initElements
        println("elements.value = ${elements.value}")
        println("arrowRef = ${arrowRef}")

        val allDirectedRelationShip = tmmAsList.filterElementIsInstance<DirectedRelationship>()
        println("allDirectedRelationShip = ${allDirectedRelationShip}")
        arrowRef as List<DiagramStateHolders.UMLRef.ArrowRef>
        val initArrows = arrowRef
            .map { aRef ->
                allDirectedRelationShip
                    .find { dirRel ->
                        dirRel.sources
                            .filterIsInstance<NamedElement>()
                            .any { it.qualifiedName == aRef.sourceReferencedQualifierName }
                                && dirRel.targets
                            .filterIsInstance<NamedElement>()
                            .any {
                                it.qualifiedName == aRef.targetReferencedQualifierName
                            }
                    } to aRef
            }
            .mapNotNull {
                if (it.first == null) {
                    println("No element $it")
                    null
                } else it as Pair<DirectedRelationship, DiagramStateHolders.UMLRef.ArrowRef>
            }
            .mapNotNull { (rel, ref) ->
                val general = elements.value.firstOrNull { it.showsElement(rel.targets.first()) }
                val special = elements.value.firstOrNull { it.showsElement(rel.sources.first()) }
                if (special != null && general != null) {
                    val fourPointArrowOffsetPath = Arrow.fourPointArrowOffsetPath(
                        sourceAnchor = ref.sourceAnchor,
                        targetAnchor = ref.targetAnchor,
                        sourceBoundingShape = special.boundingShape,
                        targetBoundingShape = general.boundingShape
                    )
                    val arrow = Arrow(
                        initArrowType = when (rel) {
                            is Generalization -> ArrowType.GENERALIZATION
                            else -> ArrowType.ASSOCIATION_DIRECTED
                        },
                        initSourceAnchor = ref.sourceAnchor,
                        initTargetAnchor = ref.targetAnchor,
                        initOffsetPath = fourPointArrowOffsetPath,
                        data = rel,
                        initSourceBoundingShape = special.boundingShape,
                        initTargetBoundingShape = general.boundingShape
                    )
                    arrow
                } else null
            }
        arrows.value = initArrows
    }

    fun updateArrows(moveResizeCommand: MoveOrResizeCommand, data: Class): List<UpdateArrowPathCommand> {
        val filteredSources = arrows.value.filter { it.data.sources.contains(data) }
        val filteredTargets = arrows.value.filter { it.data.targets.contains(data) }
        DNDCreation.logger.debug { "sources: $filteredSources, tragets: $filteredTargets" }
        if (filteredSources.isEmpty() && filteredTargets.isEmpty()) {
            return emptyList()
        } else {
            val sourceOffsets = filteredSources.map {
                it to Arrow.fourPointArrowOffsetPath(
                    sourceAnchor = it.sourceAnchor.value,
                    targetAnchor = it.targetAnchor.value,
                    sourceBoundingShape = it.initSourceBoundingShape.updateFromState(moveResizeCommand.after),
                    targetBoundingShape = it.initTargetBoundingShape
                )
            }
            val targetOffsets = filteredTargets.map {
                it to Arrow.fourPointArrowOffsetPath(
                    sourceAnchor = it.sourceAnchor.value,
                    targetAnchor = it.targetAnchor.value,
                    sourceBoundingShape = it.initSourceBoundingShape,
                    targetBoundingShape = it.initTargetBoundingShape.updateFromState(moveResizeCommand.after)
                )
            }
            val commands = sourceOffsets.plus(targetOffsets).map {
                UpdateArrowPathCommand(
                    target = it.first,
                    before = it.first.offsetPath.value,
                    after = it.second
                )
            }
            return commands
        }
    }

}

private inline fun <reified T> List<TMM>.filterElementIsInstance(): List<T> {
    return this.filterIsInstance<TMM.ModelTree.Ecore>().map { it.element }.filterIsInstance<T>()
}

enum class AnchorSide {
    S, N, E, W
}

class Anchor(val side: AnchorSide, val fromTopLeftOffsetPercentage: Float) {
    fun toOffset(width: Float, height: Float): Offset {
        val o = Offset.Zero
        return when (side) {
            AnchorSide.S -> o.copy(x = width.times(fromTopLeftOffsetPercentage), y = height)
            AnchorSide.N -> o.copy(x = width.times(fromTopLeftOffsetPercentage))
            AnchorSide.E -> o.copy(y = height.times(fromTopLeftOffsetPercentage), x = width)
            AnchorSide.W -> o.copy(y = height.times(fromTopLeftOffsetPercentage))
        }
    }
}

fun main() {


    val diagram1 = DiagramHolder(
        name = "First diagram",
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 280f,
                    height = 300f,
                )
            ),
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClassWithProperties",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 800f),
                    width = 280f,
                    height = 300f,
                )
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                sourceReferencedQualifierName = "testuml::TestClassWithProperties",
                targetReferencedQualifierName = "testuml::TestClass",
                index = 0,
                sourceAnchor = Anchor(side = AnchorSide.N, fromTopLeftOffsetPercentage = 0.5f),
                targetAnchor = Anchor(side = AnchorSide.S, fromTopLeftOffsetPercentage = 0.5f)
            )
        ),
        upwardsDiagramLink = null
    )

    val diagram2 = DiagramHolder(
        name = "second diagram",
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 280f,
                    height = 200f,
                )
            ),
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClassWithProperties",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 800f),
                    width = 280f,
                    height = 200f,
                )
            ),
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::AnotherClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(700f, 500f),
                    width = 280f,
                    height = 200f,
                )
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
    diagramsLoader.writeToFile(listOf(diagram1, diagram2))
    diagramsLoader.loadFromFile().tap { println(it) }.tapInvalid { println(it.e) }
}