package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.tree.ModelTreeItem

object DemoMenuContributions {
    fun links(hasLink: Boolean): MenuContribution.Contentful {
        return if (hasLink) {
            MenuContribution.Contentful.NestedMenuItem(
                icon = null,
                displayName = "Link",
                nestedItems = listOf(
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaImgVector(Icons.Default.Edit),
                        displayName = "Edit Link"
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = DrawableIcon.viaImgVector(Icons.Default.Remove),
                        displayName = "Delete Link"
                    )
                )
            )
        } else {
            MenuContribution.Contentful.MenuItem(
                icon = DrawableIcon.viaImgVector(Icons.Default.Add),
                displayName = "Create Link"
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun diagramContributionsFor(item: ModelTreeItem): MenuContribution.Contentful {
        val isPackage = item is ModelTreeItem.PackageItem
        val isBlock = item is ModelTreeItem.ClassItem
        return MenuContribution.Contentful.NestedMenuItem(
            icon = null,
            displayName = "New diagram",
            nestedItems = kotlin.run {
                if (isPackage) {
                    listOf(
                        MenuContribution.Contentful.MenuItem(
                            icon = DrawableIcon.viaPainterConstruction { DiagramType.PACKAGE.iconAsPainter() },
                            displayName = DiagramType.PACKAGE.name
                        ),
                        MenuContribution.Contentful.MenuItem(
                            icon = DrawableIcon.viaPainterConstruction { DiagramType.BLOCK_DEFINITION.iconAsPainter() },
                            displayName = DiagramType.BLOCK_DEFINITION.name
                        )
                    )
                } else if (isBlock) {
                    listOf(
                        MenuContribution.Contentful.MenuItem(
                            icon = DrawableIcon.viaPainterConstruction { DiagramType.PARAMETRIC.iconAsPainter() },
                            displayName = DiagramType.PARAMETRIC.name
                        )
                    )
                } else emptyList()
            }
        )
    }
}