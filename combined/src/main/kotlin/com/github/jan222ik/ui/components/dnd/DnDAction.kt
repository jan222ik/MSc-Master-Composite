package com.github.jan222ik.ui.components.dnd

import androidx.compose.ui.unit.IntOffset

interface DnDAction {
    val name: String
    fun dropEnter(data: Any?) {}
    fun drop(pos: IntOffset, data: Any?) : Boolean = true
    fun dropExit() {}
}