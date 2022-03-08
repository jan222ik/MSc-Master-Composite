package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.ui.graphics.vector.ImageVector

class MenuItem(
    override val icon: ImageVector?,
    override val displayName: String,
    override val command: ICommand
) : IMenuItem