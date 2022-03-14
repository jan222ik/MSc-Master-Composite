package com.github.jan222ik.ui.feature.main.menu_tool_bar

import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler

interface ICommand {
    fun isActive() : Boolean
    suspend fun execute(handler: JobHandler)
    fun canUndo() : Boolean
    suspend fun undo()
}