package com.github.jan222ik.canvas.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.github.jan222ik.canvas.DiagramBlockUIConfig
import com.github.jan222ik.canvas.data.DataPoint
import com.github.jan222ik.canvas.dsl.ChartScopeImpl

class DiagramChartScopeImpl : ChartScopeImpl(), DiagramChartScope {
    val anchorMap: MutableMap<DataPoint, @Composable AnchorScope.() -> Unit> = mutableMapOf()
    val anchorMapState: MutableMap<MutableState<DiagramBlockUIConfig>, @Composable AnchorScope.() -> Unit> = mutableMapOf()

    //override fun anchoredComposable(topLeftPoint: DataPoint, content: @Composable AnchorScope.() -> Unit) {
    //    anchorMap[topLeftPoint] = content
    //}

    override fun anchoredComposable(topLeftPoint: MutableState<DiagramBlockUIConfig>, content: @Composable AnchorScope.() -> Unit) {
        anchorMapState[topLeftPoint] = content
    }
}