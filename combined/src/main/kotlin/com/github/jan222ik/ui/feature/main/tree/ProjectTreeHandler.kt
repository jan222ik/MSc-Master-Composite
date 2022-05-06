@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.ui.components.dnd.dndDraggable
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.components.menu.MenuItemList
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.LocalJobHandler
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.KeyHelpers
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import mu.KLogging
import org.eclipse.uml2.uml.Element


@OptIn(ExperimentalFoundationApi::class)
class ProjectTreeHandler(
    private val showRoot: Boolean
) : ITreeContextFor {

    companion object : KLogging()

    private var items by mutableStateOf(emptyList<TreeItem>())

    private var treeSelection by mutableStateOf(emptyList<Int>())

    private val focusRequester = FocusRequester()
    private var focus by mutableStateOf<FocusState?>(null)

    var singleSelectedItem by mutableStateOf<TreeDisplayableItem?>(
        treeSelection.firstOrNull()?.let { items.getOrNull(it)?.actual })

    var setTreeSelectionByElements: ((List<Element>) -> Unit)? by mutableStateOf(null)

    var contextMenuFor by mutableStateOf<Pair<PopupPositionProvider, List<MenuContribution>>?>(null)

    override fun setContextFor(pair: Pair<PopupPositionProvider, List<MenuContribution>>?) {
        contextMenuFor = pair
    }

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
                setTreeSelectionByElements = { elements: List<Element> ->
                    logger.debug { "setTreeSelectionByElements elements:${elements.size} items:${items.size}" }
                    treeSelection = items.mapIndexed { index, treeItem ->
                        logger.debug { "treeItem = ${treeItem.actual}" }
                        if (treeItem.actual is ModelTreeItem && elements.contains(treeItem.actual.getElement())) {
                            index
                        } else null
                    }.filterNotNull()
                }
            }
        }
        val lazyListState = rememberLazyListState()
        val scrollbarAdapter = rememberScrollbarAdapter(scrollState = lazyListState)


        contextMenuFor?.let {
            val (popupPosProvider, menuContributions) = it
            Popup(
                popupPositionProvider = popupPosProvider ,
                onDismissRequest = { contextMenuFor = null },
                onPreviewKeyEvent = { KeyHelpers.onKeyDown(it) { consumeOnKey(Key.Escape) { contextMenuFor = null } } },
                focusable = true
            ) {
                Card(
                    border = BorderStroke(1.dp, EditorColors.dividerGray)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                    ) {
                        MenuItemList(items = menuContributions, jobHandler = LocalJobHandler.current, width = 400.dp)
                    }
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
                },
            state = lazyListState
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
                                        color = EditorColors.focusActive.takeIf { focus?.hasFocus == true }
                                            ?: EditorColors.focusInactive
                                    )
                                }
                                else -> Modifier
                            }
                        )
                        .padding(start = item.level.times(16).dp)
                        .drawBehind { drawLine(Color.Black, Offset.Zero, Offset.Zero.copy(y = this.size.height)) }
                        .then(
                            when (val actual = item.actual) {
                                is ModelTreeItem -> Modifier.dndDraggable(
                                    handler = LocalDropTargetHandler.current,
                                    dataProvider = { actual.getElement() },
                                    onDragCancel = Function0<Unit>::invoke,
                                    onDragFinished = { _, snapback -> snapback.invoke() }
                                )
                                else -> Modifier
                            }
                        ),
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
                    item.icon?.invoke(Modifier.size(16.dp))
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
                                            isSecondaryPressed -> item.onSecondaryAction.invoke(
                                                this@mouseCombinedClickable,
                                                lazyListState,
                                                items.indexOf(item),
                                                this@ProjectTreeHandler as ITreeContextFor
                                            )
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
        val icon: @Composable ((modifier: Modifier) -> Unit)?
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

        val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
            get() = { state, idx, contextFor ->
                onPrimaryAction.invoke(this, idx)
                actual.onSecondaryAction.invoke(this, state, idx, contextFor)
            }

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

