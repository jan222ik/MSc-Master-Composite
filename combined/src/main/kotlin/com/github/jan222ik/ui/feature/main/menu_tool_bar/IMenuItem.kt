package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.ui.graphics.vector.ImageVector

interface IMenuItem {
    val icon: ImageVector?
    val displayName: String
    val command: ICommand

    fun isActive(): Boolean = command.isActive()
}