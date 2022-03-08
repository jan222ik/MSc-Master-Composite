package com.github.jan222ik.ui.feature.main.footer.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import javax.inject.Inject

class BackgroundJobComponent @Inject constructor(
    private val jobHandler: JobHandler
) {

    @Composable
    @ExperimentalFoundationApi
    fun render(box: BoxScope) {
        var expanded by remember { mutableStateOf(false) }
        with(box) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                visible = jobHandler.jobs.isNotEmpty()
            ) {
                if (jobHandler.jobs.isNotEmpty()) {
                    val monitor = jobHandler.jobs.first()
                    TooltipArea(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { expanded = !expanded },
                        tooltip = {
                            Card(modifier = Modifier.padding(4.dp)) {
                                Text(
                                    "Background Job" + when (jobHandler.jobs.size) {
                                        1 -> " - ${monitor.name}"
                                        else -> "s (${jobHandler.jobs.size})"
                                    }
                                )
                            }
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            ProgressBarForJob(monitor = monitor)
                        }
                    }
                }
            }
            if (expanded) {
                ExpandedActiveJobPlane(onDismissRequest = {
                    expanded = false
                })
            }
        }
    }


    @Composable
    fun ProgressBarForJob(monitor: IProgressMonitor) {
        if (monitor.progressTicksState.value != IProgressMonitor.UNKNOWN_PROGRESS_ADVANCE) {
            val anim = animateFloatAsState(targetValue = monitor.progressTicksState.percentageFloat())
            LinearProgressIndicator(progress = anim.value)
        } else {
            LinearProgressIndicator()
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun ExpandedActiveJobPlane(onDismissRequest: () -> Unit) {
        AlertDialog(onDismissRequest = onDismissRequest, buttons = {
            Card(Modifier.size(600.dp, 800.dp)) {
                val scrollState = rememberLazyListState()
                val scrollAdapter = rememberScrollbarAdapter(scrollState)
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart),
                            text = "Background Jobs",
                            style = MaterialTheme.typography.h4
                        )
                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable(onClick = onDismissRequest),
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close popup"
                        )
                    }
                    Box {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState
                        ) {
                            items(items = jobHandler.jobs) { item ->
                                Column(Modifier.padding(5.dp)) {
                                    Text(text = item.name)
                                    Text(text = item.messageState.value ?: "No message")
                                    Text(text = item.progressTicksState.value.toString())
                                    if (item.progressTicksState.value != IProgressMonitor.UNKNOWN_PROGRESS_ADVANCE) {
                                        val animProg =
                                            animateFloatAsState(targetValue = item.progressTicksState.percentageFloat())
                                        LinearProgressIndicator(progress = animProg.value)
                                    } else {
                                        LinearProgressIndicator()
                                    }
                                }
                            }
                        }
                        if (jobHandler.jobs.isEmpty()) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "No Jobs running!",
                                style = MaterialTheme.typography.h5
                            )
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.TopEnd),
                            adapter = scrollAdapter
                        )
                    }
                }
            }
        })
    }

}