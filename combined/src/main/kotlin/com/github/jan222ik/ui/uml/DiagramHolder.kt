package com.github.jan222ik.ui.uml

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
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
import mu.KLogging
import org.eclipse.uml2.uml.*

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
        JsonSubTypes.Type(value = UMLRef.ComposableRef::class, name = "UMLRef.ComposableRef"),
        JsonSubTypes.Type(value = UMLRef.ArrowRef::class, name = "UMLRef.Arrow"),
    )
    sealed class UMLRef : DiagramStateHolders() {

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
        @JsonSubTypes(
            JsonSubTypes.Type(value = UMLRef.ArrowRef.GeneralRef::class, name = "UMLRef.ArrowRef.General"),
            JsonSubTypes.Type(value = UMLRef.ArrowRef.AssocRef::class, name = "UMLRef.ArrowRef.Assoc"),
            JsonSubTypes.Type(value = UMLRef.ArrowRef.ConnectorRef::class, name = "UMLRef.ArrowRef.Connector")
        )
        sealed class ArrowRef : UMLRef() {
            abstract val memberEndName0: String
            abstract val memberEndName1: String
            abstract val index: Int
            abstract val sourceAnchor: Anchor
            abstract val targetAnchor: Anchor

            class GeneralRef(
                override val memberEndName0: String,
                override val memberEndName1: String,
                override val index: Int,
                override val sourceAnchor: Anchor,
                override val targetAnchor: Anchor,
            ) : ArrowRef()

            class AssocRef(
                override val memberEndName0: String,
                override val memberEndName1: String,
                override val index: Int,
                override val sourceAnchor: Anchor,
                override val targetAnchor: Anchor,
                val memberEnd0Aggregation: Aggregation,
                val memberEnd1Aggregation: Aggregation,
            ) : ArrowRef() {
                enum class Aggregation {
                    COMPOSITE, SHARED, NONE;

                    fun asKindInt(): Int {
                        return when (this) {
                            COMPOSITE -> AggregationKind.COMPOSITE
                            SHARED -> AggregationKind.SHARED
                            NONE -> AggregationKind.NONE
                        }
                    }

                    fun toKind(): AggregationKind {
                        return when (this) {
                            COMPOSITE -> AggregationKind.COMPOSITE_LITERAL
                            SHARED -> AggregationKind.SHARED_LITERAL
                            NONE -> AggregationKind.NONE_LITERAL
                        }
                    }
                }
            }

            class ConnectorRef(
                override val memberEndName0: String,
                override val memberEndName1: String,
                override val index: Int,
                override val sourceAnchor: Anchor,
                override val targetAnchor: Anchor
            ) : ArrowRef()
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
        @JsonSubTypes(
            JsonSubTypes.Type(value = UMLRef.ComposableRef.ClassRef::class, name = "UMLRef.ComposableRef.ClassRef"),
            JsonSubTypes.Type(value = UMLRef.ComposableRef.PackageRef::class, name = "UMLRef.ComposableRef.PackageRef"),
            JsonSubTypes.Type(
                value = UMLRef.ComposableRef.ParametricNestedClassRef::class,
                name = "UMLRef.ComposableRef.ParametricNestedClassRef"
            ),
            JsonSubTypes.Type(
                value = UMLRef.ComposableRef.ParametricNestedPropertyRef::class,
                name = "UMLRef.ComposableRef.ParametricNestedPropertyRef"
            ),
        )
        sealed class ComposableRef : UMLRef() {
            abstract val referencedQualifiedName: String
            abstract val shape: BoundingRectState
            abstract val link: String?

            class ClassRef(
                override val referencedQualifiedName: String,
                override val shape: BoundingRectState,
                override val link: String?,
                val filters: List<UMLClassRefFilter.Compartment>
            ) : UMLRef.ComposableRef() {
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
                @JsonSubTypes(
                    JsonSubTypes.Type(
                        value = ClassRef.UMLClassRefFilter.Compartment::class,
                        name = "UMLClassRefFilter.Compartment"
                    ),
                )
                sealed class UMLClassRefFilter {
                    data class Compartment(val name: CompartmentEnum, val elementsNames: List<String>) :
                        UMLClassRefFilter()
                }
            }

            class ParametricNestedClassRef(
                override val referencedQualifiedName: String,
                override val shape: BoundingRectState,
                override val link: String?,
                val nestedContent: List<UMLRef>
            ) : UMLRef.ComposableRef() {
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
                @JsonSubTypes(
                    JsonSubTypes.Type(
                        value = ParametricNestedClassRef.UMLClassRefFilter.Compartment::class,
                        name = "UMLClassRefFilter.Compartment"
                    ),
                )
                sealed class UMLClassRefFilter {
                    data class Compartment(val name: CompartmentEnum, val elementsNames: List<String>) :
                        UMLClassRefFilter()
                }
            }

            class ParametricNestedPropertyRef(
                override val referencedQualifiedName: String,
                override val shape: BoundingRectState,
                override val link: String?,
                val nestedContent: List<UMLRef>,
                val isPortSized: Boolean
            ) : UMLRef.ComposableRef() {

            }

            class PackageRef(
                override val referencedQualifiedName: String,
                override val shape: BoundingRectState,
                override val link: String?
            ) : UMLRef.ComposableRef()
        }
    }

}

enum class CompartmentEnum {
    ATTRIBUTES, OPERATIONS
}

class DiagramHolderObservable(
    initName: String,
    val diagramType: DiagramType,
    val location: String,
    initContent: List<DiagramStateHolders.UMLRef>,
    val umlRoot: TMM.FS.UmlProjectFile,
    val commandStackHandler: CommandStackHandler,
    val upwardsDiagramLink: String?
) {
    val diagramName = ValidatedTextState(initial = initName, NonTransformer())

    val arrows: SnapshotStateList<Arrow> = mutableStateListOf()
    val elements: SnapshotStateList<MovableAndResizeableComponent> = mutableStateListOf()

    companion object : KLogging()

    init {
        val (elementList, arrowsList) = resolveTree(initContent)
        elements.addAll(elementList)
        arrows.addAll(arrowsList)
    }

    fun resolveTree(umlRefs: List<DiagramStateHolders.UMLRef>): Pair<List<MovableAndResizeableComponent>, List<Arrow>> {
        val (arrowRef, elementRef) = umlRefs.partition { it is DiagramStateHolders.UMLRef.ArrowRef }
        val tmmAsList = umlRoot.toList().filterIsInstance<TMM.ModelTree.Ecore>()
        val initElements = elementRef
            .map { umlRef ->
                umlRef as DiagramStateHolders.UMLRef.ComposableRef to
                        tmmAsList
                            .find { it.element is NamedElement && it.element.qualifiedName == umlRef.referencedQualifiedName }
            }
            .mapNotNull {
                val c = it.second?.element
                if (c != null && c is Class) {
                    val onMoveOrResize: (MoveOrResizeCommand) -> Unit = { moveResizeCommand ->
                        logger.debug { "Update Arrows after UMLClass movement" }
                        val updateArrowPathCommands = updateArrows(moveResizeCommand, c)
                        if (updateArrowPathCommands.isEmpty()) {
                            commandStackHandler.add(moveResizeCommand)
                        } else {
                            commandStackHandler.add(CompoundCommand(updateArrowPathCommands + moveResizeCommand))
                        }
                    }
                    val deleteCommand: (MovableAndResizeableComponent) -> RemoveFromDiagramCommand = { toDelete ->
                        val command = object : RemoveFromDiagramCommand() {
                            override suspend fun execute(handler: JobHandler) {
                                elements.remove(toDelete)
                            }

                            override suspend fun undo() {
                                elements.add(toDelete)
                            }
                        }
                        command
                    }
                    when (val composableRef = it.first) {
                        is DiagramStateHolders.UMLRef.ComposableRef.ClassRef -> {
                            UMLClassFactory.createInstance(
                                umlClass = c,
                                initBoundingRect = composableRef.shape,
                                onMoveOrResize = onMoveOrResize,
                                deleteCommand = deleteCommand,
                                filters = composableRef.filters
                            )
                        }

                        is DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedClassRef -> {
                            UMLClassFactory.createNestableInstance(
                                umlClass = c,
                                initBoundingRect = composableRef.shape,
                                onMoveOrResize = onMoveOrResize,
                                deleteCommand = deleteCommand,
                                nestedContent = resolveTree(composableRef.nestedContent)
                            )
                        }

                        is DiagramStateHolders.UMLRef.ComposableRef.ParametricNestedPropertyRef -> {
                            logger.warn { "This case should not exist!!!" }
                            null
                        }

                        else -> null
                    }

                } else
                    if (c != null && c is Package) {
                        val composableRef = it.first as DiagramStateHolders.UMLRef.ComposableRef.PackageRef
                        UMLPackageFactory.createInstance(
                            packageRef = composableRef,
                            umlPackage = c,
                            initBoundingRect = composableRef.shape,
                            onMoveOrResize = { moveResizeCommand ->
                                DNDCreation.logger.debug { "Update Arrows after UMLPackage movement" }
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
                                        elements.remove(toDelete)
                                    }

                                    override suspend fun undo() {
                                        elements.add(toDelete)
                                    }
                                }
                                command
                            },
                        )
                    } else null
            }

        println("elements.value = ${elements}")
        println("arrowRef = ${arrowRef}")

        val allDirectedRelationShip = tmmAsList.filterElementIsInstance<DirectedRelationship>()
        val allAssociations = tmmAsList.filterElementIsInstance<Association>()
        println("allDirectedRelationShip = ${allDirectedRelationShip}")
        arrowRef as List<DiagramStateHolders.UMLRef.ArrowRef>
        val initArrows = arrowRef
            .map { aRef ->
                when (aRef) {
                    is DiagramStateHolders.UMLRef.ArrowRef.GeneralRef -> {
                        allDirectedRelationShip
                            .find { dirRel ->
                                dirRel.sources
                                    .filterIsInstance<NamedElement>()
                                    .any { it.qualifiedName == aRef.memberEndName0 }
                                        && dirRel.targets
                                    .filterIsInstance<NamedElement>()
                                    .any {
                                        it.qualifiedName == aRef.memberEndName1
                                    }
                            } to aRef
                    }

                    is DiagramStateHolders.UMLRef.ArrowRef.AssocRef -> {
                        (allAssociations
                            .find { assoc ->
                                assoc.memberEnds
                                    .filter { it is NamedElement }
                                    .let { propertyList ->
                                        propertyList.any { it.name == aRef.memberEndName0 && it.aggregation.value == aRef.memberEnd1Aggregation.asKindInt() } &&
                                                propertyList.any { it.name == aRef.memberEndName1 && it.aggregation.value == aRef.memberEnd0Aggregation.asKindInt() }
                                    }
                            } to aRef).also { println("assocRef = ${it}") }
                    }

                    is DiagramStateHolders.UMLRef.ArrowRef.ConnectorRef -> TODO()
                }
            }
            .mapNotNull {
                if (it.first == null) {
                    println("No element $it")
                    null
                } else it as Pair<Relationship, DiagramStateHolders.UMLRef.ArrowRef>
            }
            .mapNotNull { (rel, ref) ->
                val firstTarget = when (rel) {
                    is DirectedRelationship -> rel.targets.first()
                    is Association -> rel.memberEnds.first()
                    else -> error("Not supported type of Relationship")
                }
                val firstSource = when (rel) {
                    is DirectedRelationship -> rel.sources.first()
                    is Association -> rel.memberEnds.last()
                    else -> error("Not supported type of Relationship")
                }
                val general = initElements.firstOrNull {
                    it.showsElement(firstTarget) || it.showsElementFromAssoc(
                        firstTarget,
                        false
                    )
                }
                val special = initElements.lastOrNull {
                    it.showsElement(firstSource) || it.showsElementFromAssoc(
                        firstSource,
                        true
                    )
                }
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
                        initOffsetPath = fourPointArrowOffsetPath.first,
                        initBoundingShape = fourPointArrowOffsetPath.second,
                        data = rel,
                        initSourceBoundingShape = special.boundingShape,
                        initTargetBoundingShape = general.boundingShape,
                        member0ArrowTypeOverride = when (ref) {
                            is DiagramStateHolders.UMLRef.ArrowRef.AssocRef -> ref.memberEnd0Aggregation
                            else -> null
                        },
                        member1ArrowTypeOverride = when (ref) {
                            is DiagramStateHolders.UMLRef.ArrowRef.AssocRef -> ref.memberEnd1Aggregation
                            else -> null
                        },
                    )
                    arrow
                } else null
            }
        return initElements to initArrows
    }

    fun updateArrows(moveResizeCommand: MoveOrResizeCommand, data: Element): List<UpdateArrowPathCommand> {

        val filteredSources = arrows.filter {arr ->
            val arrowRelationship = arr.data
            val sources = when (arrowRelationship) {
                is DirectedRelationship -> arrowRelationship.sources.contains(data)
                is Association -> (data as Class).associations.let { l ->
                    l.find { it == arrowRelationship }?.memberEnds?.get(1)
                        ?.let { it.type?.equals(data) == true && it.aggregation == arr.member0ArrowTypeOverride?.toKind() } ?: false
                }
                else -> error("Not supported type of Relationship")
            }
            sources
        }
        val filteredTargets = arrows.filter {arr ->
            val arrowRelationship = arr.data
            val targets = when (arrowRelationship) {
                is DirectedRelationship -> arrowRelationship.targets.contains(data)
                is Association -> (data as Class).associations.let { l ->
                    l.find { it == arrowRelationship }?.memberEnds?.get(0)
                        ?.let { it.type?.equals(data) == true && it.aggregation == arr.member1ArrowTypeOverride?.toKind() } ?: false
                }
                else -> error("Not supported type of Relationship")
            }
            targets
        }
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
                    before = it.first.offsetPath.value to it.first.boundingShape.value,
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
