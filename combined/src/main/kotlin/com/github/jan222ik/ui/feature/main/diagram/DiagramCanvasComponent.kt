package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import com.github.jan222ik.ui.feature.main.diagram.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.ui.feature.main.diagram.canvas.canvas.Chart
import com.github.jan222ik.ui.feature.main.diagram.canvas.grid.intGridRenderer
import com.github.jan222ik.ui.feature.main.diagram.canvas.math.linearFunctionPointProvider
import com.github.jan222ik.ui.feature.main.diagram.canvas.math.linearFunctionRenderer
import com.github.jan222ik.ui.feature.main.diagram.canvas.viewport.Viewport
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalFoundationApi
@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
class DiagramCanvasComponent(
    val parent: DiagramAreaComponent
) {

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
            Chart(
                modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.clickable { selectedIdx = idx},
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