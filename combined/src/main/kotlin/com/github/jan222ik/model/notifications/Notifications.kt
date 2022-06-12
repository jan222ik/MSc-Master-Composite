package com.github.jan222ik.model.notifications

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mu.KLogging

object Notifications : KLogging() {
    private val notifications = MutableStateFlow(emptyList<Notification>())

    suspend fun addNotification(notification: Notification) {
        logger.debug { "Add notification $notification" }
        notifications.emit(notifications.value + notification)
    }

    private suspend fun removeNotification(notification: Notification) {
        logger.debug { "Removed notification $notification" }
        notifications.emit(notifications.value - notification)
    }

    @Composable
    fun renderNotifications(boxScope: BoxScope) {
        val scope = rememberCoroutineScope()
        with(boxScope) {
            val n = notifications.collectAsState()
            if (n.value.isNotEmpty()) {
                Popup(
                    Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier.width(600.dp)
                    ) {
                        n.value.forEach {
                            val decayState = remember(it, it.id) { mutableStateOf(it.decayTimeMilli) }
                            decayState.value?.let { v ->
                                LaunchedEffect(it.id) {
                                    while (decayState.value!! > 0) {
                                        decayState.value = decayState.value!! - 10
                                        delay(10)
                                    }
                                    removeNotification(it)
                                }
                            }
                            val anim = it.decayTimeMilli
                                ?.let { it1 ->
                                    decayState.value?.toFloat()?.let { it2 -> animateFloatAsState(it2) }
                                }
                            Card(
                                backgroundColor = Color(0xFF383a42),
                                contentColor = Color.White,
                                modifier = Modifier
                                    .padding(end = Space.dp32, bottom = Space.dp32)
                                    .clickable {
                                    scope.launch { removeNotification(it) }
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = Space.dp4, horizontal = Space.dp8),
                                    verticalArrangement = Arrangement.spacedBy(Space.dp8)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                                    ) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EditorColors.focusActive)
                                        Text(text = it.title, style = MaterialTheme.typography.h5)
                                    }
                                    Text(text = it.message)
                                    if (anim != null) {
                                        LinearProgressIndicator(
                                            progress = anim.value.div(it.decayTimeMilli),
                                            modifier = Modifier.fillMaxWidth(),
                                            color = EditorColors.focusActive,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}