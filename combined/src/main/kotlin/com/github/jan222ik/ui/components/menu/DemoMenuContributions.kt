package com.github.jan222ik.ui.components.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove

object DemoMenuContributions {
    fun links(hasLink: Boolean) : MenuContribution.Contentful {
        return if (hasLink) {
            MenuContribution.Contentful.NestedMenuItem(
                icon = null,
                displayName = "Link",
                nestedItems = listOf(
                    MenuContribution.Contentful.MenuItem(
                        icon = Icons.Default.Edit,
                        displayName = "Edit Link"
                    ),
                    MenuContribution.Contentful.MenuItem(
                        icon = Icons.Default.Remove,
                        displayName = "Delete Link"
                    )
                )
            )
        } else {
            MenuContribution.Contentful.MenuItem(
                icon = Icons.Default.Add,
                displayName = "Create Link"
            )
        }
    }
}