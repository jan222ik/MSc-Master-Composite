package com.github.jan222ik.ui.components.dnd

interface DnDAction {
    fun name(): String
    fun dropEnter(data: Any?) {}
    fun drop(data: Any?) : Boolean = true
    fun dropExit() {}
}