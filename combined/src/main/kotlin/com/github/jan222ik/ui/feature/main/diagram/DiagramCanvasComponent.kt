package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.main.MainScreenScaffoldConstants
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.HorizontalDivider
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi


@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
class DiagramCanvasComponent(
    val parent: DiagramAreaComponent
) {

    companion object : KLogging()

    @Composable
    fun render(projectTreeHandler: ProjectTreeHandler) {
        LaunchedEffect(Unit) {
            EditorManager.activeEditorTab.value = EditorManager.openTabs.value.firstOrNull()
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            DiagramTabRow(
                selectedIdx = EditorManager.selectedIdx.value,
                editorTabs = EditorManager.openTabs.value,
                onEditorSwitch = EditorManager::onEditorSwitch,
                onEditorClose = EditorManager::onEditorClose
            )

            Crossfade(
                targetState = EditorManager.activeEditorTab.value
            ) { activeTab ->
                if (activeTab != null)  {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
                    val vm = remember(activeTab, activeTab.id) { activeTab }
                    EditorTabComponent(
                        stateOut = vm,
                        projectTreeHandler = projectTreeHandler
                    )
                } else {
                    NoActiveTab()
                }
            }

        }
    }

    @Composable
    fun DiagramTabRow(
        selectedIdx: Int,
        editorTabs: List<EditorTabViewModel>,
        onEditorSwitch: (Int) -> Unit,
        onEditorClose: (Int) -> Unit
    ) {
        if (editorTabs.isNotEmpty()) {
            ScrollableTabRow(
                modifier = Modifier.height(MainScreenScaffoldConstants.menuToolBarHeight),
                selectedTabIndex = selectedIdx,
                backgroundColor = EditorColors.backgroundGray,
                edgePadding = TabRowDefaults.ScrollableTabRowPadding.div(2)
            ) {
                editorTabs.forEachIndexed { idx, it ->
                    Row(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 90.dp, minHeight = 24.dp)
                            .clickable {
                                onEditorSwitch.invoke(idx)
                            }
                            .then(
                                if (idx == selectedIdx) {
                                    Modifier.background(Color.White)
                                } else Modifier
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ) {

                        Image(
                            painter = it.type.iconAsPainter(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = it.name + "${it.id}",
                            textAlign = TextAlign.Center
                        )

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close editor tab",
                            modifier = Modifier.size(16.dp).clickable {
                                logger.debug { "Close tab clicked" }
                                onEditorClose.invoke(idx)
                            },
                            tint = EditorColors.dividerGray
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun NoActiveTab() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text("No active Tab")
        }
    }
}



