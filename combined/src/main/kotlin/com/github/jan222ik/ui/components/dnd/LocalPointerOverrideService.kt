package com.github.jan222ik.ui.components.dnd

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.PointerIcon

val LocalPointerOverrideService =
    staticCompositionLocalOf<MutableState<PointerIcon?>> { error("Not provided in composition") }

