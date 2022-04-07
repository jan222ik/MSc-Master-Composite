package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.NotImplementedCommand
import com.github.jan222ik.model.mock.MockBackgroundJobs
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.components.menu.MenuContribution.Contentful.MenuItem
import com.github.jan222ik.ui.components.menu.MenuContribution.Contentful.NestedMenuItem

@OptIn(ExperimentalComposeUiApi::class)
object MenuBarContents {
    fun fileMenu(
        newCommand: ICommand,
        openExistingProjectCommand: ICommand
    ) = listOf(
        NestedMenuItem(
            icon = null,
            displayName = "New",
            nestedItems = listOf(
                MenuItem(
                    icon = null,
                    displayName = "Create new project",
                    command = newCommand
                )
            )
        ),
        MenuItem(
            icon = null,
            displayName = "Open existing project",
            command = openExistingProjectCommand
        ),
        MenuItem(
            icon = null,
            displayName = "Create mock background jobs",
            command = MockBackgroundJobs()
        ),
        MenuContribution.Separator,
        MenuItem(
            icon = null,
            displayName = "Close Editor",
            command = NotImplementedCommand("Close Editor")
        ),
        MenuItem(
            icon = null,
            displayName = "Close All Editor",
            command = NotImplementedCommand("Close All Editor")
        ),
        MenuContribution.Separator,
        MenuItem(
            icon = null,
            displayName = "Save",
            command = NotImplementedCommand("Save")
        ),
        kotlin.run {
            val displayName = "Save As..."
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Save All"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Revert"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Move..."
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Rename..."
            MenuItem(
                icon = Icons.Filled.Edit,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Refresh"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Restart"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Exit"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        NestedMenuItem(
            icon = null,
            displayName = "New 2",
            nestedItems = MenuBarContents.fileMenu2(newCommand, openExistingProjectCommand)
        ),
    )

    fun editMenu() = listOf(
        kotlin.run {
            val displayName = "Undo"
            MenuItem(
                icon = Icons.Filled.Undo,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.Z)
            )
        },
        kotlin.run {
            val displayName = "Redo"
            MenuItem(
                icon = Icons.Filled.Redo,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.ShiftLeft, Key.Z)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Cut"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.X)
            )
        },
        kotlin.run {
            val displayName = "Copy"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.C)
            )
        },
        kotlin.run {
            val displayName = "Paste"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.V)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Delete"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.Delete)
            )
        },
        kotlin.run {
            val displayName = "Select all"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName),
                keyShortcut = listOf(Key.CtrlLeft, Key.A)
            )
        }
    )

    fun fileMenu2(
        newCommand: ICommand,
        openExistingProjectCommand: ICommand
    ) = listOf(
        NestedMenuItem(
            icon = null,
            displayName = "New",
            nestedItems = listOf(
                MenuItem(
                    icon = null,
                    displayName = "Create new project",
                    command = newCommand
                )
            )
        ),
        MenuItem(
            icon = null,
            displayName = "Open existing project",
            command = openExistingProjectCommand
        ),
        MenuItem(
            icon = null,
            displayName = "Create mock background jobs",
            command = MockBackgroundJobs()
        ),
        MenuContribution.Separator,
        MenuItem(
            icon = null,
            displayName = "Close Editor",
            command = NotImplementedCommand("Close Editor")
        ),
        MenuItem(
            icon = null,
            displayName = "Close All Editor",
            command = NotImplementedCommand("Close All Editor")
        ),
        MenuContribution.Separator,
        MenuItem(
            icon = null,
            displayName = "Save",
            command = NotImplementedCommand("Save")
        ),
        kotlin.run {
            val displayName = "Save As..."
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Save All"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Revert"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Move..."
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Rename..."
            MenuItem(
                icon = Icons.Filled.Edit,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Refresh"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        MenuContribution.Separator,
        kotlin.run {
            val displayName = "Restart"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
        kotlin.run {
            val displayName = "Exit"
            MenuItem(
                icon = null,
                displayName = displayName,
                command = NotImplementedCommand(displayName)
            )
        },
    )
}