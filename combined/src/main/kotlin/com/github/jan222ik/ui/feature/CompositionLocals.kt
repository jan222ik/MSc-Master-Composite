package com.github.jan222ik.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutActionsHandler
import de.comahe.i18n4k.Locale

/**
 * LocalWindowState is a [ProvidableCompositionLocal] that provides the state of the current window [WindowState].
 */
val LocalWindowState = compositionLocalOf<WindowState> { error("No value for LocalWindowState in composition tree!") }
/**
 * LocalWindowScope is a [ProvidableCompositionLocal] that provides the scope of the current window [WindowScope].
 */
val LocalWindowScope = compositionLocalOf<WindowScope> { error("No value for LocalWindowScope in composition tree!") }

/**
 * Subset of Actions that can be applied to a window.
 */
interface WindowActions {
    /**
     * Minimizes the current window.
     */
    fun minimize()

    /**
     * Maximizes the current window.
     */
    fun maximize()

    /**
     * Closes the current window.
     */
    fun close()

    /**
     * Closes the application.
     */
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

/**
 * LocalWindowActions is a [ProvidableCompositionLocal] that provides the actions for the current window [WindowActions].
 */
val LocalWindowActions = compositionLocalOf<WindowActions> { error("No value for LocalWindowActions in composition tree!") }

/**
 * LocalShortcutActionHandler is a [ProvidableCompositionLocal] that provides the shortcut handler for the current window [ShortcutActionsHandler].
 */
val LocalShortcutActionHandler = compositionLocalOf<ShortcutActionsHandler> { error("No value for LocalShortcutActionHandler in composition tree!") }

/**
 * LocalI18N is a [ProvidableCompositionLocal] that provides a [Pair] with the locale for the current window [Locale] and a setter to change it.
 */
val LocalI18N = compositionLocalOf<Pair<Locale, (Locale) -> Unit>> { error("No value for LocalI18N in composition tree!") }

/**
 * LocalThemeSwitcher is a [ProvidableCompositionLocal] that provides a [Pair] with the theme.isDarkMode for the current window [Boolean] and a setter to change it.
 */
val LocalThemeSwitcher = compositionLocalOf<Pair<Boolean, (Boolean) -> Unit>> { error("No value for LocalThemeSwitcher in composition tree!") }

@Composable
fun stringResource(key: Any? = null, string: () -> String) : String {
    return stringResource(keys = arrayOf(key), string = string)
}

@Composable
fun stringResource(vararg keys: Any? = emptyArray(), string: () -> String) : String {
    val locale = LocalI18N.current
    return remember(string, locale, *keys) {
        string.invoke()
    }
}