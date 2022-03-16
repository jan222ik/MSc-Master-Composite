package com.github.jan222ik.playground.dragdrop

interface DnDAction {
    fun name(): String
    fun dropEnter(data: Any?) {}
    fun drop(data: Any?) {}
    fun dropExit() {}
}