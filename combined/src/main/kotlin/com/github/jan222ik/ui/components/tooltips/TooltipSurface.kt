package com.github.jan222ik.ui.components.tooltips

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun TooltipSurface(content: @Composable () -> Unit) = Surface(elevation = 8.dp, content = content)
