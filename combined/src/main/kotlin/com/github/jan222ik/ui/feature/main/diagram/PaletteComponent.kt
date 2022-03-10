package com.github.jan222ik.ui.feature.main.diagram

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
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

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
    fun render() {
        val isMinimized = remember(parent.vSplitter.positionPercentage) { parent.vSplitter.positionPercentage > 0.97 }
        Surface(
            color = Color.Gray
        ) {
            Box(Modifier.fillMaxSize()) {
                ShowMoreLess(isMinimized = isMinimized)
            }
        }
    }

    @Composable
    fun BoxScope.ShowMoreLess(
        isMinimized: Boolean
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable(
                    onClick = {
                        if (isMinimized) {
                            parent.vSplitter.setToDpFromSecond(PALETTE_EXPAND_POINT_HEIGHT)
                        } else {
                            parent.vSplitter.setToMax()
                        }
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