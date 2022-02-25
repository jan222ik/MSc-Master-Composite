package com.github.jan222ik.ui.feature.main.keyevent

import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import mu.KLogging

class KeyEventHandler(
    val applicableSize: DpSize,
    val setWindowPosition: (Alignment) -> Unit,
    val setWindowSize: (DpSize) -> Unit,
    val setPlacement: (WindowPlacement) -> Unit
) : ShortcutActionsHandler {

    companion object : KLogging() {
        const val WindowsKeyCode = 524
        const val ArrowLeft = 37
        const val ArrowUp = 38
        const val ArrowRight = 39
        const val ArrowDown = 40
    }

    private val down = mutableSetOf<Int>()

    private val applicableSizeHalfWidth = applicableSize.half(w = true)
    private val applicableSizeQuarter = applicableSize.half(w = true, h = true)

    fun handleKeyEvent(event: KeyEvent, windowState: WindowState): Boolean {
        var consume = false
        logger.trace("KeyEvent: $event")
        val nativeKeyCode = event.key.nativeKeyCode
        if (event.type == KeyEventType.KeyDown) {
            down.add(nativeKeyCode)
            processKeyEvent(
                keyDownActions[event.key.nativeKeyCode] ?: emptyList(),
                getKeyModifiers(event),
                event
            )
        } else {
            consume = handleWindowsWindowMovement(nativeKeyCode, windowState)
            if (!consume) processKeyEvent(
                keyReleaseActions[event.key.nativeKeyCode] ?: emptyList(),
                getKeyModifiers(event),
                event
            )
            down.remove(nativeKeyCode)
        }
        return consume
    }

    private fun handleWindowsWindowMovement(nativeKeyCode: Int, windowState: WindowState): Boolean {
        if (!down.contains(WindowsKeyCode)) return false
        var consume = false
        fun makeHalfAligned(toLeft: Boolean) {
            setPlacement(WindowPlacement.Floating)
            setWindowSize(applicableSizeHalfWidth)
            setWindowPosition(
                if (toLeft) Alignment.TopStart else Alignment.TopEnd
            )
            consume = true
        }
        fun makeQuarterAligned(toLeft: Boolean, toTop: Boolean) {
            setPlacement(WindowPlacement.Floating)
            setWindowSize(applicableSizeQuarter)
            setWindowPosition(
                when {
                    toTop && toLeft -> Alignment.TopStart
                    toTop && !toLeft -> Alignment.TopEnd
                    !toTop && toLeft -> Alignment.BottomStart
                    else /* !isTop && !isLeft */ -> Alignment.BottomEnd
                }
            )
            consume = true
        }
        fun makeFull() {
            setPlacement(WindowPlacement.Floating)
            setWindowSize(applicableSize)
            setWindowPosition(Alignment.TopStart)
            consume = true
        }
        val isHalf = windowState.size == applicableSizeHalfWidth
        val isQuarter = windowState.size == applicableSizeQuarter
        val isFull = !(isHalf || isQuarter)
        val isTop = windowState.position.y == Dp.Unspecified
        val isBottom = windowState.position.y == 0.dp
        val isLeft = windowState.position.x == Dp.Unspecified
        val isRight = windowState.position.y == 0.dp
        val logStr = "Placement: ${windowState.placement}, Size[h:$isHalf,q:$isQuarter,f:$isFull], Pos[t:$isTop,b:$isBottom,l:$isLeft,r:$isRight]"
        when (nativeKeyCode) {
            ArrowLeft -> {
                logger.trace { "Left  > $logStr" }
                when {
                    isFull -> {
                        makeHalfAligned(toLeft = true)
                    }
                    isHalf -> {
                        when {
                            isLeft -> { /* TODO Move Monitor */ }
                            isRight -> makeFull()
                        }
                    }
                    else -> {
                        when {
                            isLeft -> { /* TODO Move Monitor */ }
                            isRight -> makeQuarterAligned(toLeft = true, toTop = isTop)
                            else -> makeHalfAligned(toLeft = true)
                        }
                    }
                }
            }
            ArrowRight -> {
                logger.trace { "Right > $logStr" }
                when {
                    isFull -> {
                        makeHalfAligned(toLeft = false)
                    }
                    isHalf -> {
                        when {
                            isLeft -> makeFull()
                            isRight -> { /* TODO Move Monitor */ }
                        }
                    }
                    else -> {
                        when {
                            isLeft -> makeQuarterAligned(toLeft = false, toTop = isTop)
                            isRight -> { /* TODO Move Monitor */ }
                            else -> makeHalfAligned(toLeft = false)
                        }
                    }
                }
            }
            ArrowDown -> {
                logger.trace { "Down  > $logStr" }
                when {
                    isFull -> {
                        /* Minimize */ consume = true
                    }
                    isHalf -> {
                        makeQuarterAligned(toLeft = isLeft, toTop = false)
                    }
                    else -> {
                        when {
                            isTop -> {
                                makeHalfAligned(toLeft = isLeft)
                            }
                            isBottom ->  {
                                /* Minimize */ consume = true
                            }
                        }
                    }
                }
            }
            ArrowUp -> {
                logger.trace { "Up    > $logStr" }
                when {
                    isFull -> {
                        /* Fullscreen */
                    }
                    isHalf -> {
                        makeQuarterAligned(toLeft = isLeft, toTop = true)
                    }
                    else -> {
                        when {
                            isTop -> {
                                makeFull()
                            }
                            isBottom ->  {
                                makeHalfAligned(toLeft = isLeft)
                            }
                        }
                    }
                }
            }
        }

        return consume
    }


    private fun DpSize.half(w: Boolean = false, h: Boolean = false): DpSize {
        return this.copy(
            width = this.width.takeUnless { w } ?: this.width.div(2),
            height = this.height.takeUnless { h } ?: this.height.div(2)
        )
    }

    private fun processKeyEvent(
        keyEventActions: List<ShortcutAction>,
        modifiers: Int = 0,
        event: KeyEvent
    ): Boolean {
        val eventActions =
            keyEventActions.filter { it.modifiers == modifiers || it.modifiers == ShortcutAction.KeyModifier.ANY_MODIFIERS }
        eventActions.forEach {
            if (it.action.invoke()) return true
        }
        return false
    }

    private fun getKeyModifiers(e: KeyEvent): Int {
        var modifier = 0
        if (e.isShiftPressed) modifier += ShortcutAction.KeyModifier.SHIFT
        if (e.isCtrlPressed) modifier += ShortcutAction.KeyModifier.CTRL
        if (e.isAltPressed) modifier += ShortcutAction.KeyModifier.ALT
        if (e.isMetaPressed) modifier += ShortcutAction.KeyModifier.META
        return modifier
    }

    private val keyDownActions = hashMapOf<Int, MutableList<ShortcutAction>>()

    override fun register(action: ShortcutAction): ShortcutAction {
        keyDownActions.getOrPut(action.key.nativeKeyCode, ::ArrayList).add(action)
        return action
    }

    override fun deregister(action: ShortcutAction) {
        keyDownActions[action.key.nativeKeyCode]?.remove(action)
    }

    private val keyReleaseActions = hashMapOf<Int, MutableList<ShortcutAction>>()

    override fun registerOnRelease(action: ShortcutAction): ShortcutAction {
        keyReleaseActions.getOrPut(action.key.nativeKeyCode, ::ArrayList).add(action)
        return action
    }

    override fun deregisterOnRelease(action: ShortcutAction) {
        keyReleaseActions[action.key.nativeKeyCode]?.remove(action)
    }

}