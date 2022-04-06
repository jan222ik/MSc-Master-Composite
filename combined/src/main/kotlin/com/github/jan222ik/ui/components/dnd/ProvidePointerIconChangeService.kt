package com.github.jan222ik.ui.components.dnd

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

@Composable
fun ProvidePointerIconChangeService(
    defaultPointer: PointerIcon = PointerIcon(Cursor.getDefaultCursor()),
    content: @Composable BoxScope.() -> Unit
) {
    val icon = remember { mutableStateOf<PointerIcon?>(null) }
    CompositionLocalProvider(
        LocalPointerOverrideService provides icon
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerHoverIcon(
                    icon = icon.value ?: defaultPointer,
                    overrideDescendants = false
                ),
            content = content
        )
    }
}