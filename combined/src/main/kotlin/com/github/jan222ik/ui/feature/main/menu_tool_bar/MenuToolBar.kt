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
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.components.menu.ActionButton
import com.github.jan222ik.ui.components.menu.MenuButton
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.components.menu.MenuItemList
import com.github.jan222ik.ui.feature.*
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.wizard.CreateProjectWizard
import com.github.jan222ik.ui.feature.wizard.Project
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.util.HorizontalDivider
import kotlinx.coroutines.launch
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
                                            Project.load(root)?.let { switchProject(it) }
                                        }
                                    }

                                    override fun canUndo() = false
                                    override suspend fun undo() = error("Can't be undone.")
                                }
                            )
                        }
                        val commandStackHandler = LocalCommandStackHandler.current
                        val shortcutActionsHandler = LocalShortcutActionHandler.current
                        val editMenu = remember(commandStackHandler) { MenuBarContents.editMenu(commandStackHandler) }
                        val viewMenu = remember() { MenuBarContents.viewMenu(shortcutActionsHandler) }
                        DisposableShortcutsFor(menus = fileMenu)
                        DisposableShortcutsFor(menus = editMenu)
                        DisposableShortcutsFor(menus = viewMenu)
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
                        val currfileAdditon = EditorManager.activeEditorTab.value?.let {
                            " - ${it.name}"
                        } ?: ""

                        Text(text = "${project.name}$currfileAdditon", style = MaterialTheme.typography.overline)
                    }
                    Row(
                        Modifier.align(Alignment.CenterEnd)
                    ) {
                        val windowActions = LocalWindowActions.current
                        ActionButton(
                            onClick = windowActions::minimize
                        ) {
                            Icon(
                                modifier = Modifier.size(Space.dp16),
                                imageVector = Icons.Filled.Minimize,
                                contentDescription = "Minimize Application"
                            )
                        }
                        ActionButton(
                            onClick = windowActions::maximize
                        ) { isHover ->
                            val windowState = LocalWindowState.current
                            if (windowState.placement != WindowPlacement.Floating) {
                                Box(Modifier.size(Space.dp16).clipToBounds()) {
                                    Icon(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .offset(2.dp, (-2).dp),
                                        imageVector = Icons.FloatingWindowIcon,
                                        contentDescription = "Maximize Application"
                                    )
                                    Icon(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .offset((-2).dp, 2.dp)
                                            .background(EditorColors.backgroundGray.takeUnless { isHover }
                                                ?: EditorColors.dividerGray),
                                        imageVector = Icons.FloatingWindowIcon,
                                        contentDescription = "Maximize Application"
                                    )
                                }
                            } else {
                                Icon(
                                    modifier = Modifier.size(Space.dp16),
                                    imageVector = Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = "Change to floating application window"
                                )
                            }
                        }
                        ActionButton(
                            onClick = windowActions::exitApplication,
                            isCloseBtn = true
                        ) { isHover ->
                            Icon(
                                modifier = Modifier.size(Space.dp16),
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close Application",
                                tint = when {
                                    isHover -> Color.White
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
            }

        }
    }
}

fun extractMenuItems(menus: List<MenuContribution>): List<MenuContribution.Contentful.MenuItem> {
    if (menus.isEmpty()) return emptyList()
    val (menuItems, nested) = menus
        .filterIsInstance<MenuContribution.Contentful>()
        .partition { it is MenuContribution.Contentful.MenuItem }
        .mapPair(
            mapFirst = { it.filterIsInstance<MenuContribution.Contentful.MenuItem>() },
            mapSecond = { it.filterIsInstance<MenuContribution.Contentful.NestedMenuItem>() }
        )
        .mapPair(
            mapFirst = { it },
            mapSecond = { nested -> nested.map { it.nestedItems }.flatten() }
        )
    return menuItems + extractMenuItems(nested)
}

fun <E, ER, F, FR> Pair<E, F>.mapPair(
    mapFirst: (E) -> ER,
    mapSecond: (F) -> FR
): Pair<ER, FR> {
    return Pair(
        first = mapFirst(first),
        second = mapSecond(second),
    )
}

@Composable
fun DisposableShortcutsFor(menus: List<MenuContribution>) {
    val shortcutActionsHandler = LocalShortcutActionHandler.current
    val jobHandler = LocalJobHandler.current
    val scope = rememberCoroutineScope()
    DisposableEffect(menus) {
        val menuItems = extractMenuItems(menus)
        val shortcutActions = menuItems
            .filter { it.keyShortcut.isNotEmpty() && it.command != null && !it.keyShortcutAlreadyExists }
            .mapNotNull {
                var kModSum = ShortcutAction.KeyModifier.NO_MODIFIERS
                val nonModifierKeys = it.keyShortcut
                    .filterNot { key ->
                        if (listOf(Key.CtrlLeft, Key.CtrlRight).contains(key)) {
                            kModSum += ShortcutAction.KeyModifier.CTRL
                            true
                        } else {
                            if (listOf(Key.AltLeft, Key.AltRight).contains(key)) {
                                kModSum += ShortcutAction.KeyModifier.ALT
                                true
                            } else {
                                if (listOf(Key.ShiftLeft, Key.ShiftRight).contains(key)) {
                                    kModSum += ShortcutAction.KeyModifier.SHIFT
                                    true
                                } else {
                                    if (listOf(Key.MetaLeft, Key.MetaRight).contains(key)) {
                                        kModSum += ShortcutAction.KeyModifier.META
                                        true
                                    } else false
                                }
                            }
                        }
                    }
                if (nonModifierKeys.isNotEmpty()) {
                    ShortcutAction.of(
                        key = nonModifierKeys.first(),
                        modifierSum = kModSum,
                        action = { // Command is forced not null
                            it.command!!.let {
                                if (it.isActive()) {
                                    scope.launch { it.execute(jobHandler) }
                                    true
                                } else false
                            }
                        }
                    )
                } else null
            }
        shortcutActions.forEach(shortcutActionsHandler::register)

        onDispose {
            shortcutActions.forEach(shortcutActionsHandler::deregister)
        }
    }
}



