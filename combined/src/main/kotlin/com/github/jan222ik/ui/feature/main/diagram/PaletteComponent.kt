package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.main.diagram.paletteview.PaletteView
import com.github.jan222ik.ui.value.EditorColors
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalSplitPaneApi
class PaletteComponent(
    val parent: DiagramAreaComponent
) {
    companion object {
        val PALETTE_MIN_HEIGHT = 25.dp
        val PALETTE_EXPAND_POINT_HEIGHT = 250.dp
    }

    @Composable
    fun render(onToggle: () -> Unit) {
        val isMinimized = remember(parent.vSplitter.positionPercentage) { parent.vSplitter.positionPercentage > 0.97 }
        Surface(
            color = EditorColors.backgroundGray
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth()) {
                    ShowMoreLess(isMinimized = isMinimized, onToggle)
                }
                EditorManager.activeEditorTab.value?.let { PaletteView(activeEditorTab = it) } ?: Text("No editor selected")
            }
        }
    }

    @Composable
    fun BoxScope.ShowMoreLess(
        isMinimized: Boolean,
        onToggle: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable(
                    onClick = {
                        onToggle.invoke()
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val imageVector = when (isMinimized) {
                true -> Icons.Filled.ExpandLess
                false -> Icons.Filled.ExpandMore
            }
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
            Text(text = "Palette")
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )
        }
    }
}