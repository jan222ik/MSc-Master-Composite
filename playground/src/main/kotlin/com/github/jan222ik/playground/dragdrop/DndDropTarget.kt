package com.github.jan222ik.playground.dragdrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun Modifier.dndDropTarget(
    handler: DnDHandler,
    dropActions: DnDAction
): Modifier {
    var state by remember { mutableStateOf<LayoutCoordinates?>(null) }
    return this.then(Modifier.onGloballyPositioned {
        if (state != it) {
            val list = handler.dropTargets.toMutableList()
            state?.to(dropActions)?.let(list::remove)
            list += it to dropActions
            handler.dropTargets = list
            state = it
        }
    })
}