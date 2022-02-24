package org.jetbrains.compose.splitpane

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.cursorForHorizontalResize(isHorizontal: Boolean): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(if (isHorizontal) Cursor.E_RESIZE_CURSOR else Cursor.S_RESIZE_CURSOR)))

@Composable
private fun DesktopSplitPaneSeparator(
    isHorizontal: Boolean,
    color: Color = MaterialTheme.colors.background
) = Box(
    Modifier
        .run {
            if (isHorizontal) {
                this.width(1.dp)
                    .fillMaxHeight()
            } else {
                this.height(1.dp)
                    .fillMaxWidth()
            }
        }
        .background(color)
)

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun DesktopHandle(
    isHorizontal: Boolean,
    splitPaneState: SplitPaneState
) = Box(
    Modifier
        .pointerInput(splitPaneState) {
            detectDragGestures { change, _ ->
                change.consumeAllChanges()
                splitPaneState.dispatchRawMovement(
                    if (isHorizontal) change.position.x else change.position.y
                )
            }
        }
        .cursorForHorizontalResize(isHorizontal)
        .run {
            if (isHorizontal) {
                this.width(8.dp)
                    .fillMaxHeight()
            } else {
                this.height(8.dp)
                    .fillMaxWidth()
            }
        }
)

/**
 * Internal implementation of default splitter
 *
 * @param isHorizontal describes is it horizontal or vertical split pane
 * @param splitPaneState the state object to be used to control or observe the split pane state
 */
@OptIn(ExperimentalSplitPaneApi::class)
internal fun defaultSplitter(
    isHorizontal: Boolean,
    splitPaneState: SplitPaneState
): Splitter = Splitter(
    measuredPart = {
        DesktopSplitPaneSeparator(isHorizontal)
    },
    handlePart = {
        DesktopHandle(isHorizontal, splitPaneState)
    }
)

