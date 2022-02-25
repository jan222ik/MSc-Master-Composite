package com.github.jan222ik.ui.feature

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutActionsHandler

val LocalWindowState = compositionLocalOf<WindowState> { error("No value for LocalWindowState in composition tree!") }
val LocalWindowScope = compositionLocalOf<WindowScope> { error("No value for LocalWindowScope in composition tree!") }

interface WindowActions {
    fun minimize()
    fun maximize()
    fun close()
    fun exitApplication()
}


data class WindowActionsImpl(
    val minimize: () -> Unit,
    val maximize: () -> Unit,
    val close: () -> Unit,
    val exitApplication: () -> Unit,
) : WindowActions {
    override fun minimize() = minimize.invoke()

    override fun maximize() = maximize.invoke()

    override fun close() = close.invoke()

    override fun exitApplication() = exitApplication.invoke()
}

val LocalWindowActions = compositionLocalOf<WindowActions> { error("No value for LocalWindowActions in composition tree!") }

val LocalShortcutActionHandler = compositionLocalOf<ShortcutActionsHandler> { error("No value for LocalShortcutActionHandler in composition tree!") }