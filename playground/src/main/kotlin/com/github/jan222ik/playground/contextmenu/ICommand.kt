package com.github.jan222ik.playground.contextmenu


interface ICommand {
    fun isActive() : Boolean
    suspend fun execute()
    fun canUndo() : Boolean
    suspend fun undo()
}