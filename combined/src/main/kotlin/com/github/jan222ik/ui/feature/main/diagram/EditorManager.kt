package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.runtime.mutableStateOf
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.uml.DiagramHolder

object EditorManager {
    val diagrams = mutableStateOf(emptyList<DiagramHolder>())
    val openTabs = mutableStateOf(
        listOf(
            EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
            EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
            //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
            //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
            //EditorTabViewModel(name = "a", DiagramType.BLOCK_DEFINITION),
            //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
            //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
            //EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
            //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
            //EditorTabViewModel(name = "First diagram", DiagramType.PACKAGE),
            //EditorTabViewModel(name = "block diagram", DiagramType.BLOCK_DEFINITION),
            //EditorTabViewModel(name = "parametric diagram", DiagramType.PARAMETRIC),
        )
    )

    var selectedIdx = mutableStateOf(0)
    val activeEditorTab = mutableStateOf<EditorTabViewModel?>(null)

    fun onEditorSwitch(nextIdx: Int) {
        activeEditorTab.value = openTabs.value[nextIdx]
        selectedIdx.value = nextIdx
    }

    fun onEditorClose(closeIdx: Int) {
        println("closeIdx=$closeIdx")
        openTabs.value = openTabs.value.toMutableList().apply {
            removeAt(closeIdx)
        }
        if (closeIdx == selectedIdx.value) {
            if (openTabs.value.isNotEmpty()) {
                selectedIdx.value = closeIdx.dec().coerceIn(minimumValue = 0, maximumValue = openTabs.value.lastIndex)
                activeEditorTab.value = openTabs.value.getOrNull(selectedIdx.value)
            }
        }
        if (openTabs.value.isEmpty()) {
            activeEditorTab.value = null
        }
    }

    fun moveToOrOpenDiagram(diagram: DiagramHolder) {
        val firstIdx = openTabs.value.indexOfFirst { it.name == diagram.name }
        if (firstIdx != -1) {
            onEditorSwitch(firstIdx)
        } else {
            openTabs.value = openTabs.value + EditorTabViewModel(name = diagram.name, type = diagram.diagramType)
            onEditorSwitch(openTabs.value.lastIndex)
        }
    }
}