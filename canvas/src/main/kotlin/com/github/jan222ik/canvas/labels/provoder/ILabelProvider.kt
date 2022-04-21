package com.github.jan222ik.canvas.labels.provoder

fun interface ILabelProvider {
    fun provide(min: Float, max: Float) : List<Pair<String, Float>>
}
