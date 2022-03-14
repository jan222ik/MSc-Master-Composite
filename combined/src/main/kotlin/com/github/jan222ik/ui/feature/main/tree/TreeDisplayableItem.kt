package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@ExperimentalFoundationApi
abstract class TreeDisplayableItem(
    open val level: Int
) {
    abstract val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
    abstract val onDoublePrimaryAction: MouseClickScope.() -> Unit
    abstract val onSecondaryAction: MouseClickScope.() -> Unit
    abstract val displayName: String
    abstract val canExpand: Boolean

    var children: List<TreeDisplayableItem> by mutableStateOf(emptyList())

    fun addChild(item: TreeDisplayableItem) {
        children = children + item
    }
}