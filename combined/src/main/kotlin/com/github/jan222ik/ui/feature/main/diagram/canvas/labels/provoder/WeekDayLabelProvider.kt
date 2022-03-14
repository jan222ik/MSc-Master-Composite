package com.github.jan222ik.ui.feature.main.diagram.canvas.labels.provoder

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


fun weekDayLabelProvider() = ILabelProvider { min: Float, max: Float ->
    require(max > min) { "Max should be larger than min. min: $min, max: $max" }
    val days = listOf("Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun")
    if (max < 0 || min.roundToInt() > 7) {
        return@ILabelProvider listOf()
    } else {
        val sIdx = max(0.0, min.toDouble()).toInt()
        // Rounding because of floating point problems
        val eIdx = min(7.0, max.roundToInt().toDouble()).toInt()
        return@ILabelProvider days.subList(sIdx, eIdx).mapIndexed {idx, it -> it to sIdx + idx.toFloat()}
    }
}
