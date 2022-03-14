package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import com.github.jan222ik.ecore.DiagramLoader
import mu.KLogging
import org.eclipse.emf.ecore.resource.ResourceSet
import java.io.File

@ExperimentalFoundationApi
data class FileTreeItem(
    override val level: Int,
    override val displayName: String,
    override val canExpand: Boolean,
    val file: File
) : TreeDisplayableItem(level = level) {

    companion object : KLogging()

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
        get() = null

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children = emptyList()
                } else {
                    if (file.listFiles() != null) {
                        file.listFiles()!!.forEach { file ->
                            if (file.name.contains(".uml")) {
                                val res: ResourceSet = FileTree.loadedResourceSets.computeIfAbsent(
                                    file.absolutePath
                                ) {
                                    DiagramLoader.open(file)
                                }
                                FileTree.modelFilesToModelTreeRoot(res, this@FileTreeItem)
                            } else {
                                FileTree.fileToFileTreeItem(file, this@FileTreeItem)
                            }
                        }
                    }
                }
            }
        }

    override val onSecondaryAction: MouseClickScope.() -> Unit
        get() = {
            logger.debug { "TODO: Secondary Action" }
        }
}