package com.github.jan222ik.ui.feature.main.diagram.paletteview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.github.jan222ik.ui.components.dnd.dndDraggable
import com.github.jan222ik.ui.feature.LocalDropTargetHandler
import com.github.jan222ik.ui.feature.main.diagram.canvas.EditorTabViewModel

@Composable
fun PaletteView(activeEditorTab: EditorTabViewModel) {
    var showSearch by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf(TextFieldValue()) }
    val categories = remember(
        activeEditorTab,
        activeEditorTab.type
    ) { PaletteContents.getCategoriesForDiagramType(activeEditorTab.type) }
    val dndHandler = LocalDropTargetHandler.current
    Column {
        if (showSearch) {
            // TODO enable search window
            TextField(
                value = textState,
                onValueChange = { textState = it },
                label = {
                    Text("Search in palette")
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categories.forEach {
                Column {
                    Text(it.name, style = LocalTextStyle.current.copy(fontSize = LocalTextStyle.current.fontSize.value.plus(2f).sp))
                    it.options.forEach {
                        Row(
                            modifier = Modifier.dndDraggable(
                                handler = dndHandler,
                                dataProvider = { it.name },
                                onDragCancel = { snapBack -> snapBack.invoke() },
                                onDragFinished = { _, snapBack -> snapBack.invoke() }
                            )
                        ) {
                            val img = remember(it) { it.imageBitmapNotRemembered }
                            img?.let {
                                Icon(bitmap = img, contentDescription = null)
                            }
                            Text(it.name)
                        }
                    }
                }
            }
        }
    }
}