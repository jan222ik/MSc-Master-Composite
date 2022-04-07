package com.github.jan222ik.ui.components.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.value.Colors
import com.github.jan222ik.util.KeyHelpers
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
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
                .then(if (showPopup) Modifier.background(Colors.focusActive) else Modifier),
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

