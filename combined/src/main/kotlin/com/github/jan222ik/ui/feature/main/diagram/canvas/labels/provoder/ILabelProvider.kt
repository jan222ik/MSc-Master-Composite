package com.github.jan222ik.ui.feature.main.diagram.canvas.labels.provoder

fun interface ILabelProvider {
    fun provide(min: Float, max: Float) : List<Pair<String, Float>>
}
