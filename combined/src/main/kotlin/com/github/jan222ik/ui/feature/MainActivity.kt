package com.github.jan222ik.ui.feature

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.github.jan222ik.App
import com.github.jan222ik.ui.feature.main.keyevent.KeyEventHandler
import com.github.jan222ik.ui.navigation.NavHostComponent
import com.github.jan222ik.ui.value.AppTheme
import com.theapache64.cyclone.core.Activity
import com.theapache64.cyclone.core.Intent
import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.config.I18n4kConfigDefault
import de.comahe.i18n4k.i18n4k
import mu.KLogging
import java.awt.GraphicsEnvironment
import androidx.compose.ui.window.application as setContent

/**
 * The activity who will be hosting all screens in this app
 */
class MainActivity : Activity() {
    companion object : KLogging() {
        fun getStartIntent(): Intent {
            return Intent(MainActivity::class).apply {
                // data goes here
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate() {
        super.onCreate()
        val localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val displayMode = localGraphicsEnvironment.defaultScreenDevice.displayMode
        val displaySize = DpSize(displayMode.width.dp, displayMode.height.dp)
        val maxWinSize = run {
            val mwb = localGraphicsEnvironment.maximumWindowBounds
            DpSize(width = mwb.width.dp, height = mwb.height.dp)
        }
        val taskbarHeight = displaySize.height - maxWinSize.height
        val applicableSize = displaySize.copy(height = displaySize.height.minus(taskbarHeight))
        setContent {
            val windowState = rememberWindowState(
                size = applicableSize,
                position = WindowPosition(Alignment.TopStart)
            )
            val keyEventHandler = KeyEventHandler(
                applicableSize = applicableSize,
                setWindowSize = {
                    if (windowState.size != it) {
                        windowState.size = it
                    }
                },
                setWindowPosition = {
                    if (windowState.position != it) {
                        windowState.position = WindowPosition(alignment = it)
                    }
                },
                setPlacement = {
                    if (windowState.placement != it) {
                        windowState.placement = it
                    }
                }
            )
            var locale by remember { mutableStateOf(i18n4k.locale) }
            fun switchLocale(nextLocale: Locale) {
                logger.debug { "Switched Language form ${locale.language} to ${nextLocale.language}" }
                locale = nextLocale
                (i18n4k as I18n4kConfigDefault).locale = locale
            }
            var isDarkMode by remember { mutableStateOf(true) }
            fun switchTheme(toDarkMode: Boolean) {
                logger.debug { "Switched Theme form $isDarkMode to $toDarkMode (true=dark)" }
                isDarkMode = toDarkMode
            }
            Window(
                onCloseRequest = ::exitApplication,
                title = "${App.appArgs.appName} (${App.appArgs.version})",
                icon = painterResource("drawables/launcher_icons/system.png"),
                state = windowState,
                undecorated = true,
                resizable = true,
                onPreviewKeyEvent = { keyEventHandler.handleKeyEvent(it, windowState) }
            ) {
                if (Thread.currentThread().name == "AWT-EventQueue-0") {
                    Thread.currentThread().name = "AWT-EQ-0"
                }
                CompositionLocalProvider(
                    LocalWindowState provides windowState,
                    LocalWindowActions provides WindowActionsImpl(
                        minimize = {
                            windowState.isMinimized = true
                        },
                        maximize = {
                            windowState.placement = if (windowState.placement != WindowPlacement.Maximized)
                                WindowPlacement.Maximized else WindowPlacement.Floating
                        },
                        close = {},
                        exitApplication = this@setContent::exitApplication,
                    ),
                    LocalWindowScope provides this,
                    LocalShortcutActionHandler provides keyEventHandler,
                    LocalI18N provides (locale to ::switchLocale),
                    LocalThemeSwitcher provides (isDarkMode to ::switchTheme)
                ) {
                    AppTheme(
                        isDark = isDarkMode
                    ) {
                        // Igniting navigation
                        rememberRootComponent(factory = ::NavHostComponent)
                            .render()
                    }
                }
            }

        }

    }
}