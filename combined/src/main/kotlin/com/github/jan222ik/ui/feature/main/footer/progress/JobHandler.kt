package com.github.jan222ik.ui.feature.main.footer.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.*

class JobHandler(
    private val scope: CoroutineScope
) {
    var jobs by mutableStateOf(emptyList<IProgressMonitor>())
        private set

    fun run(job: IProgressMonitor) {
        scope.launch {
            jobs = jobs + job
            withContext(Dispatchers.IO) {
                job.start()
            }
            jobs = jobs.toMutableList().apply { remove(job) }
        }
    }

}

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

fun State<Float>.percentageFloat(): Float = (1 - (IProgressMonitor.MAX_PROGRESS_TICKS - this.value) / 100)

class ProgressObservedJob(
    hasTicks: Boolean,
    private val job: suspend (ProgressObservedJob) -> Unit,
    override val name: String
) : IProgressMonitor {
    val _progressTicksState =
        mutableStateOf(0f.takeIf { hasTicks } ?: IProgressMonitor.UNKNOWN_PROGRESS_ADVANCE)
    override val progressTicksState: State<Float>
        get() = _progressTicksState

    val _messageState = mutableStateOf<String?>(null)
    override val messageState: State<String?>
        get() = _messageState

    override suspend fun start() {
        job(this)
    }

    override fun cancel() {

    }

}

fun main() {
    singleWindowApplication {
        val scope = rememberCoroutineScope()
        val jobHandler = remember(scope) { JobHandler(scope) }
        LaunchedEffect(Unit) {
            jobHandler.run(
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
                jobHandler.run(
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
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = jobHandler.jobs) { item ->
                Column(Modifier.padding(5.dp)) {
                    Text(text = item.name)
                    Text(text = item.messageState.value ?: "No message")
                    Text(text = item.progressTicksState.value.toString())
                    if (item.progressTicksState.value != IProgressMonitor.UNKNOWN_PROGRESS_ADVANCE) {
                        val animProg = animateFloatAsState(targetValue = item.progressTicksState.percentageFloat())
                        LinearProgressIndicator(progress = animProg.value)
                    } else {
                        LinearProgressIndicator()
                    }
                }
            }
            if (jobHandler.jobs.isEmpty()) {
                item {
                    Text("No Jobs running!")
                }
            }
        }
    }
}
