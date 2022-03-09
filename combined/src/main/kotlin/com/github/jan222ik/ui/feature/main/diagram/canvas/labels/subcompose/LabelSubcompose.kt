package com.github.jan222ik.ui.feature.main.diagram.canvas.labels.subcompose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import com.github.jan222ik.ui.feature.main.diagram.canvas.dsl.ChartLabelSlot
import com.github.jan222ik.ui.feature.main.diagram.canvas.dsl.ChartScopeImpl
import com.github.jan222ik.ui.feature.main.diagram.canvas.viewport.Viewport


@Suppress("SpellCheckingInspection")
fun SubcomposeMeasureScope.labelSubcompose(
    slot: ChartLabelSlot,
    viewport: Viewport,
    graphScopeImpl: ChartScopeImpl,
    constraints: Constraints
): Pair<Int, List<LabelSubcomposeResult>> {
    val isVertical = slot == ChartLabelSlot.START || slot == ChartLabelSlot.END
    val labels = mutableStateOf(listOf<Pair<String, Float>>())
    return subcompose(slot) {
        graphScopeImpl.labels[slot]?.let {
            val provider = it.first
            val content = it.second
            val provide = provider.provide(
                min = viewport.minY.takeIf { isVertical } ?: viewport.minX,
                max = viewport.maxY.takeIf { isVertical } ?: viewport.maxX,
            )
            provide.forEach { pair -> content(pair) }
            labels.value = provide
        }
    }.let { measureables ->
        var justifyDist = 0
        val subcomposeResults = measureables.mapIndexed { idx, it ->
            val placeable = it.measure(constraints)
            if (isVertical) {
                if (justifyDist < placeable.width) {
                    justifyDist = placeable.width
                }
            } else {
                if (justifyDist < placeable.height) {
                    justifyDist = placeable.height
                }
            }
            LabelSubcomposeResult(
                placeable = placeable,
                labelInChartSpace = labels.value[idx].second
            )
        }
        justifyDist to subcomposeResults
    }
}
