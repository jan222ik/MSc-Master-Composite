package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.mouseClickable
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.ui.feature.LocalWindowActions
import com.github.jan222ik.ui.feature.LocalWindowScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuToolBarComponent(modifier: Modifier) {
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
                    MenuButton(key = Key.F, displayText = "File", popupContent = { repeat(5) { Text("File Popup") } })
                    MenuButton(key = Key.E, displayText = "Edit", popupContent = { Text("Edit Popup") })
                }
                Row(
                    Modifier.align(Alignment.CenterEnd)
                ) {
                    val windowActions = LocalWindowActions.current
                    Button(
                        onClick = windowActions::minimize
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Minimize,
                            contentDescription = "Minimize Application"
                        )
                    }
                    Button(
                        onClick = windowActions::maximize
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Maximize,
                            contentDescription = "Maximize Application"
                        )
                    }
                    Button(
                        onClick = windowActions::exitApplication
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
                                println("Open $displayText popup")
                            }
                        }
                    }),
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
                    println("Dismiss")
                    showPopup = false
                    isClosing = true
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
