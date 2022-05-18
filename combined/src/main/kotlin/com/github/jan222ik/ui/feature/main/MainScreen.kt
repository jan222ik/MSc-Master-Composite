package com.github.jan222ik.ui.feature.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.jan222ik.ui.feature.LocalProjectSwitcher
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.diagram.DiagramAreaComponent
import com.github.jan222ik.ui.feature.main.footer.FooterComponent
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.menu_tool_bar.MenuToolBarComponent
import com.github.jan222ik.ui.feature.main.menu_tool_bar.ToolBarComponent
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.FileTreeToolBar
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))


@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    jobHandler: JobHandler
) {
    val project = LocalProjectSwitcher.current.first
    LaunchedEffect(project) {
        FileTree.setRoot(project.root.absolutePath)
    }


    val diagramAreaComponent = remember(FileTree.treeHandler.value) {
        FileTree.treeHandler.value?.let { DiagramAreaComponent() }
    }
    MainScreenScaffold(
        menuToolBar = {
            ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
                Column {
                    MenuToolBarComponent(
                        modifier = Modifier
                            .height(MainScreenScaffoldConstants.menuToolBarHeight)
                            .fillMaxWidth(),
                        jobHandler = jobHandler
                    )
                    ToolBarComponent(
                        modifier = Modifier
                            .height(MainScreenScaffoldConstants.menuToolBarHeight)
                            .fillMaxWidth(),
                        jobHandler = jobHandler
                    )
                }
            }
        },
        footer = {
                 Box(Modifier.size(0.dp))
            /*
            ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
                val component = remember(jobHandler) { FooterComponent(jobHandler) }
                component.render(
                    modifier = Modifier
                        .height(MainScreenScaffoldConstants.footerHeight)
                        .fillMaxWidth()
                )
            }

             */
        },
        content = {
            val hSplitter = rememberSplitPaneState()
            val isMinimized = remember(hSplitter.positionPercentage) {
                hSplitter.positionPercentage < 0.03f
            }

            val action = remember {
                ShortcutAction.of(
                    key = Key.One,
                    modifierSum = ShortcutAction.KeyModifier.ALT,
                    action = {
                        if (hSplitter.positionPercentage < 0.03f) {
                            hSplitter.setToDpFromFirst(400.dp)
                        } else {
                            hSplitter.setToMin()
                        }
                        /* consume = */ true
                    }
                ).also {
                    SharedCommands.showHideExplorer = it
                }
            }
            val shortcutActionsHandler = LocalShortcutActionHandler.current
            DisposableEffect(Unit) {
                shortcutActionsHandler.register(action)
                onDispose {
                    shortcutActionsHandler.deregister(action)
                }
            }

            HorizontalSplitPane(
                //modifier = Modifier.fillMaxSize(),
                splitPaneState = hSplitter
            ) {
                first(20.dp) {
                    Box(Modifier.fillMaxSize().zIndex(3f)) {

                        Column(Modifier.padding(end = 20.dp)) {
                            ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
                                FileTreeToolBar()
                                FileTree.treeHandler.value?.render()
                            }
                        }

                        ShowMoreLess(hSplitter, isMinimized = isMinimized)
                    }
                }
                second(50.dp) {
                    Box(Modifier.zIndex(1f)) {
                        ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
                            FileTree.treeHandler.value?.let {
                                diagramAreaComponent?.render(projectTreeHandler = it)
                            }
                        }
                    }
                }
                splitter {
                    visiblePart {
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colors.background)
                        )
                    }
                    handle {
                        Box(
                            Modifier
                                .markAsHandle()
                                .cursorForHorizontalResize()
                                .background(SolidColor(Color.Gray), alpha = 0.50f)
                                .width(9.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    )

}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun BoxScope.ShowMoreLess(
    hSplitter: SplitPaneState,
    isMinimized: Boolean
) {
    Layout(modifier = Modifier.align(Alignment.TopEnd).clickable(
        onClick = {
            if (isMinimized) {
                hSplitter.setToDpFromFirst(400.dp)
            } else {
                hSplitter.setToMin()
            }

        }
    ), content = {
        Row(
            modifier = Modifier
                .rotate(90f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val imageVector = when (isMinimized) {
                true -> Icons.Filled.ExpandLess
                false -> Icons.Filled.ExpandMore
            }
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
            Text(text = "Tree")
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
        }
    }) { measurables, constraints ->
        val pRow = measurables.first()
            .measure(constraints.copy(maxHeight = constraints.maxWidth, maxWidth = constraints.maxHeight))
        layout(pRow.height, constraints.maxHeight) {
            pRow.placeRelative(x = (-pRow.width / 2) + pRow.height / 2, y = constraints.maxHeight / 2)
        }
    }
}

typealias ComposableSlot = @Composable () -> Unit

object MainScreenScaffoldConstants {
    val menuToolBarHeight = 30.dp
    val footerHeight = 30.dp
}

@Composable
fun MainScreenScaffold(
    menuToolBar: ComposableSlot,
    footer: ComposableSlot,
    content: ComposableSlot
) {
    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            menuToolBar.invoke()
            footer.invoke()
            content.invoke()
        }
    ) { measurables, initConstraints ->
        require(measurables.size == 3) { "The layout may only have a single children per slot." }
        val constraints = initConstraints.copy(minHeight = 0)
        val pTop = measurables[0].measure(constraints)
        val pBottom = measurables[1].measure(constraints)
        val restHeight = constraints.maxHeight - (pTop.measuredHeight + pBottom.measuredHeight)
        // Reset width to 0 because the splitpane would have some problems otherwise
        val nConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = restHeight)
        val pContent = measurables[2].measure(nConstraints)
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            pTop.placeRelative(IntOffset.Zero, zIndex = 2f)
            pContent.placeRelative(x = 0, y = pTop.height, zIndex = 3f)
            pBottom.placeRelative(x = 0, y = constraints.maxHeight - pBottom.height, zIndex = 1f)
        }
    }
}

