package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key

class SubmenuItem(
    override val icon: ImageVector?,
    override val displayName: String,
    val items: List<IMenuItem>
) : IMenuItem {
    override val command: ICommand? = null
    override val keyShortcut: List<Key> = emptyList()

}