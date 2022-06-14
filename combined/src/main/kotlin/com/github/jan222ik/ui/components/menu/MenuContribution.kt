package com.github.jan222ik.ui.components.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.NotImplementedCommand

sealed class DrawableIcon {
    data class viaPainterConstruction(val painter: @Composable () -> Painter) : DrawableIcon()
    data class viaImgVector(val vector: ImageVector) : DrawableIcon()
    companion object {
        val Property = viaPainterConstruction { painterResource("drawables/uml_icons/Property.gif") }
        val Block = viaPainterConstruction { painterResource("drawables/uml_icons/Block.gif") }
    }
}

fun ImageVector.toDrawableIcon(): DrawableIcon {
    return DrawableIcon.viaImgVector(this)
}

sealed class MenuContribution {
    object Separator : MenuContribution()

    sealed class Contentful(
        val icon: DrawableIcon?,
        val displayName: String,
        val command: ICommand?,
        val keyShortcut: List<Key>,
        val keyShortcutAlreadyExists: Boolean
    ) : MenuContribution() {

        fun isActive(): Boolean = command?.isActive() ?: let { it is NestedMenuItem && it.nestedItems.isNotEmpty() }

        open class NestedMenuItem(
            icon: DrawableIcon? = null,
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
            icon: DrawableIcon? = null,
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