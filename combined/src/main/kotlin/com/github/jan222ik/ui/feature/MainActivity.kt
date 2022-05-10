package com.github.jan222ik.ui.feature

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import com.github.jan222ik.App
import com.github.jan222ik.inspector.CompoundCollector
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.ui.components.dnd.DnDHandler
import com.github.jan222ik.ui.feature.debug.ApplicationBaseDebugWindow
import com.github.jan222ik.ui.feature.main.keyevent.KeyEventHandler
import com.github.jan222ik.ui.feature.wizard.Project
import com.github.jan222ik.ui.navigation.NavHostComponent
import com.github.jan222ik.ui.value.AppTheme
import com.theapache64.cyclone.core.Activity
import com.theapache64.cyclone.core.Intent
import de.comahe.i18n4k.Locale
import de.comahe.i18n4k.config.I18n4kConfigDefault
import de.comahe.i18n4k.i18n4k
import mu.KLogging
import java.awt.GraphicsEnvironment
import java.io.File
import java.util.prefs.Preferences

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
        val dnDHandler = DnDHandler()
        val compoundCollector = CompoundCollector(
            File("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\build\\spy\\").apply { delete(); mkdirs(); }
        )
        application {
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
                },
                compoundCollector = compoundCollector
            )
            var locale by remember { mutableStateOf(i18n4k.locale) }
            fun switchLocale(nextLocale: Locale) {
                logger.debug { "Switched Language form ${locale.language} to ${nextLocale.language}" }
                locale = nextLocale
                (i18n4k as I18n4kConfigDefault).locale = locale
            }

            var isDarkMode by remember { mutableStateOf(false) }
            fun switchTheme(toDarkMode: Boolean) {
                logger.debug { "Switched Theme form $isDarkMode to $toDarkMode (true=dark)" }
                isDarkMode = toDarkMode
            }

            var project by remember {
                val project = (loadRecent()
                    ?: loadOrCreateDefault("C:\\Users\\jan\\IdeaProjects\\MSc-Master-Composite\\appworkspace"))
                mutableStateOf<Project>(
                    project
                )
            }

            fun switchProject(newProject: Project) {
                logger.debug { "Switched Project form $project to $newProject" }
                project = newProject
            }

            val scope = rememberCoroutineScope()
            val commandStackHandler = remember { CommandStackHandler.INSTANCE }
            DisposableEffect(compoundCollector) {
                onDispose {
                    compoundCollector.save()
                }
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
                        exitApplication = this@application::exitApplication,
                    ),
                    LocalWindowScope provides this,
                    LocalShortcutActionHandler provides keyEventHandler,
                    LocalI18N provides (locale to ::switchLocale),
                    LocalThemeSwitcher provides (isDarkMode to ::switchTheme),
                    LocalProjectSwitcher provides (project to ::switchProject),
                    LocalDropTargetHandler provides dnDHandler,
                    LocalCommandStackHandler provides commandStackHandler
                ) {
                    AppTheme(
                        isDark = isDarkMode
                    ) {
                        // Igniting navigation
                        rememberRootComponent(factory = ::NavHostComponent)
                            .render()
                    }
                    var open by remember { mutableStateOf(true) }
                    ApplicationBaseDebugWindow.render(
                        open,
                        onClose = { open = false },
                        keyEventHandler,
                        commandStackHandler
                    )
                }
            }

        }

    }

    private fun loadRecent(): Project? {
        val lastProject = Preferences.userRoot().node("com.github.jan222ik.msc.modeller").get("lastProjects", "")
        return if (lastProject.isNotEmpty()) {
            Project.load(File(lastProject))
        } else null
    }

    private fun loadOrCreateDefault(defaultPath: String): Project {
        val root = File(defaultPath)
        return Project.load(root) ?: Project.create(root, root.name)

    }
}