package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane

@ExperimentalComposeUiApi
@ExperimentalSplitPaneApi
class DiagramAreaComponent {

    companion object : KLogging()

    private val canvasComponent = DiagramCanvasComponent(this)
    private val paletteComponent = PaletteComponent(this)
    private val propertiesViewComponent = PropertiesViewComponent(this)

    internal val hSplitter = SplitPaneState(moveEnabled = true, initialPositionPercentage = 1f)
    internal val vSplitter = SplitPaneState(moveEnabled = true, initialPositionPercentage = 1f)

    @Composable
    fun render() {
        HorizontalSplitPane(splitPaneState = hSplitter) {
            first() {
                VerticalSplitPane(splitPaneState = vSplitter) {
                    first() { canvasComponent.render() }
                    second(minSize = PaletteComponent.PALETTE_MIN_HEIGHT) {
                        paletteComponent.render()
                    }
                }
            }
            second(minSize = PropertiesViewComponent.PROPERTIES_MIN_WIDTH) {
                propertiesViewComponent.render()
            }
        }
    }
}