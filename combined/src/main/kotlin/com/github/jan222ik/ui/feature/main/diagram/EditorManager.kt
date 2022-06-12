package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.runtime.mutableStateOf
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.command.CommandStackHandler
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel
import com.github.jan222ik.ui.uml.DiagramHolder
import java.lang.Math.abs

object EditorManager {
    val allowEdit = mutableStateOf(true)
    val diagrams = mutableStateOf(emptyList<DiagramHolder>())

    val openTabs = mutableStateOf(emptyList<EditorTabViewModel>())

    var selectedIdx = mutableStateOf(0)
    val activeEditorTab = mutableStateOf<EditorTabViewModel?>(null)

    fun onEditorSwitch(nextIdx: Int) {
        activeEditorTab.value = openTabs.value[nextIdx]
        selectedIdx.value = nextIdx
    }

    fun onCloseActiveEditor() {
        openTabs.value.indexOfFirst { it.id == activeEditorTab.value?.id }.let {
            if (it != -1) {
                onEditorClose(closeIdx = it)
            }
        }
    }

    fun closeAllEditors() {
        openTabs.value = emptyList()
        activeEditorTab.value = null
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

    fun moveToOrOpenDiagram(tmmDiagram: TMM.ModelTree.Diagram, commandStackHandler: CommandStackHandler) {
        val firstIdx = openTabs.value.indexOfFirst { it.observableDiagram.diagramName.tfv.text == tmmDiagram.initDiagram.name }
        if (firstIdx != -1) {
            onEditorSwitch(firstIdx)
        } else {
            val toObservable = tmmDiagram.initDiagram.toObservable(tmmDiagram.getProjectUMLElement(), commandStackHandler)
            openTabs.value = openTabs.value + EditorTabViewModel(tmmDiagram = tmmDiagram, observableDiagram = toObservable)
            onEditorSwitch(openTabs.value.lastIndex)
        }
    }

    fun moveBack() {
        onEditorSwitch(abs(selectedIdx.value.dec().rem(openTabs.value.size)))
    }

    fun moveForward() {
        onEditorSwitch(abs(selectedIdx.value.inc().rem(openTabs.value.size)))
    }

    fun closeEditorForDiagram(diagram: TMM.ModelTree.Diagram) {
        onEditorClose(openTabs.value.indexOfFirst { it.tmmDiagram == diagram })
    }
}