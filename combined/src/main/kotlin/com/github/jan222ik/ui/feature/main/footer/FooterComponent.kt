package com.github.jan222ik.ui.feature.main.footer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FooterComponent(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Text("Footer")
    }
}