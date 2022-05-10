package com.github.jan222ik.ui.feature.main.tree

import arrow.core.getOrElse
import com.github.jan222ik.ecore.EcoreModelLoader
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.uml.DiagramsLoader
import org.eclipse.uml2.uml.Element
import java.io.File

class ProjectData(
    filename: String,
    listFiles: Array<File>
) {
    val ecore = EcoreModelLoader.open(filename + ".uml").also {
        val list = mutableListOf<Element>()
        it.model.eAllContents().forEachRemaining {
            if (it is Element) {
                list.add(it)
            }
        }
        EditorManager.allUML.value = list
    }
    val diagramsLoader = listFiles.also { println(it) }.find { it.name == filename + ".diagrams" }
        ?.let { DiagramsLoader(file = it) }
    val diagrams = diagramsLoader?.loadFromFile()?.getOrElse { emptyList() } ?: emptyList()

}