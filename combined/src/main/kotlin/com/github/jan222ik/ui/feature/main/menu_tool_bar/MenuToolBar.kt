@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.components.menu.ActionButton
import com.github.jan222ik.ui.components.menu.MenuButton
import com.github.jan222ik.ui.components.menu.MenuItemList
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalProjectSwitcher
import com.github.jan222ik.ui.feature.LocalWindowActions
import com.github.jan222ik.ui.feature.LocalWindowScope
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.wizard.CreateProjectWizard
import com.github.jan222ik.ui.feature.wizard.Project
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.HorizontalDivider
import javax.swing.JFileChooser

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuToolBarComponent(
    modifier: Modifier,
    jobHandler: JobHandler
) {
    with(LocalWindowScope.current) {
        WindowDraggableArea(
            modifier = modifier
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(color = EditorColors.backgroundGray),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource("drawables/launcher_icons/system.png"),
                            contentDescription = "Logo"
                        )
                        var showWizard by remember { mutableStateOf(false) }
                        val (project, switchProject) = LocalProjectSwitcher.current
                        val createProjectWizard = remember {
                            CreateProjectWizard(
                                onCreationFinished = switchProject,
                                onDismissRequest = {
                                    showWizard = false
                                }
                            )
                        }
                        createProjectWizard.render(showWizard)
                        val fileMenu = remember {
                            MenuBarContents.fileMenu(
                                newCommand = object : ICommand {
                                    override fun isActive() = true
                                    override suspend fun execute(handler: JobHandler) {
                                        showWizard = true
                                    }

                                    override fun canUndo() = false
                                    override suspend fun undo() = error("Can't be undone.")
                                },
                                openExistingProjectCommand = object : ICommand {
                                    override fun isActive(): Boolean = true
                                    override suspend fun execute(handler: JobHandler) {
                                        val f = JFileChooser()
                                        f.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                        f.showSaveDialog(null)
                                        val root = f.selectedFile?.absoluteFile
                                        if (root != null) {
                                            switchProject(Project.load(root))
                                        }
                                    }

                                    override fun canUndo() = false
                                    override suspend fun undo() = error("Can't be undone.")
                                }
                            )
                        }
                        val commandStackHandler = LocalCommandStackHandler.current
                        val editMenu = remember(commandStackHandler) { MenuBarContents.editMenu(commandStackHandler) }
                        val viewMenu = remember() { MenuBarContents.viewMenu() }
                        MenuButton(
                            key = Key.F,
                            displayText = "File",
                            popupContent = { MenuItemList(fileMenu, jobHandler, 400.dp) })
                        MenuButton(
                            key = Key.E,
                            displayText = "Edit",
                            popupContent = { MenuItemList(editMenu, jobHandler, 300.dp) }
                        )
                        MenuButton(
                            key = Key.W,
                            displayText = "Window",
                            popupContent = { MenuItemList(viewMenu, jobHandler, 350.dp) }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = project?.name ?: "No project open", style = MaterialTheme.typography.overline)
                    }
                    Row(
                        Modifier.align(Alignment.CenterEnd)
                    ) {
                        val windowActions = LocalWindowActions.current
                        ActionButton(
                            onClick = windowActions::minimize
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Minimize,
                                contentDescription = "Minimize Application"
                            )
                        }
                        ActionButton(
                            onClick = windowActions::maximize
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Maximize,
                                contentDescription = "Maximize Application"
                            )
                        }
                        ActionButton(
                            onClick = windowActions::exitApplication,
                            isClose = true
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Application"
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
            }

        }
    }
}



