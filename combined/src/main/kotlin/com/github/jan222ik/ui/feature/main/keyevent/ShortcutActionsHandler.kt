package com.github.jan222ik.ui.feature.main.keyevent

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode

interface ShortcutActionsHandler {
    fun register(action: ShortcutAction) : ShortcutAction
    fun deregister(action: ShortcutAction)

    fun registerOnRelease(action: ShortcutAction) : ShortcutAction
    fun deregisterOnRelease(action: ShortcutAction)
    fun execute(action: ShortcutAction?)
}

data class ShortcutAction(
    val key: Key,
    val modifiers: Int = KeyModifier.NO_MODIFIERS,
    val action: () -> Boolean
) {

    companion object {
        fun of(
            key: Key,
            vararg modifiers: Int = intArrayOf(KeyModifier.NO_MODIFIERS),
            action: () -> Boolean
        ) = ShortcutAction(key, modifiers.sum(), action)

        fun of(
            key: Key,
            modifierSum: Int = KeyModifier.NO_MODIFIERS,
            action: () -> Boolean
        ) = ShortcutAction(key, modifierSum, action)
    }

    object KeyModifier {
        const val NO_MODIFIERS = 0
        const val SHIFT = 1
        const val CTRL = 2
        const val ALT = 4
        const val META = 8
        const val ANY_MODIFIERS = -1

        fun toText(int: Int) : String {
            return when (int) {
                ANY_MODIFIERS -> "Any +"
                NO_MODIFIERS -> ""
                SHIFT -> "Shift +"
                CTRL -> "CTRL +"
                SHIFT + CTRL -> "CTRL + Shift +"
                ALT -> "ALT +"
                SHIFT + ALT -> "ALT + Shift +"
                CTRL + ALT -> "CTRL + Shift +"
                SHIFT + CTRL + ALT -> "CTRL + ALT + Shift +"
                else -> "$int +"
            }
        }
    }

    override fun toString(): String {
        return "ShortcutAction(keyCombo=${KeyModifier.toText(modifiers)} ${java.awt.event.KeyEvent.getKeyText(key.nativeKeyCode)})"
    }


}