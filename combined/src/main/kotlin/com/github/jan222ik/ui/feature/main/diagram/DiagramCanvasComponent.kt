package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.components.dnd.DnDAction
import com.github.jan222ik.ui.components.dnd.dndDropTarget
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.diagram.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.Chart
import com.github.jan222ik.ui.feature.main.diagram.canvas.grid.intGridRenderer
import com.github.jan222ik.ui.feature.main.diagram.canvas.math.linearFunctionPointProvider
import com.github.jan222ik.ui.feature.main.diagram.canvas.math.linearFunctionRenderer
import com.github.jan222ik.ui.feature.main.diagram.canvas.viewport.Viewport
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.uml.DiagramBlockUIConfig
import com.github.jan222ik.ui.uml.MovableAndResizeableComponent
import com.github.jan222ik.ui.uml.UMLClass
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
class DiagramCanvasComponent(
    val parent: DiagramAreaComponent
) {
    companion object : KLogging()

    @Composable
    fun render() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val tabs = listOf(
                EditorTabs(name = "First diagram", DiagramType.PACKAGE),
                EditorTabs(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                EditorTabs(name = "parametric diagram", DiagramType.PARAMETRIC)
            )
            DiagramTabRow(editorTabs = tabs)
            val maxViewport = remember { mutableStateOf(Viewport(0f, 0f, 1000f, 1000f)) }
            val viewport = remember { mutableStateOf(Viewport(0f, 0f, 100f, 100f)) }
            val items = remember { mutableStateOf(emptyList<MovableAndResizeableComponent>()) }
            var coords by remember { mutableStateOf<Offset>(Offset.Unspecified) }
            val commandStackHandler = LocalCommandStackHandler.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords = it.positionInWindow() }
                    .clipToBounds()
            ) {
                Chart(
                    modifier = Modifier
                        .fillMaxSize()
                        .dndDropTarget(
                            handler = LocalDropTargetHandler.current,
                            dropActions = object : DnDAction {
                                override fun name(): String = "Canvas"
                                override fun drop(pos: IntOffset, data: Any?): Boolean {
                                    return if (data is org.eclipse.uml2.uml.Class) {
                                        logger.debug { "Accept drop" }
                                        val newObj = UMLClass(
                                            umlClass = data,
                                            initUiConfig = DiagramBlockUIConfig(
                                                x = pos.x.minus(coords.x).dp,
                                                y = pos.y.minus(coords.y).dp,
                                                width = 200.dp,
                                                height = 300.dp
                                            ),
                                            onNextUIConfig = { self, old, new ->
                                                logger.debug { "NEW UI CONFIG" }
                                                commandStackHandler.add(
                                                    MoveOrResizeCommand(
                                                        target = self,
                                                        before = old,
                                                        after = new
                                                    )
                                                )
                                            }
                                        ).apply {
                                            deleteSelfCommand = object : RemoveFromDiagramCommand() {
                                                override suspend fun execute(handler: JobHandler) {
                                                    logger.debug { "Delete item from diagram" }
                                                    items.value = items.value - this@apply
                                                }

                                                override suspend fun undo() {
                                                    logger.debug { "Undo: Delete item from diagram" }
                                                    items.value = items.value + this@apply
                                                }
                                            }
                                        }
                                        val addCommand = object : AddToDiagramCommand() {
                                            override suspend fun execute(handler: JobHandler) {
                                                items.value = items.value + newObj
                                            }

                                            override suspend fun undo() {
                                                items.value = items.value - newObj
                                            }
                                        }
                                        commandStackHandler.add(addCommand)
                                        true
                                    } else false
                                }
                            }
                        ),
                    viewport = viewport,
                    maxViewport = maxViewport.value
                ) {
                    grid(intGridRenderer(stepAbscissa = 2))
                    linearFunction(
                        linearFunctionRenderer(
                            lineDrawer = simpleAxisLineDrawer(brush = SolidColor(Color.White)),
                            linearFunctionPointProvider = linearFunctionPointProvider { it }
                        )
                    )
                    linearFunction(
                        linearFunctionRenderer(
                            lineDrawer = simpleAxisLineDrawer(brush = SolidColor(Color.White)),
                            linearFunctionPointProvider = linearFunctionPointProvider { it.div(2) }
                        )
                    )
                }
                items.value.forEach {
                    it.render()
                }
            }
        }
    }

    @Composable
    fun DiagramTabRow(
        editorTabs: List<EditorTabs>
    ) {
        var selectedIdx by remember { mutableStateOf(0) }
        TabRow(selectedIdx) {
            editorTabs.forEachIndexed { idx, it ->
                Text(
                    text = "Tab ${it.name} [${it.type}]",
                    modifier = Modifier.clickable { selectedIdx = idx },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class EditorTabs(
    val name: String,
    val type: DiagramType
)

enum class DiagramType() {
    PACKAGE, PARAMETRIC, BLOCK_DEFINITION
}