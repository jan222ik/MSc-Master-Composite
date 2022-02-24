package com.github.jan222ik.ui.feature

import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import mu.KLogging

class KeyEventHandler(
    applicableSize: DpSize,
    val setWindowPosition: (Alignment) -> Unit,
    val setWindowSize: (DpSize) -> Unit
) {

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
        logger.debug("KeyEvent: $event")
        val nativeKeyCode = event.key.nativeKeyCode
        if (event.type == KeyEventType.KeyDown) {
            down.add(nativeKeyCode)
        } else {
            consume = handleWindowsMoveWindow(nativeKeyCode, windowState)
            down.remove(nativeKeyCode)
        }
        return consume
    }

    private fun handleWindowsMoveWindow(nativeKeyCode: Int, windowState: WindowState): Boolean {
        var consume = false
        if (down.contains(WindowsKeyCode)) {
            if (nativeKeyCode == ArrowLeft) {
                logger.debug("Arrow Left")
                setWindowSize(applicableSizeHalfWidth)
                setWindowPosition(Alignment.TopStart)
            }
            if (nativeKeyCode == ArrowRight) {
                logger.debug("Arrow Right")
                setWindowSize(applicableSizeHalfWidth)
                setWindowPosition(Alignment.TopEnd)
            }
            val wsSize = windowState.size
            val wsPos = windowState.position
            if (nativeKeyCode == ArrowUp) {
                logger.debug("Arrow Up")
                if (wsSize == applicableSizeHalfWidth) {
                    setWindowPosition(if (wsPos.x == 0.dp) Alignment.TopStart else Alignment.TopEnd)
                    setWindowSize(applicableSizeQuarter)
                    consume = true
                } else if (wsSize == applicableSizeQuarter) {
                    if (wsPos.y != 0.dp) {
                        setWindowSize(applicableSizeHalfWidth)
                        setWindowPosition(if (wsPos.x == 0.dp) Alignment.TopStart else Alignment.TopEnd)
                        consume = true
                    }
                }
            }
            if (nativeKeyCode == ArrowDown) {
                logger.debug("Arrow Down")
                if (wsSize == applicableSizeHalfWidth) {
                    setWindowPosition(if (wsPos.x == 0.dp) Alignment.BottomStart else Alignment.BottomEnd)
                    setWindowSize(applicableSizeQuarter)
                    consume = true
                } else if (wsSize == applicableSizeQuarter) {
                    if (wsPos.y == 0.dp) {
                        setWindowSize(applicableSizeHalfWidth)
                        setWindowPosition(if (wsPos.x == 0.dp) Alignment.TopStart else Alignment.TopEnd)
                        consume = true
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
}