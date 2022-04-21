package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jan222ik.ui.feature.main.diagram.propertyview.PropertyView
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
@OptIn(ExperimentalFoundationApi::class)
class PropertiesViewComponent(
    val parent: DiagramAreaComponent
) {

    companion object : KLogging() {
        val PROPERTIES_MIN_WIDTH = 24.dp
        val PROPERTIES_EXPAND_POINT_WIDTH = 250.dp
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun render(onToggle: () -> Unit) {
        val isMinimized = remember(parent.hSplitter.positionPercentage) { parent.hSplitter.positionPercentage > 0.99 }
        Surface(
            color = MaterialTheme.colors.background.copy(alpha = ContentAlpha.medium)
        ) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(
                            onClick = onToggle
                        )
                ) {
                    ShowMoreLess(isMinimized = isMinimized, onToggle)
                }
                ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
                    PropertyView(selectedElement = parent.projectTreeHandler.singleSelectedItem)
                }
            }
        }
    }

    @Composable
    fun BoxScope.ShowMoreLess(
        isMinimized: Boolean,
        onToggle: () -> Unit
    ) {
        Layout(modifier = Modifier.align(Alignment.CenterStart), content = {
            Row(
                modifier = Modifier
                    .rotate(-90f)
                    .clickable(
                        onClick = onToggle
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
                Text(text = "Property View")
                Icon(
                    imageVector = imageVector,
                    contentDescription = null
                )
            }
        }) { measurables, constraints ->
            val pRow = measurables.first()
                .measure(constraints.copy(maxHeight = constraints.maxWidth, maxWidth = constraints.maxHeight))
            layout(pRow.height, constraints.maxHeight) {
                pRow.placeRelative(x = (-pRow.width / 2) + pRow.height / 2, y = constraints.maxHeight / 2)
            }
        }
    }
}