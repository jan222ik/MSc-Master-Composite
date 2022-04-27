package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.SharedCommands
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler
import mu.KLogging
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.VerticalSplitPane

val LocalActiveEditorTab = compositionLocalOf<MutableState<EditorTabViewModel?>> { error("Not provided") }

@ExperimentalComposeUiApi
@ExperimentalSplitPaneApi
@ExperimentalFoundationApi
class DiagramAreaComponent(
    val projectTreeHandler: ProjectTreeHandler
) {

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
            println("showHidePalette action")
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
    fun render() {
        val hSplitterRem = remember { hSplitter }
        val vSplitterRem = remember { vSplitter }
        val expandToggleForPropViewShortcutActionRem = remember { expandToggleForPropViewShortcutAction }
        val expandToggleForPaletteShortcutActionRem = remember { expandToggleForPaletteShortcutAction }

        val shortcutActionsHandler = LocalShortcutActionHandler.current
        DisposableEffect(Unit) {
            shortcutActionsHandler.register(expandToggleForPropViewShortcutActionRem)
            SharedCommands.showHidePropertiesView = expandToggleForPropViewShortcutActionRem
            shortcutActionsHandler.register(expandToggleForPaletteShortcutActionRem)
            SharedCommands.showHidePalette = expandToggleForPaletteShortcutActionRem
            onDispose {
                shortcutActionsHandler.deregister(expandToggleForPropViewShortcutActionRem)
                SharedCommands.showHidePropertiesView = null
                shortcutActionsHandler.deregister(expandToggleForPaletteShortcutActionRem)
                SharedCommands.showHidePalette = null
            }
        }
        val activeEditorTab = remember { mutableStateOf<EditorTabViewModel?>(null) }
        CompositionLocalProvider(
            LocalActiveEditorTab provides activeEditorTab
        ) {
            HorizontalSplitPane(splitPaneState = hSplitterRem) {
                first() {
                    VerticalSplitPane(splitPaneState = vSplitterRem) {
                        first() {
                            canvasComponent.render()
                        }
                        second(minSize = PaletteComponent.PALETTE_MIN_HEIGHT) {
                            paletteComponent.render(
                                onToggle = {
                                    if (vSplitterRem.positionPercentage > 0.97) {
                                        vSplitterRem.setToDpFromSecond(PaletteComponent.PALETTE_EXPAND_POINT_HEIGHT)
                                    } else {
                                        vSplitterRem.setToMax()
                                    }
                                }
                            )
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
                        }
                    )
                }
            }
        }
    }
}