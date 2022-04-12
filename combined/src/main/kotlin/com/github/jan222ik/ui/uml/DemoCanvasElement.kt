package com.github.jan222ik.ui.uml

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand

class DemoCanvasElement(
    uiConfig: DiagramBlockUIConfig,
    onNextUIConfig: (self: MovableAndResizeableComponent, old: DiagramBlockUIConfig, new: DiagramBlockUIConfig) -> Unit
) : MovableAndResizeableComponent(uiConfig, onNextUIConfig) {
    @Composable
    override fun content() {
        Text("Test [Selected:${this@DemoCanvasElement.selected}]")
    }

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
                uiConfig = DiagramBlockUIConfig(
                    x = 100.dp,
                    y = 45.dp,
                    width = 100.dp,
                    height = 200.dp
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
        movableAndResizeableComponent.render()

    }
}
