@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import mu.KLogging
import java.io.File

@ExperimentalFoundationApi
class ProjectTreeHandler(
    private val showRoot: Boolean
) {

    companion object : KLogging()

    private var items by mutableStateOf(emptyList<TreeItem>())

    private var treeSelection by mutableStateOf(emptyList<Int>())

    private var focus by mutableStateOf<FocusState?>(null)

    private val selectAllAction = ShortcutAction.of(
        key = Key.A,
        modifierSum = ShortcutAction.KeyModifier.CTRL,
        action = {
            if (focus?.hasFocus == true) {
                treeSelection = items.indices.toMutableList()
                /*consume = */ true
            } else /*consume = */ false
        }
    )


    @Composable
    fun render(
        root: TreeDisplayableItem,
    ) {
        val shortcutActionsHandler = LocalShortcutActionHandler.current
        root.toItems().let {
            if (it.size != items.size) {
                items = it
                treeSelection = emptyList()
            }
        }


        DisposableEffect(shortcutActionsHandler) {
            shortcutActionsHandler.register(action = selectAllAction)
            onDispose {
                shortcutActionsHandler.deregister(action = selectAllAction)
            }
        }
        val focusRequester = remember { FocusRequester() }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    println("Focus State = $it")
                    focus = it
                }.apply {
                    if (focus?.hasFocus == true) {
                        this.border(4.dp, Color.White)
                    }
                }
                .focusTarget()
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusRequester.requestFocus()
                    }
                }
        ) {
            items(items.size) { itemIdx ->
                val item = items[itemIdx]
                val isSelected = remember(itemIdx, treeSelection) {
                    treeSelection.contains(itemIdx)
                }
                Row(
                    modifier = Modifier.padding(start = item.level.times(8).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                                    with(buttons) {
                                        when {
                                            isPrimaryPressed -> item.onPrimaryAction.invoke(
                                                this@mouseCombinedClickable,
                                                itemIdx
                                            )
                                            isSecondaryPressed -> item.onSecondaryAction.invoke(this@mouseCombinedClickable)
                                        }
                                    }
                                },
                                onDoubleClick = {
                                    if (buttons.isPrimaryPressed) {
                                        item.onDoublePrimaryAction.invoke(this@mouseCombinedClickable)
                                    }
                                }
                            ),
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

        val onPrimaryAction: MouseClickScope.(idx: Int) -> Unit
            get() = actual.onPrimaryAction ?: { idx ->
                val singleSelect = keyboardModifiers.let { it.isCtrlPressed && !it.isShiftPressed }
                val keepSelect = keyboardModifiers.let { it.isCtrlPressed || it.isShiftPressed }
                selectItem(idx = idx, singleSelect = singleSelect, keepSelect = keepSelect)
            }

        val onDoublePrimaryAction: MouseClickScope.() -> Unit
            get() = actual.onDoublePrimaryAction

        val onSecondaryAction: MouseClickScope.() -> Unit
            get() = actual.onSecondaryAction

        val children: List<TreeDisplayableItem>
            get() = actual.children

        private fun selectItem(idx: Int, singleSelect: Boolean, keepSelect: Boolean) {
            treeHandler.selectItem(idx = idx, singleSelect = singleSelect, keepSelect = keepSelect)
        }
    }

    private fun selectItem(idx: Int, singleSelect: Boolean, keepSelect: Boolean) {
        val selection = (treeSelection.takeIf { keepSelect } ?: emptyList()).toMutableList()
        if (singleSelect) {
            if (selection.contains(idx)) {
                selection.remove(element = idx)
            } else {
                selection.add(idx)
            }
        } else {
            if (selection.isEmpty()) {
                selection.add(idx)
            } else {
                val lastAdditionIdx = selection.last()
                val min = minOf(lastAdditionIdx + 1, idx)
                val max = maxOf(lastAdditionIdx + 1, idx + 1)
                selection.addAll(IntRange(start = min, endInclusive = max - 1).toList())
            }
        }
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
    abstract val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
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

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
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
