@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.value.Colors
import mu.KLogging

@ExperimentalFoundationApi
class ProjectTreeHandler(
    private val showRoot: Boolean
) {

    companion object : KLogging()

    private var items by mutableStateOf(emptyList<TreeItem>())

    private var treeSelection by mutableStateOf(emptyList<Int>())

    private val focusRequester = FocusRequester()
    private var focus by mutableStateOf<FocusState?>(null)

    var singleSelectedItem by mutableStateOf<TreeDisplayableItem?>(
        treeSelection.firstOrNull()?.let { items.getOrNull(it)?.actual })

    private val selectAllAction = ShortcutAction.of(
        key = Key.A,
        modifierSum = ShortcutAction.KeyModifier.CTRL,
        action = {
            if (focus?.hasFocus == true) {
                treeSelection = items.indices.toMutableList()
                singleSelectedItem = null
                /*consume = */ true
            } else /*consume = */ false
        }
    )

    private val clearSelection = ShortcutAction.of(
        key = Key.Escape,
        action = {
            if (focus?.hasFocus == true) {
                treeSelection = emptyList()
                singleSelectedItem = null
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
                singleSelectedItem = null
                if (it.size == 1) {
                    it.first().onDoublePrimaryAction.invoke(com.github.jan222ik.ui.feature.main.keyevent.EmptyClickContext)
                }
            }
        }


        DisposableEffect(shortcutActionsHandler) {
            shortcutActionsHandler.apply {
                register(action = selectAllAction)
                register(action = clearSelection)
            }
            onDispose {
                shortcutActionsHandler.apply {
                    deregister(action = selectAllAction)
                    deregister(action = clearSelection)
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    println("TreeFocus = ${it}")
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when {
                                isSelected -> {
                                    Modifier.background(
                                        color = Colors.focusActive.takeIf { focus?.hasFocus == true }
                                            ?: Colors.focusInactive
                                    )
                                }
                                else -> Modifier
                            }
                        )
                        .padding(start = item.level.times(16).dp)
                        .drawBehind { drawLine(Color.Black, Offset.Zero, Offset.Zero.copy(y = this.size.height)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.canExpand) {
                            if (item.children.isNotEmpty()) {
                                Icon(
                                    modifier = Modifier.mouseClickable {
                                        if (buttons.isPrimaryPressed) {
                                            item.onDoublePrimaryAction.invoke(this)
                                            selectItem(
                                                idx = itemIdx,
                                                singleSelect = true,
                                                keepSelect = this.keyboardModifiers.isCtrlPressed
                                            )
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
                                            selectItem(
                                                idx = itemIdx,
                                                singleSelect = true,
                                                keepSelect = this.keyboardModifiers.isCtrlPressed
                                            )
                                        }
                                    },
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Expand Item"
                                )
                            }
                        }
                    }
                    item.icon?.let {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = it,
                            contentDescription = "tree item icon"
                        )

                    }
                    Text(
                        modifier = Modifier
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
        internal val actual: TreeDisplayableItem,
        private val treeHandler: ProjectTreeHandler
    ) {
        val icon: ImageVector?
            get() = actual.icon
        val name: String
            get() = actual.displayName

        val level: Int
            get() = actual.level

        val canExpand: Boolean
            get() = actual.canExpand

        val onPrimaryAction: MouseClickScope.(idx: Int) -> Unit
            get() = actual.onPrimaryAction ?: { idx ->
                treeHandler.focusRequester.requestFocus()
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
        singleSelectedItem = if (selection.size == 1) {
            treeSelection.firstOrNull()?.let { items.getOrNull(it)?.actual }
        } else {
            null
        }
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

