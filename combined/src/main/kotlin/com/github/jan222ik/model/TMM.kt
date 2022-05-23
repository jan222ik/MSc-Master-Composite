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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.decapitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ecore.EcoreModelLoader
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.ToPatternTransformer
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.uml.DiagramHolder
import com.github.jan222ik.ui.uml.DiagramsLoader
import com.github.jan222ik.ui.value.Space
import mu.KLogging
import org.eclipse.uml2.uml.*
import java.io.File
import java.util.regex.Pattern

sealed class TMM {
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
            }
        ).find()
    }

    fun getType() : String {
        return when (this) {
            is FS.Directory -> "Folder"
            is FS.TreeFile -> "File"
            is FS.UmlProjectFile -> "Model File"
            is ModelTree.Diagram -> "${initDiagram.diagramType.name.toLowerCase(Locale.current).capitalize(Locale.current)}-Diagram"
            is ModelTree.Ecore.TClass -> "Class"
            is ModelTree.Ecore.TGeneralisation -> "Generalization"
            is ModelTree.Ecore.TPackage -> "Package"
            is ModelTree.Ecore.TPackageImport -> "PackageImport"
            is ModelTree.Ecore.TProperty -> "Property"
            is ModelTree.Ecore.TUNKNOWN -> "Unknown Element"
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
        return if (this is ModelTree.Diagram && this.initDiagram.let { "${it.location}::${it.name}" } == location) {
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

    fun find(predicate: (TMM) -> Boolean) : TMM? {
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

    interface IBreadCrumbDisplayableMarker

    sealed class FS(
        val file: File
    ) : TMM() {
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

        override val displayName: String
            get() = when (this) {
                is Diagram -> this.initDiagram.name
                is Ecore.TClass -> this.umlClass.name
                is Ecore.TGeneralisation -> this.generalization.general.name
                is Ecore.TPackage -> this.umlPackage.name
                is Ecore.TPackageImport -> this.packageImport.importedPackage.name
                is Ecore.TProperty -> this.property.name
                is Ecore.TUNKNOWN -> "<<UNKNOWN ELEMENT>>"
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
                override val children: SnapshotStateList<ModelTree>
                    get() = ownedElements
            }

            class TProperty(val property: Property, initOwnedElements: List<ModelTree>) : Ecore(
                element = property,
                initOwnedElements = initOwnedElements
            )

            class TGeneralisation(val generalization: Generalization, initOwnedElements: List<ModelTree>) : Ecore(
                element = generalization,
                initOwnedElements = initOwnedElements
            )

            class TPackageImport(val packageImport: PackageImport, initOwnedElements: List<ModelTree>) : Ecore(
                element = packageImport,
                initOwnedElements = initOwnedElements
            )

            class TUNKNOWN(element: Element, initOwnedElements: List<ModelTree>) : Ecore(
                element = element,
                initOwnedElements = initOwnedElements
            )
        }

        class Diagram(val initDiagram: DiagramHolder) : ModelTree(), IBreadCrumbDisplayableMarker
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
                    }
                    TMM.ModelTree.Ecore.TUNKNOWN(
                        element = this,
                        initOwnedElements = initOwnedElements
                    )
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

