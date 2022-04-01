package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.jan222ik.ecore.DiagramLoader
import com.github.jan222ik.ecore.ProjectClientPerModel
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

    override val icon: ImageVector?
        get() = when {
            canExpand -> Icons.Default.Folder
            else -> Icons.Default.FileCopy
        }

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
                                val res: ProjectClientPerModel = FileTree.loadedClients.computeIfAbsent(
                                    file.name
                                ) {
                                    DiagramLoader.open("testuml.uml") // TODO Change
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