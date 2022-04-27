package com.github.jan222ik.ui.feature.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.keyevent.KeyEventHandler
import com.github.jan222ik.ui.value.AppTheme

object ApplicationBaseDebugWindow {

    @Composable
    fun render(isVisible: Boolean, onClose: () -> Unit, keyEventHandler: KeyEventHandler) {
        val dnDHandler = LocalDropTargetHandler.current
        Window(visible = isVisible, onCloseRequest = onClose) {
            AppTheme(
                isDark = true
            ) {
                Surface(Modifier.fillMaxSize()) {
                    Column {
                        DebugCategory(
                            name = "Drag and Drop Info:"
                        ) {
                            dnDHandler.activeTarget.value.let {
                                Text("Active: ${it?.second?.name}")
                            }
                            dnDHandler.dropTargets.value.forEach {
                                Text(text = it.second.name)
                            }
                        }
                        var keyDownBinds by remember { mutableStateOf(keyEventHandler.keyDownActions.values.flatten()) }
                        var keyReleaseBinds by remember { mutableStateOf(keyEventHandler.keyReleaseActions.values.flatten()) }
                        DebugCategory(
                            name = "Active KeyBinds",
                            nameRowComposable = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        keyDownBinds = keyEventHandler.keyDownActions.values.flatten()
                                        keyReleaseBinds = keyEventHandler.keyReleaseActions.values.flatten()
                                    }
                                )
                            }
                        ) {
                            Text("Down")
                            keyDownBinds.forEach {
                                Row {
                                    Text("Key:" + java.awt.event.KeyEvent.getKeyText(it.key.nativeKeyCode))
                                    Text("Mod: ${it.modifiers}")
                                }
                            }
                            Text("Release")
                            keyReleaseBinds.forEach {
                                Row {
                                    Text("Key:" + java.awt.event.KeyEvent.getKeyText(it.key.nativeKeyCode))
                                    Text("Mod: ${it.modifiers}")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    @Composable
    fun DebugCategory(
        name: String,
        nameRowComposable: @Composable (RowScope.() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                ) {
                    Text(text = name)
                    nameRowComposable?.invoke(this)
                }
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content
                )
            }
        }
    }
}