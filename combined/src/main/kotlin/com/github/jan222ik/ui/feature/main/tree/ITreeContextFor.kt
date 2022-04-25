package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.ui.components.menu.MenuContribution

interface ITreeContextFor {
    fun setContextFor(pair: Pair<PopupPositionProvider, List<MenuContribution>>?)
}