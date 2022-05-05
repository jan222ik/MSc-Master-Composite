package com.github.jan222ik.ui.adjusted

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.ui.feature.main.tree.ProjectTreeHandler

interface ICanvasComposable {
    val boundingShape: IBoundingShape

    @Composable
    fun render(projectTreeHandler: ProjectTreeHandler, offset: Offset)
}