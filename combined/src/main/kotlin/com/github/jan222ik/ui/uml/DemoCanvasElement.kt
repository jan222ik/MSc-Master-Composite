package com.github.jan222ik.ui.uml

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.ui.adjusted.BoundingRectState
import com.github.jan222ik.ui.adjusted.MovableAndResizeableComponent
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import org.eclipse.uml2.uml.Element

class DemoCanvasElement(
    uiConfig: BoundingRectState,
    onNextUIConfig: (self: MovableAndResizeableComponent, old: BoundingRectState, new: BoundingRectState) -> Unit
) : MovableAndResizeableComponent(uiConfig, onNextUIConfig) {
    @Composable
    override fun content(projectTreeHandler: ProjectTreeHandler) {
        Text("Test [Selected:${this@DemoCanvasElement.selected}]")
    }

    override fun getMenuContributions(): List<MenuContribution> {
        return emptyList()
    }

    override fun showsElement(element: Element?): Boolean = false

}

fun main() {
    singleWindowApplication(focusable = true) {
        if (Thread.currentThread().name == "AWT-EventQueue-0") {
            Thread.currentThread().name = "AWT-EQ-0"
        }
        val scope = rememberCoroutineScope()
        val commandStack = remember { CommandStackHandler(scope = scope) }
        Row {
            Button(
                enabled = commandStack.hasUndo,
                onClick = {
                    commandStack.undo()
                }
            ) {
                Text("Undo")
            }
            Button(
                enabled = commandStack.hasRedo,
                onClick = {
                    commandStack.redo()
                }
            ) {
                Text("Redo")
            }
            Column {
                commandStack.commandStack.value.forEach { Text(it.toString()) }
                Text("IDX: ${commandStack.commandStackIdx.value}")
            }
        }

        val movableAndResizeableComponent = remember {
            DemoCanvasElement(
                uiConfig = BoundingRectState(
                    topLeftPacked = packFloats(
                        100f,
                        45f
                    ),
                    width = 100f,
                    height = 200f
                ),
                onNextUIConfig = { self, old, new ->
                    val moveOrResizeCommand = MoveOrResizeCommand(
                        target = self,
                        before = old,
                        after = new
                    )
                    commandStack.add(moveOrResizeCommand)
                }
            )
        }
        movableAndResizeableComponent.render(ProjectTreeHandler(false), offset = Offset.Zero)

    }
}
