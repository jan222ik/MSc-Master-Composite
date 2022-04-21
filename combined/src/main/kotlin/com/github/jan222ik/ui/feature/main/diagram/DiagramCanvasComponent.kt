package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.canvas.Chart
import com.github.jan222ik.canvas.grid.intGridRenderer
import com.github.jan222ik.canvas.math.linearFunctionPointProvider
import com.github.jan222ik.canvas.math.linearFunctionRenderer
import com.github.jan222ik.canvas.viewport.Viewport
import com.github.jan222ik.model.command.commands.AddToDiagramCommand
import com.github.jan222ik.model.command.commands.MoveOrResizeCommand
import com.github.jan222ik.model.command.commands.RemoveFromDiagramCommand
import com.github.jan222ik.ui.components.dnd.DnDAction
import com.github.jan222ik.ui.components.dnd.dndDropTarget
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.uml.DiagramBlockUIConfig
import com.github.jan222ik.ui.uml.MovableAndResizeableComponent
import com.github.jan222ik.ui.uml.UMLClass
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.HorizontalDivider
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
                EditorTabs(name = "parametric diagram", DiagramType.PARAMETRIC),
                EditorTabs(name = "First diagram", DiagramType.PACKAGE),
                EditorTabs(name = "a", DiagramType.BLOCK_DEFINITION),
                EditorTabs(name = "parametric diagram", DiagramType.PARAMETRIC),
                EditorTabs(name = "First diagram", DiagramType.PACKAGE),
                EditorTabs(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                EditorTabs(name = "parametric diagram", DiagramType.PARAMETRIC),
                EditorTabs(name = "First diagram", DiagramType.PACKAGE),
                EditorTabs(name = "block diagram", DiagramType.BLOCK_DEFINITION),
                EditorTabs(name = "parametric diagram", DiagramType.PARAMETRIC),
            )
            DiagramTabRow(editorTabs = tabs)
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
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
                                                width = 255.dp,
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
                    maxViewport = maxViewport.value,
                    enableZoom = true
                ) {
                    grid(intGridRenderer(stepAbscissa = 20, stepOrdinate = 20))
                    linearFunction(
                        linearFunctionRenderer(
                            lineDrawer = simpleAxisLineDrawer(brush = SolidColor(Color.White)),
                            linearFunctionPointProvider = linearFunctionPointProvider { it }
                        )
                    )
                }
                items.value.forEach {
                    it.render(projectTreeHandler = parent.projectTreeHandler)
                }
            }
        }
    }

    @Composable
    fun DiagramTabRow(
        editorTabs: List<EditorTabs>
    ) {
        var selectedIdx by remember { mutableStateOf(0) }
        ScrollableTabRow(
            selectedTabIndex = selectedIdx,
            backgroundColor = EditorColors.backgroundGray,
            edgePadding = TabRowDefaults.ScrollableTabRowPadding.div(2)
        ) {
            editorTabs.forEachIndexed { idx, it ->
                Row(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 90.dp, minHeight = 24.dp)
                        .clickable { selectedIdx = idx }
                        .then(
                            if (idx == selectedIdx) {
                                Modifier.background(Color.White)
                            } else Modifier
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {

                    Image(
                        painter = when (it.type) {
                            DiagramType.PACKAGE -> painterResource("drawables/uml_icons/Diagram_SysML_Package.gif")
                            DiagramType.PARAMETRIC -> painterResource("drawables/uml_icons/Diagram_Parametric.png")
                            DiagramType.BLOCK_DEFINITION -> painterResource("drawables/uml_icons/Diagram_BlockDefinition.gif")
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = it.name,
                        textAlign = TextAlign.Center
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close editor tab",
                        modifier = Modifier.size(16.dp).clickable {
                            logger.debug { "Close tab clicked - TODO: Impl Close" } // TODO Impl close
                        },
                        tint = EditorColors.dividerGray
                    )
                }
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