package com.github.jan222ik.ui.feature.main.tree

import arrow.core.getOrElse
import com.github.jan222ik.ecore.EcoreModelLoader
import com.github.jan222ik.ui.uml.DiagramsLoader
import java.io.File

class ProjectData(
    filename: String,
    listFiles: Array<File>
) {
    val ecore = EcoreModelLoader.open(filename + ".uml")
    val diagramsLoader = listFiles.also { println(it) }.find { it.name == filename + ".diagrams" }
        ?.let { DiagramsLoader(file = it) }
    val diagrams = diagramsLoader?.loadFromFile()?.getOrElse { emptyList() } ?: emptyList()

}