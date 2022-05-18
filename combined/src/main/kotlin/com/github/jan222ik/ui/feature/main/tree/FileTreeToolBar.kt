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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.components.tooltips.TooltipArea
import com.github.jan222ik.ui.components.tooltips.TooltipSurface
import com.github.jan222ik.ui.feature.main.MainScreenScaffoldConstants
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.util.HorizontalDivider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTreeToolBar() {
    Column(
        modifier = Modifier.height(MainScreenScaffoldConstants.menuToolBarHeight),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            color = EditorColors.backgroundGray,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = Space.dp8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("File Tree")
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                ) {
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
                                }
                            ,
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
        }
        HorizontalDivider(Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
    }
}