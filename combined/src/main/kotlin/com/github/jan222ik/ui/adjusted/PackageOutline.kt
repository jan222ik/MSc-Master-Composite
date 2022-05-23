package com.github.jan222ik.ui.adjusted

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.singleWindowApplication


fun main() {
    singleWindowApplication {
        Surface(color = Color.DarkGray) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    backgroundColor = Color.Transparent,
                    shape = RectangleShape,
                ) {
                    Column {
                        Surface {
                            Text("Package name")
                        }
                        Surface {
                            Text("Package content area that is bigger than the top text")
                        }
                    }
                }
            }
        }
    }
}