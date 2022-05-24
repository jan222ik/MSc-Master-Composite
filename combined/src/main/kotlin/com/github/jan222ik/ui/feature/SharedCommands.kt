package com.github.jan222ik.ui.feature

import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction

object SharedCommands {
    var forceOpenProperties: (() -> Unit)? = null
    var showHideExplorer: ShortcutAction? = null
    var showHidePalette: ShortcutAction? = null
    var showHidePropertiesView: ShortcutAction? = null
}