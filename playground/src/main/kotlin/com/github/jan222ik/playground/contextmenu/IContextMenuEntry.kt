package com.github.jan222ik.playground.contextmenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.ui.graphics.vector.ImageVector

sealed class IContextMenuEntry(
    val command: ICommand?,
    val hasSubmenu: Boolean
) {
    abstract val icon: ImageVector?
    abstract val name: String

    fun isEnabled() = command?.isActive() ?: hasSubmenu


    data class NestedContextMenuEntry(
        override val icon: ImageVector? = null,
        override val name: String,
        val content: List<IContextMenuEntry>
    ) : IContextMenuEntry(
        command = null,
        hasSubmenu = true
    )

    class ContextMenuEntry(
        override val icon: ImageVector? = null,
        override val name: String,
        command: ICommand
    ) : IContextMenuEntry(
        command = command,
        hasSubmenu = false
    )
}

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

val newPackageContextMenuEntry = IContextMenuEntry.ContextMenuEntry(
    icon = Icons.Filled.CreateNewFolder,
    name = "Package",
    command = NewCommand(type = NewType.PACKAGE)
)

val newFileContextMenuEntry = IContextMenuEntry.ContextMenuEntry(
    icon = Icons.Filled.Create,
    name = "File",
    command = NewCommand(type = NewType.FILE)
)
val newFileMenuEntry = IContextMenuEntry.NestedContextMenuEntry(
    icon = Icons.Filled.Create,
    name = "New",
    content = listOf(
        newPackageContextMenuEntry,
        newFileContextMenuEntry,
    )
)

val newFileMenuEntry2 = IContextMenuEntry.NestedContextMenuEntry(
    icon = Icons.Filled.Create,
    name = "New",
    content = listOf(
        newPackageContextMenuEntry,
        newFileContextMenuEntry,
        newFileMenuEntry
    )
)