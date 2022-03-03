@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.navigation.Component
import mu.KLogging
import java.io.File

@ExperimentalFoundationApi
class ProjectTreeHandler(
    private val root: TreeDisplayableItem,
    private val showRoot: Boolean
) : Component {

    companion object : KLogging()

    private val items = root.toItems()

    private var treeSelection by mutableStateOf(emptyList<TreeItem>())


    @Composable
    override fun render() {
        val isAllSelected = remember(items, treeSelection) { items.size == treeSelection.size }
        val shortcutActionsHandler = LocalShortcutActionHandler.current
        shortcutActionsHandler.register(
            action = ShortcutAction.of(
                key = Key.A,
                modifierSum = ShortcutAction.KeyModifier.CTRL,
                action = {
                    logger.debug { "Tree Select: CTRL + A" }
                    treeSelection = items
                    true
                }
            )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().focusable()
        ) {
            items(items.size) {
                val item = items[it]
                val isSelected = isAllSelected || remember(item, treeSelection) {
                    treeSelection.contains(item)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp)) {
                        if (item.canExpand) {
                            if (item.children.isNotEmpty()) {
                                Icon(
                                    modifier = Modifier.mouseClickable {
                                        if (buttons.isPrimaryPressed) {
                                            item.onDoublePrimaryAction.invoke(this)
                                        }
                                    },
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = "Close Item"
                                )
                            } else {
                                Icon(
                                    modifier = Modifier.mouseClickable {
                                        if (buttons.isPrimaryPressed) {
                                            item.onDoublePrimaryAction.invoke(this)
                                        }
                                    },
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Expand Item"
                                )
                            }
                        }
                    }
                    Text(
                        modifier = (if (isSelected) {
                            Modifier.border(width = 4.dp, color = Color.Red)
                        } else Modifier)
                            .mouseCombinedClickable(
                                onClick = {
                                    logger.debug { "Single" }
                                    with(buttons) {
                                        when {
                                            isPrimaryPressed -> item.onPrimaryAction.invoke(this@mouseCombinedClickable)
                                            isSecondaryPressed -> item.onSecondaryAction.invoke(this@mouseCombinedClickable)
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    logger.debug { "Double" }
                                    if (buttons.isPrimaryPressed) {
                                        item.onDoublePrimaryAction.invoke(this@mouseCombinedClickable)
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
        private val treeHandler: ProjectTreeHandler
    ) {
        val name: String
            get() = actual.displayName

        val level: Int
            get() = actual.level

        val canExpand: Boolean
            get() = actual.canExpand

        val onPrimaryAction: MouseClickScope.() -> Unit
            get() = actual.onPrimaryAction ?: {
                val singleSelect = keyboardModifiers.let { it.isCtrlPressed && !it.isShiftPressed }
                val keepSelect = keyboardModifiers.let { it.isCtrlPressed || it.isShiftPressed }
                selectItem(singleSelect = singleSelect, keepSelect = keepSelect)
            }

        val onDoublePrimaryAction: MouseClickScope.() -> Unit
            get() = actual.onDoublePrimaryAction

        val onSecondaryAction: MouseClickScope.() -> Unit
            get() = actual.onSecondaryAction

        val children: List<TreeDisplayableItem>
            get() = actual.children

        private fun selectItem(singleSelect: Boolean, keepSelect: Boolean) {
            treeHandler.selectItem(this, singleSelect, keepSelect)
        }
    }

    private fun selectItem(item: TreeItem, singleSelect: Boolean, keepSelect: Boolean) {
        logger.debug { "Select item $item, singleSelect: $singleSelect, keepSelect: $keepSelect" }
        val selection = (treeSelection.takeIf { keepSelect } ?: emptyList()).toMutableList()
        if (singleSelect) {
            selection.add(item)
        } else {
            if (selection.isEmpty()) {
                selection.add(item)
            } else {
                val lastAdditionIdx = items.indexOf(selection.last())
                val newItemIdx = items.indexOf(item)
                val min = minOf(lastAdditionIdx + 1, newItemIdx)
                val max = maxOf(lastAdditionIdx + 1, newItemIdx + 1)
                selection.addAll(items.subList(min, max))
            }
        }
        logger.debug { selection }
        treeSelection = selection
    }


    private fun TreeDisplayableItem.toItems(): List<TreeItem> {
        fun TreeDisplayableItem.addTo(list: MutableList<TreeItem>) {
            list.add(TreeItem(actual = this, treeHandler = this@ProjectTreeHandler))
            for (child in children) {
                child.addTo(list)
            }
        }

        val list = mutableListOf<TreeItem>()
        addTo(list)
        return list
    }

}

@ExperimentalFoundationApi
abstract class TreeDisplayableItem(
    open val level: Int
) {
    abstract val onPrimaryAction: (MouseClickScope.() -> Unit)?
    abstract val onDoublePrimaryAction: MouseClickScope.() -> Unit
    abstract val onSecondaryAction: MouseClickScope.() -> Unit
    abstract val displayName: String
    abstract val canExpand: Boolean

    var children: List<TreeDisplayableItem> by mutableStateOf(emptyList())
}

@ExperimentalFoundationApi
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

    override val onPrimaryAction: (MouseClickScope.() -> Unit)?
        get() = null

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children = emptyList()
                } else {
                    file.listFiles()?.forEach {
                        FileTree.fileToFileTreeItem(it, this@FileTreeItem)
                    }
                }
            }
        }

    override val onSecondaryAction: MouseClickScope.() -> Unit
        get() = {
            logger.debug { "TODO: Secondary Action" }
        }
}

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

}

