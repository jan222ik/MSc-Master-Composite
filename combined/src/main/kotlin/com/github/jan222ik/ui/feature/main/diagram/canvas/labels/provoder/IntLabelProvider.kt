package com.github.jan222ik.ui.feature.main.diagram.canvas.labels.provoder

@OptIn(ExperimentalUnsignedTypes::class)
fun intLabelProvider(step: UInt = 1u) = ILabelProvider { min: Float, max: Float ->
    require(max > min) { "Max should be larger than min. min: $min, max: $max" }
    return@ILabelProvider IntProgression
        .fromClosedRange(rangeStart = min.toInt(), rangeEnd = max.toInt(), step = step.toInt())
        .map { "$it" to it.toFloat() }
}
