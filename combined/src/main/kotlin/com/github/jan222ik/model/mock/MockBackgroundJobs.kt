package com.github.jan222ik.model.mock

import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.footer.progress.ProgressObservedJob
import com.github.jan222ik.ui.feature.main.menu_tool_bar.ICommand
import kotlinx.coroutines.delay

class MockBackgroundJobs : ICommand {
    override fun isActive(): Boolean = true

    override suspend fun execute(handler: JobHandler) {
        handler.run(
            ProgressObservedJob(
                name = "Job \"Start Unknown Time to finish Job will wait 5 sec\"",
                hasTicks = false,
                job = {
                    println("Job 1")
                    it._messageState.value = "Start Unknown Time to finish Job will wait 5 sec"
                    delay(5000)
                    it._messageState.value = "Done"
                }
            )
        )
        repeat(50) { loop ->
            delay(100)
            val jitter = 5L * (loop.rem(10))
            handler.run(
                ProgressObservedJob(
                    name = "Job $loop",
                    hasTicks = false,
                    job = {
                        it._messageState.value = "Advance one tick every 100ms"
                        repeat(100) { _ ->
                            delay(100 + jitter)
                            it._progressTicksState.value = it._progressTicksState.value.inc()
                            it._messageState.value = "Advance one tick every 100ms ${it.progressTicksState.value}"
                        }
                        it._messageState.value = "Done"
                    }
                )
            )
        }
    }

    override fun canUndo(): Boolean = false
    override suspend fun undo() = error("Can't be undone.")
}