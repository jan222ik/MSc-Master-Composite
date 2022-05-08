package com.github.jan222ik.ui.adjusted

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class BoundingRectState(
    @JsonProperty("topLeftPacked")
    val topLeftPacked: Long,
    @JsonProperty("width")
    val width: Float,
    @JsonProperty("height")
    val height: Float
) {
    val topLeft: Offset
        @JsonIgnore()
        get() = Offset(unpackFloat1(topLeftPacked), unpackFloat2(topLeftPacked))
}