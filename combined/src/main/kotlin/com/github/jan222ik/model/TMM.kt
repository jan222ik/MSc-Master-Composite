package com.github.jan222ik.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ecore.EcoreModelLoader
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.transformations.ToPatternTransformer
import com.github.jan222ik.ui.feature.main.diagram.propertyview.typeNameString
import com.github.jan222ik.ui.uml.DiagramHolder
import com.github.jan222ik.ui.uml.DiagramsLoader
import com.github.jan222ik.ui.value.Space
import mu.KLogging
import org.eclipse.uml2.uml.*
import java.io.File
import java.util.regex.Pattern

sealed class TMM {
    @get:Composable
    abstract val displayName: String

    var parent: TMM? = null

    companion object : KLogging()

    fun matchesHighlightPattern(pattern: Pattern): Boolean {
        return pattern.matcher(
            when (this) {
                is FS.Directory -> file.name
                is FS.TreeFile -> file.name
                is FS.UmlProjectFile -> this.modelName
                is ModelTree.Diagram -> this.initDiagram.name // TODO
                is ModelTree.Ecore.TClass -> umlClass.name
                is ModelTree.Ecore.TPackage -> umlPackage.name
                is ModelTree.Ecore.TProperty -> property.name
                is ModelTree.Ecore.TGeneralisation -> generalization.toString()
                is ModelTree.Ecore.TPackageImport -> packageImport.importingNamespace.name
                is ModelTree.Ecore.TUNKNOWN -> element.toString()
                is ModelTree.Ecore.TAssociation -> association.name
                is ModelTree.Ecore.TConnector -> connector.name
                is ModelTree.Ecore.TConnectorEnd -> connectorEnd.definingEnd.name
                is ModelTree.Ecore.TConstraint -> constraint.name
                is ModelTree.Ecore.TEnumeration -> enumeration.name
            }
        ).find()
    }

    fun getType(): String {
        return when (this) {
            is FS.Directory -> "Folder"
            is FS.TreeFile -> "File"
            is FS.UmlProjectFile -> "Model File"
            is ModelTree.Diagram -> "${this.observed.value.diagramType.displayableName()}-Diagram"
            is ModelTree.Ecore.TClass -> "Class"
            is ModelTree.Ecore.TGeneralisation -> "Generalization"
            is ModelTree.Ecore.TPackage -> "Package"
            is ModelTree.Ecore.TPackageImport -> "PackageImport"
            is ModelTree.Ecore.TProperty -> "Property"
            is ModelTree.Ecore.TUNKNOWN -> "Unknown Element"
            is ModelTree.Ecore.TAssociation -> "Association"
            is ModelTree.Ecore.TConnector -> "Connector"
            is ModelTree.Ecore.TConnectorEnd -> "ConnectorEnd"
            is ModelTree.Ecore.TConstraint -> "Constraint"
            is ModelTree.Ecore.TEnumeration -> "Enumeration"
        }
    }

    fun findModellingElementOrNull(element: Element): TMMPath<TMM.ModelTree.Ecore>? {
        if (this is TMM.ModelTree.Ecore) {
            if (this.element == element) {
                return TMMPath(nodes = listOf(this), this)
            }
        }
        if (this is IHasChildren<*>) {
            return children.firstNotNullOfOrNull { it.findModellingElementOrNull(element) }
                ?.copyWithAttachedParent(this)
        } else {
            return null
        }
    }

    fun findDiagramElementByLocation(location: String): TMMPath<TMM.ModelTree.Diagram>? {
        return if (this is ModelTree.Diagram && this.initDiagram.let { "${it.location}::${it.name}" }
                .also { println("it = ${it} location == ${location}") } == location) {
            TMMPath(nodes = listOf(this), this)
        } else {
            if (this is TMM.IHasChildren<*>) {
                children.firstNotNullOfOrNull { it.findDiagramElementByLocation(location) }
                    ?.copyWithAttachedParent(this)
            } else null
        }
    }

    fun toList(): List<TMM> {
        val self = listOf(this)
        if (this is IHasChildren<*>) {
            return self + children.map { it.toList() }.flatten()
        } else return self
    }

    fun treePath(): TMMPath<TMM> {
        fun TMM.treePathRec(): List<TMM> {
            return (this.parent?.treePathRec() ?: emptyList()) + this
        }
        return TMMPath(
            nodes = this.treePathRec(),
            target = this
        )
    }

    fun find(predicate: (TMM) -> Boolean): TMM? {
        return if (predicate(this)) {
            this
        } else {
            if (this is IHasChildren<*>) {
                children.firstNotNullOfOrNull { it.find(predicate) }
            } else {
                null
            }
        }
    }

    interface IHasChildren<T : TMM> {
        val children: SnapshotStateList<T>
    }

    interface IBreadCrumbDisplayableMarker {
        fun markerAsTMM(): TMM = this as TMM

        fun isActive() : Boolean
    }

    sealed class FS(
        val file: File
    ) : TMM() {
        fun findModelFilesOrNull(): List<TMM.FS.UmlProjectFile>? {
            return when (this) {
                is Directory -> children.mapNotNull { it.findModelFilesOrNull() }.flatten().ifEmpty { null }
                is TreeFile -> null
                is UmlProjectFile -> listOf(this)
            }
        }

        @get:Composable
        override val displayName: String
            get() = when (this) {
                is Directory -> this.file.name
                is TreeFile -> this.file.name
                is UmlProjectFile -> this.modelName
            }

        class TreeFile(file: File) : FS(file = file)
        class Directory(file: File, val initChildren: List<FS>) : FS(file = file), IHasChildren<TMM.FS>,
            IBreadCrumbDisplayableMarker {
            override val children = initChildren.toMutableStateList()

            init {
                // TODO add file watcher for dir contents
            }

            override fun isActive() = children.isNotEmpty()
        }

        class UmlProjectFile(file: File) : FS(file = file), IHasChildren<TMM.ModelTree.Ecore.TModel>,
            IBreadCrumbDisplayableMarker {
            val modelName
                get() = file.nameWithoutExtension

            private var tmpModel: ModelTree.Ecore.TModel
            private var tmpDiagrams: List<DiagramHolder> = emptyList()

            init {
                val ecoreClientPerModel = EcoreModelLoader.open(file.name)
                DiagramsLoader(File(file.parent, "$modelName.diagrams")).loadFromFile()
                    .tap {
                        tmpDiagrams = it
                    }
                    .tapInvalid {
                        logger.debug { "Loading error of diagrams file" }
                    }

                val model = ecoreClientPerModel.model
                tmpModel = model.convertToTreeItem(tmpDiagrams) as ModelTree.Ecore.TModel
                tmpModel.ownedElements.onEach { parent = tmpModel }
            }

            val diagrams = mutableStateOf<List<DiagramHolder>>(tmpDiagrams)
            override val children: SnapshotStateList<ModelTree.Ecore.TModel> = mutableStateListOf(tmpModel)

            fun getDiagramsInSubtree(): List<TMM.ModelTree.Diagram> {
                fun TMM.dInSubRec(): List<TMM.ModelTree.Diagram> {
                    return if (this is ModelTree.Diagram) {
                        listOf(this)
                    } else {
                        if (this is IHasChildren<*>) {
                            children.flatMap { it.dInSubRec() }
                        } else {
                            emptyList()
                        }
                    }
                }
                return this.dInSubRec()
            }

            override fun isActive(): Boolean = children.isNotEmpty()
        }
    }

    sealed class ModelTree : TMM() {

        fun getProjectUMLElement(): TMM.FS.UmlProjectFile {
            val p = parent
            return if (p is TMM.FS.UmlProjectFile) {
                p
            } else {
                if (p is TMM.ModelTree) {
                    p.getProjectUMLElement()
                } else throw ClassCastException("Should not happen to be in this case")
            }
        }

        fun closestPackage(): TMM.ModelTree.Ecore.TPackage {
            val p = parent
            return if (p is TMM.ModelTree.Ecore.TPackage) {
                p
            } else {
                if (p is TMM.ModelTree) {
                    p.closestPackage()
                } else throw ClassCastException("Should not happen to be in this case")
            }
        }

        @get:Composable
        override val displayName: String
            get() = when (this) {
                is Diagram -> observed.value.diagramName.tfv.text
                is Ecore.TClass -> this.umlClass.name
                is Ecore.TGeneralisation -> this.generalization.general.name
                is Ecore.TPackage -> this.umlPackage.name
                is Ecore.TPackageImport -> this.packageImport.importedPackage.name
                is Ecore.TProperty -> this.name.tfv.text.takeUnless { it.isEmpty() }
                    ?: property.type?.name?.let { ":$it" } ?: "null"

                is Ecore.TUNKNOWN -> "<<UNKNOWN ELEMENT>>"
                is Ecore.TAssociation -> this.association.name?.let { "<Association> $it" }
                    ?: "A_${association.memberEnds.let { it.first().name + "_" + it.last().name }}"

                is Ecore.TConnector -> this.connector.name
                is Ecore.TConnectorEnd -> this.connectorEnd.definingEnd.name
                is Ecore.TConstraint -> this.constraint.name
                is Ecore.TEnumeration -> this.enumeration.name
            }

        sealed class Ecore(
            val element: Element,
            initOwnedElements: List<ModelTree>
        ) : ModelTree() {

            val ownedElements = initOwnedElements.toMutableStateList()

            open class TPackage(
                val umlPackage: org.eclipse.uml2.uml.Package,
                initOwnedElements: List<ModelTree>
            ) : Ecore(
                element = umlPackage,
                initOwnedElements = initOwnedElements
            ), IHasChildren<TMM.ModelTree>, IBreadCrumbDisplayableMarker {
                override val children: SnapshotStateList<ModelTree>
                    get() = ownedElements

                fun createOwnedClass(name: String, isAbstract: Boolean): TClass {
                    val createOwnedClass = umlPackage.createOwnedClass(name, isAbstract)
                    val newTMM = TClass(umlClass = createOwnedClass, initOwnedElements = emptyList())
                    newTMM.parent = this
                    ownedElements.add(newTMM)
                    return newTMM
                }

                override fun isActive(): Boolean = children.any { it is IBreadCrumbDisplayableMarker }

            }

            class TModel(
                model: org.eclipse.uml2.uml.Model,
                initOwnedElements: List<ModelTree>
            ) : TPackage(
                umlPackage = model,
                initOwnedElements = initOwnedElements
            )

            class TClass(val umlClass: Class, initOwnedElements: List<ModelTree>) : Ecore(
                element = umlClass,
                initOwnedElements = initOwnedElements
            ), IHasChildren<TMM.ModelTree>, IBreadCrumbDisplayableMarker {

                val name = ValidatedTextState<String>(
                    initial = umlClass.name ?: "",
                    transformation = NonTransformer()
                )

                fun createOwnedProperty(): TProperty {
                    val prop = umlClass.createOwnedAttribute("Property", null)
                    val newTmm = TMM.ModelTree.Ecore.TProperty(
                        property = prop,
                        initOwnedElements = emptyList()
                    )
                    newTmm.parent = this
                    ownedElements.add(newTmm)
                    return newTmm
                }

                fun createGeneralization(classifier: Classifier): TGeneralisation {
                    val generalization = umlClass.createGeneralization(classifier)
                    val newTMM = TGeneralisation(generalization = generalization, initOwnedElements = emptyList())
                    newTMM.parent = this
                    ownedElements.add(newTMM)
                    return newTMM
                }

                fun createAssociation(
                    end1isNavigable: Boolean = false,
                    end1Aggregation: AggregationKind,
                    end1Name: String,
                    end1Lower: Int,
                    end1Upper: Int,
                    end1Type: Type,
                    end2isNavigable: Boolean = false,
                    end2Aggregation: AggregationKind,
                    end2Name: String,
                    end2Lower: Int,
                    end2Upper: Int,
                ): TAssociation {

                    val association = umlClass.createAssociation(
                        end1isNavigable,
                        end1Aggregation,
                        end1Name,
                        end1Lower,
                        end1Upper,
                        end1Type,
                        end2isNavigable,
                        end2Aggregation,
                        end2Name,
                        end2Lower,
                        end2Upper,
                    )

                    val newTMM = TAssociation(association = association, initOwnedElements = emptyList())
                    newTMM.parent = this
                    ownedElements.add(newTMM)
                    return newTMM
                }


                override val children: SnapshotStateList<ModelTree>
                    get() = ownedElements

                override fun isActive(): Boolean = children.any { it is IBreadCrumbDisplayableMarker }
            }

            class TProperty(val property: Property, initOwnedElements: List<ModelTree>) : Ecore(
                element = property,
                initOwnedElements = initOwnedElements
            ) {
                val type = ValidatedTextState<String>(
                    initial = property.typeNameString() ?: "",
                    transformation = NonTransformer()
                )
                val name = ValidatedTextState<String>(
                    initial = property.name ?: "",
                    transformation = NonTransformer()
                )
            }

            class TGeneralisation(val generalization: Generalization, initOwnedElements: List<ModelTree>) : Ecore(
                element = generalization,
                initOwnedElements = initOwnedElements
            )

            class TPackageImport(val packageImport: PackageImport, initOwnedElements: List<ModelTree>) : Ecore(
                element = packageImport,
                initOwnedElements = initOwnedElements
            )

            class TAssociation(val association: Association, initOwnedElements: List<ModelTree>) : Ecore(
                element = association,
                initOwnedElements = initOwnedElements
            ) {
                val memberEnd0MultiplicityString = ValidatedTextState<String>(
                    initial = getMultiplicityStringForMemberAt(0),
                    transformation = NonTransformer()
                )

                val memberEnd1MultiplicityString = ValidatedTextState<String>(
                    initial = getMultiplicityStringForMemberAt(1),
                    transformation = NonTransformer()
                )

                fun getMultiplicityStringForMemberAt(idx: Int): String {
                    val end = association.memberEnds[idx]
                    if (end.lower == 1 && end.upper == 1) {
                        return "1"
                    } else {
                        val lower = end.lower.takeUnless { it == -1 }?.toString() ?: "*"
                        val upper = end.upper.takeUnless { it == -1 }?.toString() ?: "*"
                        return "$lower..$upper"
                    }
                }
            }

            class TConnector(val connector: Connector, initOwnedElements: List<ModelTree>) : Ecore(
                element = connector,
                initOwnedElements = initOwnedElements
            )

            class TConnectorEnd(val connectorEnd: ConnectorEnd, initOwnedElements: List<ModelTree>) : Ecore(
                element = connectorEnd,
                initOwnedElements = initOwnedElements
            )

            class TEnumeration(val enumeration: Enumeration, initOwnedElements: List<ModelTree>) : Ecore(
                element = enumeration,
                initOwnedElements = initOwnedElements
            )

            class TConstraint(val constraint: Constraint, initOwnedElements: List<ModelTree>) : Ecore(
                element = constraint,
                initOwnedElements = initOwnedElements
            )

            class TUNKNOWN(element: Element, initOwnedElements: List<ModelTree>) : Ecore(
                element = element,
                initOwnedElements = initOwnedElements
            )
        }

        class Diagram(val initDiagram: DiagramHolder) : ModelTree(), IBreadCrumbDisplayableMarker {
            val observed = lazy {
                initDiagram.toObservable(
                    umlRoot = getProjectUMLElement(),
                    commandStackHandler = CommandStackHandler.INSTANCE
                )
            }

            override fun isActive() = true
        }
    }
}

data class TMMPath<T : TMM>(
    val nodes: List<TMM>,
    val target: T
) {
    fun copyWithAttachedParent(parent: TMM): TMMPath<T> {
        val newNodes = nodes.toMutableList()
        newNodes.add(0, parent)
        return this.copy(nodes = newNodes)
    }

}

fun Element.convertToTreeItem(tmpDiagrams: List<DiagramHolder>): TMM.ModelTree {
    val ownedDiagrams = if (this is NamedElement) {
        tmpDiagrams.filter { it.location == this.qualifiedName }.map {
            TMM.ModelTree.Diagram(initDiagram = it)
        }
    } else emptyList()
    val initOwnedElements = ownedDiagrams + this.ownedElements.map { it.convertToTreeItem(tmpDiagrams) }
    val tmmElement = if (this is org.eclipse.uml2.uml.Package) {
        if (this is Model) {
            TMM.ModelTree.Ecore.TModel(
                model = this,
                initOwnedElements = initOwnedElements
            )
        } else {
            TMM.ModelTree.Ecore.TPackage(
                umlPackage = this,
                initOwnedElements = initOwnedElements
            )
        }
    } else {
        if (this is org.eclipse.uml2.uml.Class) {
            TMM.ModelTree.Ecore.TClass(
                umlClass = this,
                initOwnedElements = initOwnedElements
            )
        } else {
            if (this is Property) {
                TMM.ModelTree.Ecore.TProperty(
                    property = this,
                    initOwnedElements = initOwnedElements
                )
            } else {
                if (this is Generalization) {
                    TMM.ModelTree.Ecore.TGeneralisation(
                        generalization = this,
                        initOwnedElements = initOwnedElements
                    )
                } else {
                    if (this is PackageImport) {
                        TMM.ModelTree.Ecore.TPackageImport(
                            packageImport = this,
                            initOwnedElements = initOwnedElements
                        )
                    } else {
                        if (this is Association) {
                            TMM.ModelTree.Ecore.TAssociation(
                                association = this,
                                initOwnedElements = initOwnedElements
                            )
                        } else {
                            if (this is Connector) {
                                TMM.ModelTree.Ecore.TConnector(
                                    connector = this,
                                    initOwnedElements = initOwnedElements
                                )
                            } else {
                                if (this is ConnectorEnd) {
                                    TMM.ModelTree.Ecore.TConnectorEnd(
                                        connectorEnd = this,
                                        initOwnedElements = initOwnedElements
                                    )
                                } else {
                                    if (this is Enumeration) {
                                        TMM.ModelTree.Ecore.TEnumeration(
                                            enumeration = this,
                                            initOwnedElements = initOwnedElements
                                        )
                                    } else {
                                        if (this is Constraint) {
                                            TMM.ModelTree.Ecore.TConstraint(
                                                constraint = this,
                                                initOwnedElements = initOwnedElements
                                            )
                                        } else {
                                            TMM.ModelTree.Ecore.TUNKNOWN(
                                                element = this,
                                                initOwnedElements = initOwnedElements
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    tmmElement.ownedElements.onEach { it.parent = tmmElement }
    return tmmElement
}

fun File.convertToTreeItem(): TMM.FS {
    return if (this.isDirectory) {
        TMM.FS.Directory(
            file = this,
            initChildren = this.listFiles()?.map { it.convertToTreeItem() } ?: emptyList()
        ).apply {
            children.onEach { it.parent = this }
        }
    } else {
        if (this.extension == "uml") {
            TMM.FS.UmlProjectFile(this).apply {
                children.onEach { it.parent = this }
            }
        } else {
            TMM.FS.TreeFile(this)
        }
    }
}

@Composable
fun TMM.viewer(level: Int, showFS: Boolean, highlight: Pattern, searchFor: (Element) -> Unit) {
    val matchesHighlightPattern = matchesHighlightPattern(highlight)
    Column(modifier = Modifier.padding(start = Space.dp8.times(level))) {
        with(this@viewer) {
            ProvideTextStyle(LocalTextStyle.current.copy(background = Color.White.takeUnless { matchesHighlightPattern }
                ?: Color.Yellow.copy(alpha = 0.5f))) {
                when (this) {
                    is TMM.FS.Directory -> {
                        if (showFS) {
                            Text("Dir: ${this.file.name}")
                        }
                        this.children.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                    }

                    is TMM.FS.TreeFile -> {
                        if (showFS) {
                            Text("File: ${this.file.name}")
                        }
                    }

                    is TMM.FS.UmlProjectFile -> {
                        if (showFS) {
                            Text("UML File: ${this.file.name}")
                        }
                        this.children.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                    }

                    is TMM.ModelTree.Diagram -> {
                        Text("Diagram")
                    }

                    is TMM.ModelTree.Ecore -> {
                        val mod = Modifier.clickable { searchFor(this.element) }
                        when (this) {
                            is TMM.ModelTree.Ecore.TClass -> {
                                Text("Class: ${umlClass.name}", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }

                            is TMM.ModelTree.Ecore.TProperty -> {
                                Text("Property: ${property.name}", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }

                            is TMM.ModelTree.Ecore.TPackage -> {
                                Text("Package: ${umlPackage.name}", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }

                            is TMM.ModelTree.Ecore.TGeneralisation -> {
                                Text("Generalization: ${generalization.toString()}", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }

                            is TMM.ModelTree.Ecore.TPackageImport -> {
                                Text("PackageImport: ${packageImport.importedPackage.name}", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }

                            is TMM.ModelTree.Ecore.TUNKNOWN -> {
                                Text("Unknown: $element", mod)
                                ownedElements.forEach { it.viewer(level.inc(), showFS, highlight, searchFor) }
                            }
                        }
                    }
                }
            }

        }
    }
}

fun main(args: Array<String>) {
    val file = File("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\appworkspace")
    val treeItem = file.convertToTreeItem()
    println("End")
    singleWindowApplication {
        val showFS = remember(treeItem) { mutableStateOf(false) }
        val transformer = remember { ToPatternTransformer() }
        val activePattern = remember { mutableStateOf<Pattern>("".toPattern()) }
        val patternText = remember(transformer) {
            ValidatedTextState(
                initial = "",
                transformation = transformer,
                onValidValue = { pattern, _ ->
                    activePattern.value = pattern
                }
            )
        }
        Column {
            Row {
                TextField(
                    value = patternText.tfv,
                    onValueChange = patternText::onValueChange,
                    isError = patternText.errors.isNotEmpty()
                )
                Switch(
                    checked = showFS.value,
                    onCheckedChange = { showFS.value = it }
                )
            }
            treeItem.viewer(0, showFS.value, activePattern.value, searchFor = {
                println("tmm= " + treeItem.findModellingElementOrNull(it))
            })
        }
    }
}

