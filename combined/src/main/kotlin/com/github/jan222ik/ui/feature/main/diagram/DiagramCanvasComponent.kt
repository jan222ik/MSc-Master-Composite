package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.isForwardPressed
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.main.MainScreenScaffoldConstants
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.feature.main.keyevent.mouseCombinedClickable
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
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
            modifier = Modifier
                .fillMaxSize()
                .mouseCombinedClickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (this.buttons.isBackPressed) {
                            EditorManager.moveBack()
                        } else {
                            if (this.buttons.isForwardPressed) {
                                EditorManager.moveForward()
                            }
                        }
                    },
                    onLongClick = {},
                    onDoubleClick = {}
                ),
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
                if (activeTab != null) {
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
                            text = it.name,
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
        val commandStackHandler = LocalCommandStackHandler.current
        ProvideTextStyle(
            LocalTextStyle.current.copy(fontSize = 18.sp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().background(EditorColors.dividerGray),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No active Tab")
                Text("Existing Diagrams in Project")
                Column {
                    val modelFiles = remember(
                        FileTree.treeHandler.value, FileTree.treeHandler.value?.metamodelRoot
                    ) {
                        FileTree.treeHandler.value?.metamodelRoot?.findModelFilesOrNull()
                    }
                    modelFiles?.map { it.getDiagramsInSubtree() }?.flatten()?.forEach {
                        Row(
                            modifier = Modifier.clickable {
                                EditorManager.moveToOrOpenDiagram(
                                    tmmDiagram = it,
                                    commandStackHandler = commandStackHandler
                                )
                            },
                            horizontalArrangement = Arrangement.spacedBy(Space.dp8),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = it.initDiagram.diagramType.iconAsPainter(),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                            Text(text = it.initDiagram.name)
                        }
                    }
                }
            }
        }
    }
}



