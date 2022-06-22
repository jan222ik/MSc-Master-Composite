package com.github.jan222ik.ui.components.dnd

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import mu.KotlinLogging

@Composable
fun Modifier.dndDropTarget(
    handler: DnDHandler,
    dropActions: DnDAction
): Modifier {
    var state by remember(dropActions.name) { mutableStateOf<LayoutCoordinates?>(null) }
    val mod = remember(dropActions.name) {
        handler.dropTargets.value = handler.dropTargets.value.toMutableList().filterNot { it.second.name == dropActions.name }
        Modifier.onGloballyPositioned {
        if (state != it) {
            val list = handler.dropTargets.value.toMutableList()
            state?.to(dropActions)?.let(list::remove)
            list.add(0, it to dropActions)
            handler.dropTargets.value = list
            state = it
        }
    } }
    return this.then(mod)
}