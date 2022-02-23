package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.jan222ik.ui.feature.LocalWindowActions
import com.github.jan222ik.ui.feature.LocalWindowScope

@Composable
fun MenuToolBarComponent(modifier: Modifier) {
    with(LocalWindowScope.current) {
        WindowDraggableArea(
            modifier = modifier
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Menu + Toolbar")
                Row(
                    Modifier.align(Alignment.CenterEnd)
                ) {
                    val windowActions = LocalWindowActions.current
                    Button(
                        onClick = windowActions::minimize
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Minimize,
                            contentDescription = "Minimize Application"
                        )
                    }
                    Button(
                        onClick = windowActions::maximize
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Maximize,
                            contentDescription = "Maximize Application"
                        )
                    }
                    Button(
                        onClick = windowActions::exitApplication
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Application"
                        )
                    }
                }
            }

        }
    }
}