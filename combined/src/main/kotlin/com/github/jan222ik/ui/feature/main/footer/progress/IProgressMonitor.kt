package com.github.jan222ik.ui.feature.main.footer.progress

import androidx.compose.runtime.State

interface IProgressMonitor {
    companion object {
        const val MAX_PROGRESS_TICKS = 100f
        const val UNKNOWN_PROGRESS_ADVANCE = -1f
    }

    val name: String
    val progressTicksState: State<Float>
    val messageState: State<String?>

    suspend fun start()
    fun cancel()


    fun percentageString(): String = progressTicksState.percentageFloat().times(100).toInt().toString()
}