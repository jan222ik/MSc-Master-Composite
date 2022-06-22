package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import javax.annotation.meta.Exhaustive

enum class DiagramType {
    PACKAGE, PARAMETRIC, BLOCK_DEFINITION;

    @Composable
    fun iconAsPainter(): Painter {
        @Exhaustive
        val painter = when (this) {
            DiagramType.PACKAGE -> painterResource("drawables/uml_icons/Diagram_SysML_Package.gif")
            DiagramType.PARAMETRIC -> painterResource("drawables/uml_icons/Diagram_Parametric.png")
            DiagramType.BLOCK_DEFINITION -> painterResource("drawables/uml_icons/Diagram_BlockDefinition.gif")
        }
        return painter
    }

    fun displayableName() : String = when (this) {
        PACKAGE -> "Package"
        PARAMETRIC -> "Parametric"
        BLOCK_DEFINITION -> "Block"
    }
}