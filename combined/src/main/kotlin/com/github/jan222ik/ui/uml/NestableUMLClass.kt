package com.github.jan222ik.ui.uml

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.components.menu.DemoMenuContributions
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.util.HorizontalDivider
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Element

class NestableUMLClass(
    val umlClass: Class,
    initBoundingRect: BoundingRectState,
    onNextUIConfig: (MovableAndResizeableComponent, BoundingRectState, BoundingRectState) -> Unit,
    val nestedContent: Pair<List<MovableAndResizeableComponent>, List<Arrow>>

) : MovableAndResizeableComponent(
    initBoundingRect, onNextUIConfig
) {
    lateinit var deleteSelfCommand: RemoveFromDiagramCommand

    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler) {
        Column {
            Text(text = umlClass.label ?: umlClass.name, Modifier.align(Alignment.CenterHorizontally))
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.Black)
            nestedContent.first.forEach {
                FileTree.treeHandler.value?.let { it1 -> it.render(it1, offset = Offset.Zero) }
            }
        }
    }

    override fun getMenuContributions(): List<MenuContribution> {
        return listOf(
            DemoMenuContributions.links(hasLink = false),
            MenuContribution.Contentful.MenuItem(
                displayName = "Delete from Diagram",
                command = deleteSelfCommand
            )
        )
    }

    override fun showsElement(element: Element?): Boolean {
        return umlClass == element
    }
}