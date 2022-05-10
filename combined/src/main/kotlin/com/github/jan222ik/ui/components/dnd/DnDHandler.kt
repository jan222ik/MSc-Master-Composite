package com.github.jan222ik.ui.components.dnd

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import mu.KLogging

class DnDHandler {

    companion object : KLogging()

    internal val dropTargets = mutableStateOf<List<Pair<LayoutCoordinates, DnDAction>>>(emptyList())
    internal val activeTarget = mutableStateOf<Pair<LayoutCoordinates, DnDAction>?>(null)

    fun updateActiveTarget(offset: IntOffset, data: Any?) {
        val filter = dropTargets.value
            .filter { it.first.isAttached }
            .filter { (coord, _) ->
                val pos = coord.positionInWindow()
                (pos.x..(pos.x + coord.size.width)).contains(offset.x.toFloat()) &&
                        (pos.y..(pos.y + coord.size.height)).contains(offset.y.toFloat())
            }
        val nActive = filter.maxByOrNull {
            it.first.positionInWindow().getDistanceSquared()
        }
        if (nActive != activeTarget) {
            activeTarget.value?.second?.dropExit()
            activeTarget.value = nActive
            activeTarget.value?.second?.dropEnter(data)
        }

    }


    fun drop(pos: IntOffset, data: Any?): Boolean? {
        val b = activeTarget.value?.second?.let {
            logger.debug { "Drop for ${it.name}" }
            it.drop(pos, data)
        }
        activeTarget.value = null
        return b
    }
}