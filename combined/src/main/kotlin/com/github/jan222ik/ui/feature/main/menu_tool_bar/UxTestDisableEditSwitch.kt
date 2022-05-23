package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.adjusted.DebugCanvas
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.value.Space

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UxTestDisableEditSwitch() {
    Box(Modifier.size(0.dp)) {
        if (EditorManager.allowEdit.value && DebugCanvas.blockUXTestEdit.value) {
            AlertDialog(
                modifier = Modifier.size(400.dp, 150.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                        Text("Info")
                    }
                },
                text = {
                    Text("Editing is not supported in this UX test")
                },
                onDismissRequest = {
                    EditorManager.allowEdit.value = false
                },
                confirmButton = {
                    Button(
                        onClick = {
                            EditorManager.allowEdit.value = false
                        }
                    ) {
                        Text("Continue in Viewer Mode")
                    }
                }
            )
        }
    }
}