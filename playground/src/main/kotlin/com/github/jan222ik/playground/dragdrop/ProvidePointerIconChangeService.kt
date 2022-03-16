package com.github.jan222ik.playground.dragdrop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

val LocalPointerOverrideService = staticCompositionLocalOf<MutableState<PointerIcon?>> { error("Not provided in composition") }

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