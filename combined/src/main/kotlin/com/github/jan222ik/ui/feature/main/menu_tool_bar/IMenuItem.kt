package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key

interface IMenuItem {
    val icon: ImageVector?
    val displayName: String
    val command: ICommand?
    val keyShortcut: List<Key>

    fun isActive(): Boolean? = command?.isActive()
}