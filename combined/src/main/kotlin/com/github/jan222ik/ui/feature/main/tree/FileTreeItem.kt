package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mu.KLogging
import java.io.File

@ExperimentalFoundationApi
data class FileTreeItem(
    override val level: Int,
    override val displayName: String,
    override val canExpand: Boolean,
    val file: File
) : TreeDisplayableItem(level = level) {

    companion object : KLogging()

    override val icon: @Composable ((modifier: Modifier) -> Unit)
        get() = @Composable { modifier ->
            Image(
                modifier = modifier,
                imageVector = when {
                    canExpand -> Icons.Default.Folder
                    else -> Icons.Default.FileCopy
                },
                contentDescription = null,
            )
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
                        val listFiles = file.listFiles()!!
                        listFiles.forEach { file ->
                            if (file.name.contains(".uml")) {
                                val map = FileTree.loadedClients.value
                                val res = if (map.contains(file.name)) {
                                    map[file.name]!!
                                } else {
                                    val projectData = ProjectData("testuml", listFiles)
                                    FileTree.loadedClients.value = map.toMutableMap().apply { put(file.name, projectData) }
                                    projectData // TODO Change
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

    override val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
        get() = { state, idx, treeContextProvider ->
            logger.debug { "TODO: Secondary Action" }
        }
}