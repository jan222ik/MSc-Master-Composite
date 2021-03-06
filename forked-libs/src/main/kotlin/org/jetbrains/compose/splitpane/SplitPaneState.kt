package org.jetbrains.compose.splitpane

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp

@ExperimentalSplitPaneApi
class SplitPaneState(
    initialPositionPercentage: Float,
    moveEnabled: Boolean,
) {

    var moveEnabled by mutableStateOf(moveEnabled)
        internal set

    var positionPercentage by mutableStateOf(initialPositionPercentage)
        internal set

    internal var minPosition: Float = 0f

    internal var maxPosition: Float = Float.POSITIVE_INFINITY

    fun dispatchRawMovement(delta: Float) {
        val movableArea = maxPosition - minPosition
        if (movableArea > 0) {
            positionPercentage =
                ((movableArea * positionPercentage) + delta).coerceIn(minPosition, maxPosition) / movableArea
        }
    }

    fun setToMin() {
        val movableArea = maxPosition - minPosition
        if (movableArea > 0) {
            positionPercentage = minPosition / movableArea
        }
    }

    fun setToMax() {
        val movableArea = maxPosition - minPosition
        if (movableArea > 0) {
            positionPercentage = 1f
        }
    }

    fun setToDpFromFirst(dp: Dp) {
        val movableArea = maxPosition - minPosition
        if (movableArea > 0) {
            positionPercentage = dp.value / movableArea
        }
    }

    fun setToDpFromSecond(dp: Dp) {
        val movableArea = maxPosition - minPosition
        if (movableArea > 0) {
            positionPercentage = 1 - (dp.value / movableArea)
        }
    }

}