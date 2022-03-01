package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.navigation.Component
import mu.KLogging
import mu.KotlinLogging
import java.io.File

class ProjectTreeHandler(
    private val root: TreeDisplayableItem,
    private val showRoot: Boolean
) : Component {

    companion object : KLogging()

    private val items get() = root.toItems()


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render() {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items.size) {
                val item = items[it]
                Row {
                    Box(modifier = Modifier.size(48.dp)) {
                        if (item.canExpand) {
                            if (item.children.isNotEmpty()) {
                                Icon(
                                    modifier = Modifier.mouseClickable {
                                        if (buttons.isPrimaryPressed) {
                                            item.onDoublePrimaryAction()
                                        }
                                    },
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = "Close Item"
                                )
                            } else {
                                Icon(
                                    modifier = Modifier.mouseClickable {
                                        if (buttons.isPrimaryPressed) {
                                            item.onDoublePrimaryAction()
                                        }
                                    },
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Expand Item"
                                )
                            }
                        }
                    }
                    Text(
                        modifier = Modifier
                            .mouseCombinedClickable(
                                onClick = {
                                    logger.debug { "Single" }
                                    with(buttons) {
                                        when {
                                            isPrimaryPressed -> item.onPrimaryAction()
                                            isSecondaryPressed -> item.onSecondaryAction()
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    logger.debug { "Double" }
                                    if (buttons.isPrimaryPressed) {
                                        item.onDoublePrimaryAction()
                                    }
                                }
                            )
                            .padding(start = item.level.times(8).dp),
                        text = item.name
                    )
                }
            }
        }
    }

    internal class TreeItem(
        private val actual: TreeDisplayableItem,
    ) {
        val name: String
            get() = actual.displayName

        val level: Int
            get() = actual.level

        val canExpand: Boolean
            get() = actual.canExpand

        val onPrimaryAction: () -> Unit
            get() = actual.onPrimaryAction

        val onDoublePrimaryAction: () -> Unit
            get() = actual.onDoublePrimaryAction

        val onSecondaryAction: () -> Unit
            get() = actual.onSecondaryAction

        val children: List<TreeDisplayableItem>
            get() = actual.children
    }

    private fun TreeDisplayableItem.toItems(): List<TreeItem> {
        fun TreeDisplayableItem.addTo(list: MutableList<TreeItem>) {
            list.add(TreeItem(this))
            for (child in children) {
                child.addTo(list)
            }
        }

        val list = mutableListOf<TreeItem>()
        addTo(list)
        return list
    }

}

abstract class TreeDisplayableItem(
    open val level: Int
) {
    abstract val onPrimaryAction: () -> Unit
    abstract val onDoublePrimaryAction: () -> Unit
    abstract val onSecondaryAction: () -> Unit
    abstract val displayName: String
    abstract val canExpand: Boolean

    var children: List<TreeDisplayableItem> by mutableStateOf(emptyList())
}

data class FileTreeItem(
    override val level: Int,
    override val displayName: String,
    override val canExpand: Boolean,
    val file: File
) : TreeDisplayableItem(level = level) {

    companion object : KLogging()

    fun addChild(item: FileTreeItem) {
        children = children + item
    }

    override val onPrimaryAction: () -> Unit
        get() = {
            logger.debug { "TODO: Primary Action" }
        }

    override val onDoublePrimaryAction: () -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children = emptyList()
                } else {
                    file.listFiles()?.forEach {
                        FileTree.fileToFileTreeItem(it, this)
                    }
                }
            }
        }

    override val onSecondaryAction: () -> Unit
        get() = {
            logger.debug { "TODO: Secondary Action" }
        }
}

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

}

