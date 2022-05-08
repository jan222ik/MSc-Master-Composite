package com.github.jan222ik.ui.uml

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.packFloats
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.adjusted.arrow.ArrowType
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.DirectedRelationship
import org.eclipse.uml2.uml.Generalization
import org.eclipse.uml2.uml.NamedElement
import java.io.File

data class DiagramHolder(
    val name: String,
    val diagramType: DiagramType,
    val location: String,
    val content: List<DiagramStateHolders.UMLRef>
) {
    fun toObservable(
        allUml: List<NamedElement>
    ): DiagramHolderObservable {
        return DiagramHolderObservable(
            initName = name,
            diagramType = diagramType,
            location = location,
            initContent = content,
            allUML = allUml
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
    sealed class UMLRef(open val referencedQualifiedName: String) : DiagramStateHolders() {
        class ArrowRef(referencedQualifiedName: String) : UMLRef(referencedQualifiedName)
        class ClassRef(
            override val referencedQualifiedName: String,
            val shape: BoundingRectState,
            val filters: List<UMLClassRefFilter>
        ) : UMLRef(referencedQualifiedName) {
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
            @JsonSubTypes(
                JsonSubTypes.Type(value = ClassRef.UMLClassRefFilter.Compartment::class, name = "UMLClassRefFilter.Compartment"),
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
    allUML: List<NamedElement>
) {
    val diagramName = ValidatedTextState(initial = initName, NonTransformer())

    val arrows: MutableState<List<Arrow>> = mutableStateOf(emptyList())
    val elements: MutableState<List<MovableAndResizeableComponent>> = mutableStateOf(emptyList())

    init {
        val (arrowRef, elementRef) = initContent.partition { it is DiagramStateHolders.UMLRef.ArrowRef }
        val initElements = elementRef
            .map { umlRef ->
                umlRef as DiagramStateHolders.UMLRef.ClassRef to
                        allUML.find { it.qualifiedName == umlRef.referencedQualifiedName }
            }
            .mapNotNull {
                val c = it.second
                if (c != null && c is Class) {
                    UMLClassFactory.createInstance(
                        umlClass = c,
                        initBoundingRect = it.first.shape,
                        onMoveOrResize = {},
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
        val initArrows = arrowRef
            .map { aRef -> allUML.find { it.qualifiedName == aRef.referencedQualifiedName } }
            .map {
                if (it == null || it !is DirectedRelationship) {
                    print("No element")
                    null
                } else it
            }
            .filterNotNull()
            .mapNotNull { rel ->
                val general = elements.value.firstOrNull { it.showsElement(rel.targets.first()) }
                val special = elements.value.firstOrNull { it.showsElement(rel.sources.first()) }
                if (special != null && general != null) {
                    val arrow = Arrow(
                        initOffsetPath = listOf(
                            special.boundingShape.topLeft.value,
                            general.boundingShape.topLeft.value
                        ),
                        initArrowType = when (rel) {
                            is Generalization -> ArrowType.GENERALIZATION
                            else -> ArrowType.ASSOCIATION_DIRECTED
                        },
                        data = rel
                    )
                    arrow
                } else null
            }
        arrows.value = initArrows
    }

}

sealed class DiagramStateObservable {
    class RootObservable : DiagramStateObservable() {

    }
}

fun main() {

    val diagram = DiagramHolder(
        name = "First diagram",
        diagramType = DiagramType.BLOCK_DEFINITION,
        location = "testuml",
        content = listOf(
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClass",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 200f),
                    width = 300f,
                    height = 400f,
                )
            ),
            DiagramStateHolders.UMLRef.ClassRef(
                referencedQualifiedName = "testuml::TestClassWithProperties",
                filters = emptyList(),
                shape = BoundingRectState(
                    topLeftPacked = packFloats(500f, 800f),
                    width = 300f,
                    height = 400f,
                )
            ),
            DiagramStateHolders.UMLRef.ArrowRef(
                referencedQualifiedName = ""
            )
        )
    )

    jacksonObjectMapper().writeValueAsString(diagram).also { println("diagramJson = $it") }
    val diagramsLoader = DiagramsLoader(File("C:\\Users\\jan\\Desktop\\test\\testuml.diagrams"))
    diagramsLoader.writeToFile(listOf(diagram))
    diagramsLoader.loadFromFile().tap { println(it) }.tapInvalid { println(it.e) }
}