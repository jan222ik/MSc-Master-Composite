package com.github.jan222ik.ui.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.components.menu.DrawableIcon
import com.github.jan222ik.ui.components.menu.MenuContribution
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.diagram.canvas.DNDCreation
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SharedCommands {
    var forceOpenProperties: (() -> Unit)? = null
    var showHideExplorer: ShortcutAction? = null
    var showHidePalette: ShortcutAction? = null
    var showHidePropertiesView: ShortcutAction? = null

}