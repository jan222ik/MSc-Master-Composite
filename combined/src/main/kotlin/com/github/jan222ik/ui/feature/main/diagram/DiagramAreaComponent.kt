package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane


@ExperimentalComposeUiApi
@ExperimentalSplitPaneApi
@ExperimentalFoundationApi
class DiagramAreaComponent {

    companion object : KLogging()

    private val canvasComponent = DiagramCanvasComponent(this)
    private val paletteComponent = PaletteComponent(this)
    private val propertiesViewComponent = PropertiesViewComponent(this)

    internal val hSplitter = SplitPaneState(moveEnabled = true, initialPositionPercentage = 1f)
    internal val vSplitter = SplitPaneState(moveEnabled = true, initialPositionPercentage = 1f)

    private val expandToggleForPropViewShortcutAction = ShortcutAction.of(
        key = Key.Two,
        modifierSum = ShortcutAction.KeyModifier.ALT,
        action = {
            if (hSplitter.positionPercentage > 0.99) {
                hSplitter.setToDpFromSecond(PropertiesViewComponent.PROPERTIES_EXPAND_POINT_WIDTH)
            } else {
                hSplitter.setToMax()
            }
            /* consume = */ true
        }
    )

    private val expandToggleForPaletteShortcutAction = ShortcutAction.of(
        key = Key.Three,
        modifierSum = ShortcutAction.KeyModifier.ALT,
        action = {
            if (vSplitter.positionPercentage > 0.97) {
                vSplitter.setToDpFromSecond(PaletteComponent.PALETTE_EXPAND_POINT_HEIGHT)
            } else {
                vSplitter.setToMax()
            }
            /* consume = */ true
        }
    )

    @Composable
    fun render(projectTreeHandler: ProjectTreeHandler) {
        val hSplitterRem = remember { hSplitter }
        val vSplitterRem = remember { vSplitter }
        val expandToggleForPropViewShortcutActionRem = remember { expandToggleForPropViewShortcutAction }
        val expandToggleForPaletteShortcutActionRem = remember { expandToggleForPaletteShortcutAction }

        val shortcutActionsHandler = LocalShortcutActionHandler.current
        DisposableEffect(EditorManager.allowEdit.value) {
            shortcutActionsHandler.register(expandToggleForPropViewShortcutActionRem)
            SharedCommands.showHidePropertiesView = expandToggleForPropViewShortcutActionRem
            if (EditorManager.allowEdit.value) {
                shortcutActionsHandler.register(expandToggleForPaletteShortcutActionRem)
            } else {
                vSplitterRem.setToMax()
            }
            SharedCommands.showHidePalette = expandToggleForPaletteShortcutActionRem
            onDispose {
                shortcutActionsHandler.deregister(expandToggleForPropViewShortcutActionRem)
                SharedCommands.showHidePropertiesView = null
                shortcutActionsHandler.deregister(expandToggleForPaletteShortcutActionRem)
                SharedCommands.showHidePalette = null
            }
        }

        HorizontalSplitPane(splitPaneState = hSplitterRem) {
            first() {
                VerticalSplitPane(splitPaneState = vSplitterRem) {
                    first() {
                        canvasComponent.render(projectTreeHandler)
                    }
                    second(
                        minSize = when (EditorManager.allowEdit.value) {
                            true -> PaletteComponent.PALETTE_MIN_HEIGHT
                            false -> 0.dp
                        }
                    ) {
                        if (EditorManager.allowEdit.value) {
                            paletteComponent.render(
                                onToggle = {
                                    if (vSplitterRem.positionPercentage > 0.97) {
                                        vSplitterRem.setToDpFromSecond(PaletteComponent.PALETTE_EXPAND_POINT_HEIGHT)
                                    } else {
                                        vSplitterRem.setToMax()
                                    }
                                }
                            )
                        } else {
                            Box {}
                        }
                    }
                }
            }
            second(minSize = PropertiesViewComponent.PROPERTIES_MIN_WIDTH) {
                propertiesViewComponent.render(
                    onToggle = {
                        if (hSplitterRem.positionPercentage > 0.99) {
                            hSplitterRem.setToDpFromSecond(PropertiesViewComponent.PROPERTIES_EXPAND_POINT_WIDTH)
                        } else {
                            hSplitterRem.setToMax()
                        }
                    },
                    projectTreeHandler = projectTreeHandler
                )
            }
        }

    }
}