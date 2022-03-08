package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalI18N
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.ComposableSlot
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.stringResource
import com.github.jan222ik.ui.value.R
import de.comahe.i18n4k.Locale
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@ExperimentalComposeUiApi
@ExperimentalSplitPaneApi
class DiagramAreaComponent {

    companion object : KLogging()

    private val canvasComponent = DiagramCanvasComponent(this)
    private val paletteComponent = PaletteComponent(this)
    private val propertiesViewComponent = PropertiesViewComponent(this)

    @Composable
    fun render() {
        DiagramAreaComponentLayout(
            palette = { paletteComponent.render() },
            canvas = { canvasComponent.render() },
            propertyView = { propertiesViewComponent.render() }
        )
    }

    @Composable
    fun DiagramAreaComponentLayout(
        palette: ComposableSlot,
        propertyView: ComposableSlot,
        canvas: ComposableSlot
    ) {
        val hSplitter = rememberSplitPaneState()
        HorizontalSplitPane(splitPaneState = hSplitter) {
            first {
                val vSplitter = rememberSplitPaneState()
                VerticalSplitPane(splitPaneState = vSplitter) {
                    first { canvas.invoke() }
                    second { palette.invoke() }
                }
            }
            second { propertyView.invoke() }
        }
    }
}