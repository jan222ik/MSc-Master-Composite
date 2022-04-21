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
import com.github.jan222ik.ui.value.EditorColors

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
                true -> EditorColors.backgroundGray.takeUnless { isHover } ?: EditorColors.closeBtn
                else -> EditorColors.backgroundGray.takeUnless { isHover } ?: EditorColors.backgroundGray.copy(
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