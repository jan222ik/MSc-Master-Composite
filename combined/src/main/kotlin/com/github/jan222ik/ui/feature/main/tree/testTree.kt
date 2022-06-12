package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.convertToTreeItem
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun testTree(tmmRoot: TMM.FS) {
    val treeDisplayableRoot = remember(tmmRoot) { tmmRoot.toViewTreeElement(0) }
    LazyColumn {
        fun renderDisplayable(treeDisplayableItem: TreeDisplayableItem) {
            item {
                TreeItem2(item = treeDisplayableItem)
            }
            treeDisplayableItem.children.forEach {
                renderDisplayable(it)
            }
        }
        treeDisplayableRoot?.let { renderDisplayable(it) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeItem2(item: TreeDisplayableItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically
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
                                /*
                                selectItem(
                                    idx = itemIdx,
                                    singleSelect = true,
                                    keepSelect = this.keyboardModifiers.isCtrlPressed
                                )

                                 */
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
                                /*
                                selectItem(
                                    idx = itemIdx,
                                    singleSelect = true,
                                    keepSelect = this.keyboardModifiers.isCtrlPressed
                                )

                                 */
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
                        /*
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

                         */
                    },
                    onDoubleClick = {
                        if (buttons.isPrimaryPressed) {
                            item.onDoublePrimaryAction.invoke(this@mouseCombinedClickable)
                        }
                    }
                ),
            text = item.displayName.invoke()
        )
    }
}

fun main(args: Array<String>) {
    val tmmRoot = File("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\appworkspace").convertToTreeItem()
    singleWindowApplication {
        testTree(tmmRoot)
    }
}