package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key

class MenuSeparator(
    override val icon: ImageVector? = null,
    override val displayName: String = "",
    override val command: ICommand? = null,
    override val keyShortcut: List<Key> = emptyList(),
) : IMenuItem {
}