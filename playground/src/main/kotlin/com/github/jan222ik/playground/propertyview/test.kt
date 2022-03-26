package com.github.jan222ik.playground.propertyview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.singleWindowApplication


@ExperimentalComposeUiApi
fun main() {
    singleWindowApplication {
        examplePhone()
    }
}

@ExperimentalComposeUiApi
@Composable
fun exampleDesktop() {
    val focusManager = LocalFocusManager.current
    fun handleKeyPreview(evt: KeyEvent): Boolean {
        return if (evt.type == KeyEventType.KeyDown && evt.key == Key.Tab) {
            if (evt.isShiftPressed) {
                focusManager.moveFocus(FocusDirection.Up)
            } else {
                focusManager.moveFocus(FocusDirection.Down)
            }
            true
        } else false
    }
    LazyColumn {
        repeat(100) {
            item {
                FocusableItem(it, ::handleKeyPreview)
            }
        }
    }
}


@ExperimentalComposeUiApi
@Composable
fun examplePhone() {
    val focusManager = LocalFocusManager.current
    Column {
        Row {
            Button(onClick = { focusManager.moveFocus(FocusDirection.Up) }) { Text("Up") }
            Button(onClick = { focusManager.moveFocus(FocusDirection.Down) }) { Text("Down") }
        }
        LazyColumn {
            repeat(100) {
                item {
                    FocusableItem(it, handler = { false })
                }
            }
        }
    }
}


@Composable
fun FocusableItem(i: Int, handler: (KeyEvent) -> Boolean) {
    val requester = remember { FocusRequester() }
    TextField(
        modifier = Modifier
            .focusRequester(requester)
            .onPreviewKeyEvent(onPreviewKeyEvent = handler),
        value = "$i", onValueChange = {})
}