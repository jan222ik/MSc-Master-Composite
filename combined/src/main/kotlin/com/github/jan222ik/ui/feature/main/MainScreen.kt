package com.github.jan222ik.ui.feature.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalI18N
import com.github.jan222ik.ui.feature.LocalProjectSwitcher
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.diagram.DiagramAreaComponent
import com.github.jan222ik.ui.feature.main.footer.FooterComponent
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.menu_tool_bar.MenuToolBarComponent
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.feature.stringResource
import com.github.jan222ik.ui.value.R
import de.comahe.i18n4k.Locale
import mu.KotlinLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
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
    MainScreenScaffold(
        menuToolBar = {
            MenuToolBarComponent(
                modifier = Modifier
                    .height(MainScreenScaffoldConstants.menuToolBarHeight)
                    .fillMaxWidth(),
                jobHandler = jobHandler
            )
        },
        footer = {
            val component = remember(jobHandler) { FooterComponent(jobHandler) }
            component.render(
                modifier = Modifier
                    .height(MainScreenScaffoldConstants.footerHeight)
                    .fillMaxWidth()
            )
        },
        content = {
            val splitterState = rememberSplitPaneState()
            var fileTreeExpanded by remember(splitterState) { mutableStateOf(splitterState.positionPercentage < 0.2f) }

            HorizontalSplitPane(
                //modifier = Modifier.fillMaxSize(),
                splitPaneState = splitterState
            ) {
                first(400.dp) {
                    Box(Modifier.background(Color.Magenta).fillMaxSize()) {
                        Column {
                            Text("File Tree")
                            val project = LocalProjectSwitcher.current.first
                            LaunchedEffect(project) {
                                if (project == null) {
                                    FileTree.setRoot("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite")
                                } else {
                                    FileTree.setRoot(project.root.absolutePath)
                                }
                            }
                            FileTree.root?.let {
                                val projectTreeHandler = remember(it) {
                                    ProjectTreeHandler(
                                        showRoot = true,
                                    )
                                }
                                projectTreeHandler.render(root = it)
                            }
                        }
                        Button(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onClick = {
                                fileTreeExpanded = if (fileTreeExpanded) {
                                    splitterState.setToMin()
                                    false
                                } else {
                                    splitterState.dispatchRawMovement(100f)
                                    true
                                }

                            }
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open File Tree")
                        }
                    }
                }
                second(50.dp) {
                    DiagramAreaComponent().render()
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
            pTop.placeRelative(IntOffset.Zero)
            pContent.placeRelative(x = 0, y = pTop.height)
            pBottom.placeRelative(x = 0, y = constraints.maxHeight - pBottom.height)
        }
    }
}

