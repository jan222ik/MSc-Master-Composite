package com.github.jan222ik.canvas.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.dsl.ChartScope

interface DiagramChartScope : ChartScope {
    //fun anchoredComposable(topLeftPoint: DataPoint, content: @Composable AnchorScope.() -> Unit)
    fun anchoredComposable(topLeftPoint: MutableState<DiagramBlockUIConfig>, content: @Composable AnchorScope.() -> Unit)
}