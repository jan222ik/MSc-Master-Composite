package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.components.tooltips.TooltipArea
import com.github.jan222ik.ui.components.tooltips.TooltipSurface
import com.github.jan222ik.ui.feature.main.MainScreenScaffoldConstants
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.util.HorizontalDivider
import com.github.jan222ik.util.VerticalDivider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTreeToolBar(
    isMinimized: Boolean,
    onMinimizeRequest: () -> Unit
) {
    ToolWindowToolbar(
        isMinimized = isMinimized,
        onMinimizeRequest = onMinimizeRequest,
        title = "File Tree",
        closeBeforeContent = false
    ) {
        TooltipArea(
            tooltip = {
                TooltipSurface {
                    Text("Find current editor in tree")
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(Space.dp16)
                    .clickable(
                        enabled = EditorManager.activeEditorTab.value != null
                    ) {
                        EditorManager.activeEditorTab.value?.tmmDiagram?.treePath()?.let { FileTree.treeHandler.value?.expandToTarget(it) }
                    },
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = when (EditorManager.activeEditorTab.value) {
                    null -> EditorColors.dividerGray
                    else -> Color.Unspecified
                }
            )
        }
        TooltipArea(
            tooltip = {
                TooltipSurface {
                    Text("Expand all")
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(Space.dp16)
                    .clickable {
                        FileTree.treeHandler.value?.expandAll()
                    },
                imageVector = Icons.Default.Expand,
                contentDescription = null
            )
        }
        TooltipArea(
            tooltip = {
                TooltipSurface {
                    Text("Collapse all")
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(Space.dp16)
                    .clickable {
                        FileTree.treeHandler.value?.collapseAll()
                    },
                imageVector = Icons.Default.Compress,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolWindowToolbar(
    isMinimized: Boolean,
    onMinimizeRequest: () -> Unit,
    title: String,
    closeBeforeContent: Boolean,
    toolbarContent: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenScaffoldConstants.menuToolBarHeight),
        color = EditorColors.backgroundGray,
    ) {
        if (!isMinimized) {
            Column(
                modifier = Modifier,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            start = Space.dp8
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title)
                    Spacer(modifier = Modifier.weight(1f))
                    InteractionsRow(
                        onMinimizeRequest, title, closeBeforeContent, toolbarContent
                    )
                }
                HorizontalDivider(Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
            }
        }
    }
}

@Composable
private fun InteractionsRow(
    onMinimizeRequest: () -> Unit,
    title: String,
    closeBeforeContent: Boolean,
    toolbarContent: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
    ) {
        if (closeBeforeContent) {
            ToolbarCloseButton(
                title, closeBeforeContent, onMinimizeRequest
            )
            toolbarContent()
        } else {
            toolbarContent()
            ToolbarCloseButton(
                title, closeBeforeContent, onMinimizeRequest
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolbarCloseButton(
    title: String,
    closeBeforeContent: Boolean,
    onMinimizeRequest: () -> Unit
) {
    Row(
        modifier = Modifier.width(21.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f), color = EditorColors.dividerGray)

        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            TooltipArea(
                tooltip = {
                    TooltipSurface {
                        Text("Minimize $title")
                    }
                }
            ) {
                Icon(
                    modifier = Modifier
                        .size(Space.dp16)
                        .clickable(onClick = onMinimizeRequest),
                    imageVector = Icons.Default.Remove,
                    contentDescription = null
                )
            }
        }
    }
}