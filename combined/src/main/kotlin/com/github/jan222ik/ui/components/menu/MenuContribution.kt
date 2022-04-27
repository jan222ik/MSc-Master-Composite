package com.github.jan222ik.ui.components.menu

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.NotImplementedCommand

sealed class MenuContribution {
    object Separator : MenuContribution()

    sealed class Contentful(
        val icon: ImageVector?,
        val displayName: String,
        val command: ICommand?,
        val keyShortcut: List<Key>,
        val keyShortcutAlreadyExists: Boolean
    ) : MenuContribution() {

        fun isActive(): Boolean = command?.isActive() ?: let { it is NestedMenuItem && it.nestedItems.isNotEmpty() }

        open class NestedMenuItem(
            icon: ImageVector? = null,
            displayName: String,
            val nestedItems: List<MenuContribution>
        ) : Contentful(
            icon = icon,
            displayName = displayName,
            command = null,
            keyShortcut = emptyList(),
            keyShortcutAlreadyExists = false
        )

        open class MenuItem(
            icon: ImageVector? = null,
            displayName: String,
            command: ICommand? = NotImplementedCommand(displayName),
            keyShortcut: List<Key> = emptyList(),
            keyShortcutAlreadyExists: Boolean = false
        ) :
            Contentful(
                icon = icon,
                displayName = displayName,
                command = command,
                keyShortcut = keyShortcut,
                keyShortcutAlreadyExists = keyShortcutAlreadyExists
            )
    }
}