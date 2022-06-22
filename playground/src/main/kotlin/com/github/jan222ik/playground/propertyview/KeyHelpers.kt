package com.github.jan222ik.playground.propertyview

import androidx.compose.ui.input.key.*

object KeyHelpers {
    inline fun onKeyDown(evt: KeyEvent, defaultConsume: Boolean = false, onDown: KeyEvent.() -> Boolean): Boolean {
        if (evt.type == KeyEventType.KeyDown) {
            return onDown.invoke(evt)
        }
        return defaultConsume
    }

    inline fun onKeyDown(defaultConsume: Boolean = false, crossinline onDown: KeyEvent.() -> Boolean): (KeyEvent) -> Boolean {
        return { evt ->
            if (evt.type == KeyEventType.KeyDown) {
                onDown.invoke(evt)
            } else defaultConsume
        }
    }

    inline fun KeyEvent.onKey(
        key: Key,
        enabled: Boolean = true,
        defaultConsume: Boolean = false,
        onKey: () -> Boolean
    ): Boolean {
        if (enabled && this.key == key) {
            return onKey.invoke()
        }
        return defaultConsume
    }

    inline fun KeyEvent.consumeOnKey(
        key: Key,
        enabled: Boolean = true,
        defaultConsume: Boolean = false,
        onKey: () -> Unit
    ): Boolean {
        if (enabled && this.key == key) {
            onKey.invoke()
            return true
        }
        return defaultConsume
    }

}