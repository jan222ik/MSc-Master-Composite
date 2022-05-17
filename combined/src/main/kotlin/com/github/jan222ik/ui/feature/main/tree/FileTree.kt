package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.mutableStateOf
import com.github.jan222ik.model.convertToTreeItem
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.NamedElement
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
object FileTree {
    val treeHandler = mutableStateOf<ProjectTreeHandler?>(null)

    fun setRoot(path: String) {
        val file = File(path)
        val tmm = file.convertToTreeItem()
        treeHandler.value = ProjectTreeHandler(
            showRoot = false,
            metamodelRoot = tmm
        )
    }

    /*
    fun fileToFileTreeItem(file: File, parent: FileTreeItem) {
        if (file.exists()) {
            val item = FileTreeItem(
                level = parent.level + 1,
                displayName = file.name,
                canExpand = file.isDirectory && file.listFiles()?.isNotEmpty() == true,
                file = file
            )
            parent.addChild(item)
        }
    }

    fun modelFilesToModelTreeRoot(projectData: ProjectData, parent: FileTreeItem) {
        val model = projectData.ecore.model
        val packageItem = ModelTreeItem.PackageItem(
            level = parent.level.inc(),
            displayName = model.name,
            canExpand = model.eContents().isNotEmpty() || projectData.diagrams.any { it.location == model.name },
            umlPackage = model as org.eclipse.uml2.uml.Package,
            allDiagrams = projectData.diagrams
        )
        parent.addChild(packageItem)
    }


    fun eContentsToModelTreeItem(element: Element, parent: ModelTreeItem) {
        val level = parent.level.inc()
        if (element is NamedElement) {
            parent.allDiagrams
                .filter { it.location == element.qualifiedName }
                .forEach {
                    val diagram = ModelTreeItem.Diagram(
                        level = level,
                        diagram = it
                    )
                    parent.addChild(diagram)
                }
        }
        element.eContents().forEach {
            val mapped = ModelTreeItem.parseItem(
                level = level,
                element = it,
                allDiagrams = parent.allDiagrams
            )
            if (mapped != null) {
                parent.addChild(mapped)
            }
        }
    }

     */

    val loadedClients = mutableStateOf<Map<String, ProjectData>>(emptyMap())

}