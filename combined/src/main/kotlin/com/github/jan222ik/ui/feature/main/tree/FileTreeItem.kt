package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.github.jan222ik.model.TMM
import mu.KLogging

@ExperimentalFoundationApi
data class FileTreeItem(
    override val level: Int,
    val tmmElement: TMM.FS
) : TreeDisplayableItem(level = level) {

    companion object : KLogging()

    override val children: SnapshotStateList<TreeDisplayableItem> = mutableStateListOf()
    override fun getTMM(): TMM = tmmElement

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
            logger.error { "Double Click $canExpand" }
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children.clear()
                } else {
                    if (tmmElement is TMM.IHasChildren<*>) {
                        children.addAll(tmmElement.children.mapNotNull { it.toViewTreeElement(level = level.inc()) })
                    }
                }
            }
        }

    override val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
        get() = { state, idx, treeContextProvider ->
            logger.debug { "TODO: Secondary Action" }
        }

    override val displayName: @Composable () -> String
        get() = @Composable { tmmElement.file.name }

    override val canExpand: Boolean
        get() = tmmElement !is TMM.FS.TreeFile


    override fun toString(): String {
        return "FileTreeItem(level=$level, tmmElement=$tmmElement, children=$children)"
    }


}