package com.github.jan222ik.ui.components.dnd

import androidx.compose.runtime.compositionLocalOf

val LocalDropTargetHandler = compositionLocalOf<DnDHandler> { error("None present") }
