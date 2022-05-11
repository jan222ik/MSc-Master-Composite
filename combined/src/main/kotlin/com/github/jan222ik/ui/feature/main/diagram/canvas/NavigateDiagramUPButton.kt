package com.github.jan222ik.ui.feature.main.diagram.canvas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space

@Composable
fun NavigateDiagramUPButton(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = EditorColors.backgroundGray,
        shape = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                val cutSize = 5f
                return Outline.Generic(path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(x = size.width, y = 0f)
                    lineTo(x = size.width, y = size.height - cutSize)
                    lineTo(x = size.width - cutSize, y = size.height)
                    lineTo(x = 0f, y = size.height)
                    close()
                })
            }

            override fun toString(): String = "RectangleShapeWithCut"
        },
        border = BorderStroke(0.dp, EditorColors.dividerGray)
    ) {
        Row(
            modifier = Modifier.padding(vertical = Space.dp4, horizontal = Space.dp8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.dp4)
        ) {
            Icon(
                modifier = Modifier.size(Space.dp16),
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = null
            )
            Text(text = "<Diagram name>")
        }
    }
}
