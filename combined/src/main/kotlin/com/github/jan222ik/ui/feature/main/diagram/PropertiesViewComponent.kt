package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jan222ik.ui.feature.main.diagram.propertyview.PropertyView
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import com.github.jan222ik.ui.feature.main.tree.ToolWindowToolbar
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.util.VerticalDivider
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
    fun render(onToggle: () -> Unit, projectTreeHandler: ProjectTreeHandler) {
        val isMinimized = remember(parent.hSplitter.positionPercentage) { parent.hSplitter.positionPercentage > 0.99 }
        ProvideTextStyle(LocalTextStyle.current.copy(fontSize = 14.sp)) {
            Row(Modifier.fillMaxSize()) {
                VerticalDivider(modifier = Modifier.fillMaxHeight(), color = EditorColors.dividerGray)
                Column(Modifier.fillMaxHeight()) {
                    val selectedElement = projectTreeHandler.singleSelectedItem.value
                    ToolWindowToolbar(
                        isMinimized = isMinimized,
                        onMinimizeRequest = {
                            parent.hSplitter.setToMax()
                        },
                        title = "Properties: ${selectedElement?.getType() ?: ""}",
                        toolbarContent = {},
                        closeBeforeContent = true
                    )
                    Layout(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        content = {
                            Box {
                                PropertyView(selectedElement = selectedElement)
                            }
                            VerticalDivider(
                                modifier = Modifier.fillMaxHeight(),
                                color = EditorColors.dividerGray
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                            ) {
                                ShowMoreLess(isMinimized = isMinimized, onToggle)
                            }
                        }
                    ) { measureables, constraints ->
                        val pDivider = measureables[1].measure(constraints.copy(minWidth = 1))
                        val pShowMoreLess = measureables[2].measure(constraints)
                        val pProperties = measureables[0].measure(
                            Constraints.fixed(
                                width = constraints.maxWidth.minus(pDivider.width)
                                    .minus(pShowMoreLess.width).coerceAtLeast(0),
                                height = constraints.maxHeight
                            )
                        )
                        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
                            pShowMoreLess.placeRelative(IntOffset.Zero)
                            pDivider.placeRelative(x = pShowMoreLess.width, y = 0)
                            pProperties.placeRelative(x = pShowMoreLess.width + pDivider.width, y = 0)
                        }

                    }

                }
            }
        }
    }

    @Composable
    fun BoxScope.ShowMoreLess(
        isMinimized: Boolean,
        onToggle: () -> Unit
    ) {
        Layout(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .background(EditorColors.backgroundGray),
            content = {
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