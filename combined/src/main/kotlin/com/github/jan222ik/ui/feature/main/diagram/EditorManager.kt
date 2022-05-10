package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.runtime.mutableStateOf
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.uml.DiagramHolder
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.NamedElement

object EditorManager {
    val allUML = mutableStateOf<List<Element>>(emptyList())
    val diagrams = mutableStateOf(emptyList<DiagramHolder>())

    val openTabs = mutableStateOf(emptyList<EditorTabViewModel>())

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
        } else {
            selectedIdx.value = selectedIdx.value.coerceIn(minimumValue = 0, maximumValue = openTabs.value.lastIndex)
            activeEditorTab.value = openTabs.value.getOrNull(selectedIdx.value)
        }
        if (openTabs.value.isEmpty()) {
            activeEditorTab.value = null
        }
    }

    fun moveToOrOpenDiagram(diagram: DiagramHolder, commandStackHandler: CommandStackHandler) {
        val firstIdx = openTabs.value.indexOfFirst { it.observableDiagram.diagramName.tfv.text == diagram.name }
        if (firstIdx != -1) {
            onEditorSwitch(firstIdx)
        } else {
            val toObservable = diagram.toObservable(allUML.value, commandStackHandler)
            openTabs.value = openTabs.value + EditorTabViewModel(observableDiagram = toObservable)
            onEditorSwitch(openTabs.value.lastIndex)
        }
    }
}