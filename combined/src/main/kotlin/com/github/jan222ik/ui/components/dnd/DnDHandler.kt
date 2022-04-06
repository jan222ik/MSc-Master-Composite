package com.github.jan222ik.ui.components.dnd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset

class DnDHandler {
    internal var dropTargets by mutableStateOf<List<Pair<LayoutCoordinates, DnDAction>>>(emptyList())
    private var activeTarget by mutableStateOf<Pair<LayoutCoordinates, DnDAction>?>(null)

    fun updateActiveTarget(offset: IntOffset, data: Any?) {
        val filter = dropTargets.filter { (coord, _) ->
            val pos = coord.positionInWindow()
            (pos.x..(pos.x + coord.size.width)).contains(offset.x.toFloat()) &&
                    (pos.y..(pos.y + coord.size.height)).contains(offset.y.toFloat())
        }
        val nActive = filter.maxByOrNull {
            it.first.positionInWindow().getDistanceSquared()
        }
        if (nActive != activeTarget) {
            activeTarget?.second?.dropExit()
            activeTarget = nActive
            activeTarget?.second?.dropEnter(data)
        }

    }


    fun drop(data: Any?): Boolean? {
        val b = activeTarget?.second?.drop(data)
        activeTarget = null
        return b
    }
}