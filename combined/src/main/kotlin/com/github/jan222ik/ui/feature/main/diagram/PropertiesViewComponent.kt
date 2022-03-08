package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
class PropertiesViewComponent(
    val parent: DiagramAreaComponent
) {

    @Composable
    fun render() {
        Box(Modifier.fillMaxSize()) { Text(text = "Property View") }
    }
}