@file:OptIn(ExperimentalComposeUiApi::class)

package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.mouseClickable
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.ui.components.inputs.ShortcutDisplay
import com.github.jan222ik.ui.feature.LocalProjectSwitcher
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.LocalWindowActions
import com.github.jan222ik.ui.feature.LocalWindowScope
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.wizard.CreateProjectWizard
import com.github.jan222ik.ui.feature.wizard.Project
import com.github.jan222ik.util.KeyHelpers
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import com.github.jan222ik.util.KeyHelpers.onKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray),
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
                    val editMenu = remember { MenuBarContents.editMenu() }
                    MenuButton(
                        key = Key.F,
                        displayText = "File",
                        popupContent = { MenuItemList(fileMenu, jobHandler, 400.dp) })
                    MenuButton(
                        key = Key.E,
                        displayText = "Edit",
                        popupContent = { MenuItemList(editMenu, jobHandler, 300.dp) }
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

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    onClick: () -> Unit,
    isClose: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    var isHover by remember { mutableStateOf(false) }
    IconButton(
        modifier = Modifier
            .background(when (isClose) {
                true -> MaterialTheme.colors.background.takeUnless { isHover } ?: MaterialTheme.colors.error
                else -> MaterialTheme.colors.background.takeUnless { isHover } ?: MaterialTheme.colors.background.copy(
                    alpha = ContentAlpha.medium
                )
            })
            .onPointerEvent(PointerEventType.Enter) { isHover = true }
            .onPointerEvent(PointerEventType.Exit) { isHover = false },
        onClick = onClick
    ) {
        Row(content = content)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuButton(
    key: Key,
    displayText: String,
    popupContent: @Composable ColumnScope.() -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }
    LaunchedEffect(showPopup) {
        if (!showPopup) {
            withContext(Dispatchers.IO) {
                delay(100)
                isClosing = false
            }
        }
    }
    val shortcutActionsHandler = LocalShortcutActionHandler.current
    val shortcutKey = remember {
        ShortcutAction.of(
            key = key,
            modifierSum = ShortcutAction.KeyModifier.ALT,
            action = { showPopup = true; true })
    }

    DisposableEffect(Unit) {
        shortcutActionsHandler.register(shortcutKey)
        onDispose {
            shortcutActionsHandler.deregister(shortcutKey)
        }
    }
    val animDurationMillis = 100
    Box(contentAlignment = Alignment.Center) {

        val text = buildAnnotatedString {
            append(displayText)
            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), 0, 1)
        }

        Text(
            modifier = Modifier
                .mouseClickable(
                    role = Role.Button,
                    onClickLabel = "Open $displayText menu",
                    onClick = {
                        if (buttons.isPrimaryPressed) {
                            if (!isClosing) {
                                showPopup = true
                            }
                        }
                    })
                .then(if (showPopup) Modifier.background(com.github.jan222ik.ui.value.Colors.focusActive) else Modifier),
            text = text
        )

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomStart),
            visible = showPopup,
            enter = fadeIn(
                animationSpec = tween(animDurationMillis)
            ) + expandIn(
                animationSpec = tween(animDurationMillis),
                expandFrom = Alignment.TopCenter
            ),
            exit = shrinkOut(
                animationSpec = tween(animDurationMillis),
                shrinkTowards = Alignment.TopCenter
            ) + fadeOut(animationSpec = tween(animDurationMillis))
        ) {
            Popup(
                alignment = Alignment.TopStart,
                focusable = true,
                onDismissRequest = {
                    showPopup = false
                    isClosing = true
                },
                onPreviewKeyEvent = {
                    KeyHelpers.onKeyDown(it) {
                        consumeOnKey(Key.Escape) { showPopup = false }
                    }
                }
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        content = popupContent
                    )
                }
            }
        }
    }

}

@Composable
fun MenuItemList(items: List<IMenuItem>, jobHandler: JobHandler, width: Dp) {
    Column(
        modifier = Modifier
            .width(width)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val iconSpace = 16.dp
            if (item is MenuSeparator) {
                Divider(Modifier.fillMaxWidth(), startIndent = iconSpace)
            } else {
                val enabled = item.isActive()
                val scope = rememberCoroutineScope()
                var showSubFor by remember { mutableStateOf(emptyList<IMenuItem>()) }
                if (showSubFor.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        focusable = true,
                        offset = IntOffset(width.value.toInt(), 0),
                        onDismissRequest = {
                            showSubFor = emptyList()
                        }
                    ) {
                        MenuItemList(items = showSubFor, jobHandler = jobHandler, width)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                when (item) {
                                    is SubmenuItem -> {
                                        showSubFor = item.items
                                    }
                                    else -> item.command?.let {
                                        scope.launch { it.execute(jobHandler) }
                                    }
                                }
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(iconSpace)
                    ) {
                        item.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = "Execute: ${item.displayName}"
                            )
                        }
                    }
                    Box(Modifier.fillMaxWidth()){
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart),
                            text = item.displayName
                        )
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            ShortcutDisplay(item.keyShortcut)
                        }
                    }
                }
            }
        }
    }
}
