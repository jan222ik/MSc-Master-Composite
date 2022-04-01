package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.jan222ik.ecore.ProjectClientPerModel
import org.eclipse.uml2.uml.Element
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
object FileTree {
    var root by mutableStateOf<FileTreeItem?>(null)

    fun setRoot(path: String) {
        val file = File(path)
        root = FileTreeItem(
            level = 0,
            displayName = file.name,
            canExpand = file.isDirectory && file.listFiles()?.isNotEmpty() == true,
            file = file
        )
    }

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

    fun modelFilesToModelTreeRoot(clientPerModel: ProjectClientPerModel, parent: FileTreeItem) {
        val it = clientPerModel.model
        val packageItem = ModelTreeItem.PackageItem(
            level = parent.level.inc(),
            displayName = it.name,
            canExpand = it.eContents().isNotEmpty(),
            umlPackage = it as org.eclipse.uml2.uml.Package
        )
        parent.addChild(packageItem)
    }


    fun eContentsToModelTreeItem(element: Element, parent: ModelTreeItem) {
        element.eContents().forEach {
            val mapped = ModelTreeItem.parseItem(parent.level.inc(), it)
            if (mapped != null) {
                parent.addChild(mapped)
            }
        }
    }

    val loadedClients = mutableMapOf<String, ProjectClientPerModel>()

}