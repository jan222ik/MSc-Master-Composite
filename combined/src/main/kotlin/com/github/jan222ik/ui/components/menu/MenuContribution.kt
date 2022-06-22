package com.github.jan222ik.ui.components.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.model.command.commands.NotImplementedCommand
import org.eclipse.uml2.uml.Association
import org.eclipse.uml2.uml.Generalization

sealed class DrawableIcon {
    data class viaPainterConstruction(val painter: @Composable () -> Painter) : DrawableIcon()
    data class viaImgVector(val vector: ImageVector) : DrawableIcon()
    companion object {
        val ConstraintBlock = viaPainterConstruction { painterResource("drawables/uml_icons/ConstraintBlock.gif") }
        val Package = viaPainterConstruction { painterResource("drawables/uml_icons/Package.gif") }
        val PackageImport = viaPainterConstruction { painterResource("drawables/uml_icons/PackageImport.gif") }
        val Property = viaPainterConstruction { painterResource("drawables/uml_icons/Property.gif") }
        val Block = viaPainterConstruction { painterResource("drawables/uml_icons/Block.gif") }
        // General Annotations
        val Comment = viaPainterConstruction { painterResource("drawables/uml_icons/Comment.gif") }
        val Constraint = viaPainterConstruction { painterResource("drawables/uml_icons/Constraint.gif") }
        val ContextLink = viaPainterConstruction { painterResource("drawables/uml_icons/Constraint.gif") }
        val Dependency = viaPainterConstruction { painterResource("drawables/uml_icons/Dependency.gif") }
        val ElementGroup = viaPainterConstruction { painterResource("drawables/uml_icons/ElementGroup.gif") }
        val Link = viaPainterConstruction { painterResource("drawables/uml_icons/Link.gif") }
        val Problem = viaPainterConstruction { painterResource("drawables/uml_icons/Problem.gif") }
        val Rationale = viaPainterConstruction { painterResource("drawables/uml_icons/Rationale.gif") }
        val Realization = viaPainterConstruction { painterResource("drawables/uml_icons/Constraint.gif") }
        val Refine = viaPainterConstruction { painterResource("drawables/uml_icons/Refine.gif") }

        val Abstraction = viaPainterConstruction { painterResource("drawables/uml_icons/Abstraction.gif") }
        val Model = viaPainterConstruction { painterResource("drawables/uml_icons/Model.gif") }
        val Enumeration = viaPainterConstruction { painterResource("drawables/uml_icons/Enumeration.gif") }
        val Generalization = viaPainterConstruction { painterResource("drawables/uml_icons/Generalization.gif") }
        val Association = viaPainterConstruction { painterResource("drawables/uml_icons/Association.gif") }
        val AssociationDirected = viaPainterConstruction { painterResource("drawables/uml_icons/Association_none_directed.gif") }
        val AssociationComposite = viaPainterConstruction { painterResource("drawables/uml_icons/Association_composite_directed.gif") }
        val AssociationShared = viaPainterConstruction { painterResource("drawables/uml_icons/Association_shared_directed.gif") }


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