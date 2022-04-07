package com.github.jan222ik.playground.contextmenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.ui.graphics.vector.ImageVector


enum class NewType {
    FILE, PACKAGE, CLASS, PROPERTY
}


class NewCommand(val type: NewType) : ICommand {
    override fun isActive(): Boolean = true

    override suspend fun execute() {
        println("New of type $type")
    }

    override fun canUndo(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun undo() {
        TODO("Not yet implemented")
    }
}

val newPackageContextMenuEntry = MenuContribution.Contentful.MenuItem(
    icon = Icons.Filled.CreateNewFolder,
    displayName = "Package",
    command = NewCommand(type = NewType.PACKAGE)
)

val newFileContextMenuEntry = MenuContribution.Contentful.MenuItem(
    icon = Icons.Filled.Create,
    displayName = "File",
    command = NewCommand(type = NewType.FILE)
)
val newFileMenuEntry = MenuContribution.Contentful.NestedMenuItem(
    icon = Icons.Filled.Create,
    displayName = "New",
    nestedItems = listOf(
        newPackageContextMenuEntry,
        newFileContextMenuEntry,
    )
)

val newFileMenuEntry2 = MenuContribution.Contentful.NestedMenuItem(
    icon = Icons.Filled.Create,
    displayName = "New",
    nestedItems = listOf(
        newPackageContextMenuEntry,
        newFileContextMenuEntry,
        newFileMenuEntry
    )
)