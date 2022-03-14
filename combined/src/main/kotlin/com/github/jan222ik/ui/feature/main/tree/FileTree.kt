package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Model
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

    fun modelFilesToModelTreeRoot(resourceSet: ResourceSet, parent: FileTreeItem) {
        val contents = EcoreUtil.getAllContents<Any>(resourceSet, true)
        contents.forEachRemaining {
            if (!(it is Element && it.mustBeOwned())) {
                if (it is Model) {
                    val packageItem = ModelTreeItem.PackageItem(
                        level = parent.level,
                        displayName = it.name,
                        canExpand = it.eContents().isNotEmpty(),
                        umlPackage = it as org.eclipse.uml2.uml.Package
                    )
                    parent.addChild(packageItem)
                }
            }
        }

    }

    fun eContentsToModelTreeItem(element: Element, parent: ModelTreeItem) {
        element.eContents().forEach {
            val mapped = ModelTreeItem.parseItem(parent.level, it)
            if (mapped != null) {
                parent.addChild(mapped)
            }
        }
    }

    val loadedResourceSets = mutableMapOf<String, ResourceSet>()

}