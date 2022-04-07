package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

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