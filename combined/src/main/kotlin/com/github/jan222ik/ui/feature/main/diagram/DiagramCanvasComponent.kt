package com.github.jan222ik.ui.feature.main.diagram

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabComponent
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
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
    fun render() {
        val tabs = remember { mutableStateOf(
            listOf(
                EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
                EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
                //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
                //EditorTabViewModel(name = "a", DiagramType.BLOCK_DEFINITION),
                //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
                //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
                //EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
                //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
                //EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
            )
        ) }
        val activeEditorTab = LocalActiveEditorTab.current
        LaunchedEffect(Unit) {
            activeEditorTab.value = tabs.value.firstOrNull()
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var selectedIdx by remember { mutableStateOf(0) }
            DiagramTabRow(
                selectedIdx = selectedIdx,
                editorTabs = tabs.value,
                onEditorSwitch = { nextIdx ->
                    selectedIdx = nextIdx
                    activeEditorTab.value = tabs.value[nextIdx]
                },
                onEditorClose = { closeIdx ->
                    tabs.value = tabs.value.toMutableList().apply { removeAt(closeIdx) }
                    if (closeIdx == selectedIdx) {
                        if (tabs.value.isNotEmpty()) {
                            selectedIdx = closeIdx.dec()
                            activeEditorTab.value = tabs.value.getOrNull(selectedIdx)
                        }
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
            LocalActiveEditorTab.current.value?.let {
                EditorTabComponent(
                    state = it,
                    projectTreeHandler = parent.projectTreeHandler
                )
            } ?: kotlin.run {
                Text("No active Tab")
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
                            painter = when (it.type) {
                                DiagramType.PACKAGE -> painterResource("drawables/uml_icons/Diagram_SysML_Package.gif")
                                DiagramType.PARAMETRIC -> painterResource("drawables/uml_icons/Diagram_Parametric.png")
                                DiagramType.BLOCK_DEFINITION -> painterResource("drawables/uml_icons/Diagram_BlockDefinition.gif")
                            },
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
}



