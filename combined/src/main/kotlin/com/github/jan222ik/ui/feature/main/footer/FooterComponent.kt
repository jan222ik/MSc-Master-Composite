package com.github.jan222ik.ui.feature.main.footer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.jan222ik.ui.feature.main.footer.progress.BackgroundJobComponent
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.footer.progress.ProgressObservedJob
import kotlinx.coroutines.delay

@Composable
fun FooterComponent(modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Text("Footer")
        BackgroundJobIntegration()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.BackgroundJobIntegration() {
    val scope = rememberCoroutineScope()
    val handler = remember(scope) { JobHandler(scope) }
    val component = remember(handler) { BackgroundJobComponent(handler) }
    component.render(this)
    LaunchedEffect(Unit) {
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
}