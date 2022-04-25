package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@ExperimentalFoundationApi
abstract class TreeDisplayableItem(
    open val level: Int
) {
    abstract val icon: @Composable ((modifier: Modifier) -> Unit)?
    abstract val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
    abstract val onDoublePrimaryAction: MouseClickScope.() -> Unit
    abstract val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
    abstract val displayName: String
    abstract val canExpand: Boolean

    var children: List<TreeDisplayableItem> by mutableStateOf(emptyList())

    fun addChild(item: TreeDisplayableItem) {
        children = children + item
    }
}