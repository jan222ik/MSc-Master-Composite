package com.github.jan222ik.ui.feature.main.footer.progress

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
