package com.github.jan222ik.playground.contextmenu

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import mu.KLogging
import mu.KotlinLogging
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalFoundationApi
fun Modifier.mouseCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (MouseClickScope.() -> Unit)? = null,
    onDoubleClick: (MouseClickScope.() -> Unit)? = null,
    onClick: MouseClickScope.(evt: PointerEvent?) -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "mouseCombinedClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["onDoubleClick"] = onDoubleClick
        properties["onLongClick"] = onLongClick
        properties["onLongClickLabel"] = onLongClickLabel
    }
) {
    Modifier.mouseCombinedClickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick ?: {},
        onDoubleClick = onDoubleClick ?: {},
        onClick = onClick,
        role = role,
        indication = LocalIndication.current,
        interactionSource = remember { MutableInteractionSource() }
    )
}

internal fun Modifier.genericClickableWithoutGesture(
    gestureModifiers: Modifier,
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    fun Modifier.clickSemantics() = this.semantics(mergeDescendants = true) {
        if (role != null) {
            this.role = role
        }
        // b/156468846:  add long click semantics and double click if needed
        onClick(
            action = { onClick(); true },
            label = onClickLabel
        )
        if (onLongClick != null) {
            onLongClick(action = { onLongClick(); true }, label = onLongClickLabel)
        }
        if (!enabled) {
            disabled()
        }
    }

    fun Modifier.detectClickFromKey() = this.onKeyEvent {
        if (enabled && it.isClick) {
            onClick()
            true
        } else {
            false
        }
    }
    return this
        .clickSemantics()
        .detectClickFromKey()
        .indication(interactionSource, indication)
        .hoverable(enabled = enabled, interactionSource = interactionSource)
        .focusableInNonTouchMode(enabled = enabled, interactionSource = interactionSource)
        .then(gestureModifiers)
}

// TODO: b/202856230 - consider either making this / a similar API public, or add a parameter to
//  focusable to configure this behavior.
/**
 * [focusable] but only when not in touch mode - when [LocalInputModeManager] is
 * not [InputMode.Touch]
 */
internal fun Modifier.focusableInNonTouchMode(
    enabled: Boolean,
    interactionSource: MutableInteractionSource?
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "focusableInNonTouchMode"
        properties["enabled"] = enabled
        properties["interactionSource"] = interactionSource
    }
) {
    val inputModeManager = LocalInputModeManager.current
    Modifier
        .focusProperties { canFocus = inputModeManager.inputMode != InputMode.Touch }
        .focusable(enabled, interactionSource)
}


/**
 * Whether the specified [KeyEvent] represents a user intent to perform a click.
 * (eg. When you press Enter on a focused button, it should perform a click).
 */
internal val KeyEvent.isClick: Boolean
    get() = type == KeyEventType.KeyUp && when (key.nativeKeyCode) {
        java.awt.event.KeyEvent.VK_ENTER -> true
        else -> false
    }

@ExperimentalFoundationApi
internal val EmptyClickContext = MouseClickScope(
    PointerButtons(0), PointerKeyboardModifiers(0)
)



/**
 * Iterates through a [List] using the index and calls [action] for each item.
 * This does not allocate an iterator like [Iterable.forEach].
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

/**
 * Returns `true` if all elements match the given [predicate].
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (!predicate(it)) return false }
    return true
}

/**
 * Returns `true` if at least one element matches the given [predicate].
 *
 * **Do not use for collections that come from public APIs**, since they may not support random
 * access in an efficient way, and this method may actually be a lot slower. Only use for
 * collections that are created by code we control and are known to support random access.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return true }
    return false
}


private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
    } while (
        !event.changes.fastAll { it.changedToDown() }
    )
    return event
}

private suspend fun AwaitPointerEventScope.waitForFirstInboundUpOrCancellation(): PointerEvent? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.fastAll { it.changedToUp() }) {
            // All pointers are up
            return event
        }

        if (event.changes.fastAny {
                it.consumed.downChange || it.isOutOfBounds(size, extendedTouchPadding)
            }
        ) {
            return null // Canceled
        }

        // Check for cancel by position consumption. We can look on the Final pass of the
        // existing pointer event because it comes after the Main pass we checked above.
        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.fastAny { it.positionChangeConsumed() }) {
            return null
        }
    }
}


@ExperimentalFoundationApi
fun Modifier.mouseCombinedClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: MouseClickScope.() -> Unit,
    onDoubleClick: MouseClickScope.() -> Unit,
    onClick: MouseClickScope.(evt: PointerEvent?) -> Unit
) = composed(
    factory = {
        val onClickState = rememberUpdatedState(onClick)
        val onLongClickState = rememberUpdatedState(onLongClick)
        val onDoubleClickState = rememberUpdatedState(onDoubleClick)
        val pressedInteraction = remember { mutableStateOf<PressInteraction.Press?>(null) }
        // Used for scroll containers to give time to initiate scroll
        val delayPressInteraction: State<() -> Boolean> = remember { mutableStateOf({false}) }
        val gesture = if (enabled) {
            Modifier.pointerInput(Unit) {
                detectTapMouseGestures(
                    onTap = { event, _ ->
                        onClickState.value.invoke(
                            MouseClickScope(
                                event.buttons,
                                event.keyboardModifiers
                            ),
                            event
                        )
                    },
                    onDoubleTap = { event, _ ->
                        onDoubleClickState.value.invoke(
                            MouseClickScope(
                                event.buttons,
                                event.keyboardModifiers
                            )
                        )
                    },
                    onLongPress ={ event, _ ->
                        onLongClickState.value.invoke(
                            MouseClickScope(
                                event.buttons,
                                event.keyboardModifiers
                            )
                        )
                    },
                    onPress = { event, offset ->
                        if (enabled) {
                            handlePressInteraction(
                                offset,
                                interactionSource,
                                pressedInteraction,
                                delayPressInteraction
                            )
                        }
                    }
                )
            }
        } else {
            Modifier
        }
        Modifier
            .genericClickableWithoutGesture(
                gestureModifiers = gesture,
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = role,
                onLongClickLabel = onLongClickLabel,
                onLongClick = { onLongClick.invoke(EmptyClickContext) },
                indication = indication,
                interactionSource = interactionSource,
                onClick = { onClick(EmptyClickContext, null) }
            )
    },
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
)

private val NoPressGesture: suspend PressGestureScope.(PointerEvent, Offset) -> Unit = { _, _ -> }

/**
 * Detects tap, double-tap, and long press gestures and calls [onTap], [onDoubleTap], and
 * [onLongPress], respectively, when detected. [onPress] is called when the press is detected
 * and the [PressGestureScope.tryAwaitRelease] and [PressGestureScope.awaitRelease] can be
 * used to detect when pointers have released or the gesture was canceled.
 * The first pointer down and final pointer up are consumed, and in the
 * case of long press, all changes after the long press is detected are consumed.
 *
 * When [onDoubleTap] is provided, the tap gesture is detected only after
 * the [ViewConfiguration.doubleTapMinTimeMillis] has passed and [onDoubleTap] is called if the
 * second tap is started before [ViewConfiguration.doubleTapTimeoutMillis]. If [onDoubleTap] is not
 * provided, then [onTap] is called when the pointer up has been received.
 *
 * If the first down event was consumed, the entire gesture will be skipped, including
 * [onPress]. If the first down event was not consumed, if any other gesture consumes the down or
 * up events, the pointer moves out of the input area, or the position change is consumed,
 * the gestures are considered canceled. [onDoubleTap], [onLongPress], and [onTap] will not be
 * called after a gesture has been canceled.
 */
suspend fun PointerInputScope.detectTapMouseGestures(
    onDoubleTap: ((PointerEvent, Offset) -> Unit)? = null,
    onLongPress: ((PointerEvent, Offset) -> Unit)? = null,
    onPress: suspend PressGestureScope.(PointerEvent, Offset) -> Unit = NoPressGesture,
    onTap: ((PointerEvent, Offset) -> Unit)? = null
) = coroutineScope {
    // special signal to indicate to the sending side that it shouldn't intercept and consume
    // cancel/up events as we're only require down events
    val pressScope = PressGestureScopeImpl(this@detectTapMouseGestures)

    forEachGesture {
        awaitPointerEventScope {
            val down = awaitEventFirstDown().also {
                it.changes.forEach { it.consumeDownChange() }
            }
            //val down2: PointerInputChange = awaitFirstDown()
            //down.consumeDownChange()
            pressScope.reset()
            if (onPress !== NoPressGesture) launch {
                pressScope.onPress(down, down.changes.first().position)
            }
            val longPressTimeout = onLongPress?.let {
                viewConfiguration.longPressTimeoutMillis
            } ?: (Long.MAX_VALUE / 2)
            var upOrCancel: PointerEvent? = null
            var upOrCancelChange: PointerInputChange? = null
            try {
                // wait for first tap up or long press
                upOrCancel = withTimeout(longPressTimeout) {
                    waitForUpOrCancellationMouse()
                }
                upOrCancelChange = upOrCancel?.changes?.firstOrNull()
                if (upOrCancel == null) {
                    pressScope.cancel() // tap-up was canceled
                } else {
                    upOrCancelChange?.consumeDownChange()
                    pressScope.release()
                }
            } catch (_: PointerEventTimeoutCancellationException) {
                onLongPress?.invoke(down, down.changes.first().position)
                consumeUntilUp()
                pressScope.release()
            }

            if (upOrCancel != null && upOrCancelChange != null) {
                // tap was successful.
                if (onDoubleTap == null) {
                    onTap?.invoke(down, upOrCancelChange.position) // no need to check for double-tap.
                } else {
                    // check for second tap
                    val secondDown = awaitSecondDown(upOrCancelChange)

                    if (secondDown == null) {
                        onTap?.invoke(down, upOrCancelChange.position) // no valid second tap started
                    } else {
                        // Second tap down detected
                        pressScope.reset()
                        if (onPress !== NoPressGesture) {
                            launch { pressScope.onPress(down, secondDown.position) }
                        }

                        try {
                            // Might have a long second press as the second tap
                            withTimeout(longPressTimeout) {
                                val secondUp = waitForUpOrCancellation()
                                if (secondUp != null) {
                                    secondUp.consumeDownChange()
                                    pressScope.release()
                                    onDoubleTap(down, secondUp.position)
                                } else {
                                    pressScope.cancel()
                                    onTap?.invoke(down, upOrCancelChange.position)
                                }
                            }
                        } catch (e: PointerEventTimeoutCancellationException) {
                            // The first tap was valid, but the second tap is a long press.
                            // notify for the first tap
                            onTap?.invoke(down, upOrCancelChange.position)

                            // notify for the long press
                            onLongPress?.invoke(down, secondDown.position)
                            consumeUntilUp()
                            pressScope.release()
                        }
                    }
                }
            }
        }
    }
}


/**
 * [detectTapMouseGestures]'s implementation of [PressGestureScope].
 */
private class PressGestureScopeImpl(
    density: Density
) : PressGestureScope, Density by density {
    private var isReleased = false
    private var isCanceled = false
    private val mutex = Mutex(locked = false)

    /**
     * Called when a gesture has been canceled.
     */
    fun cancel() {
        isCanceled = true
        mutex.unlock()
    }

    /**
     * Called when all pointers are up.
     */
    fun release() {
        isReleased = true
        mutex.unlock()
    }

    /**
     * Called when a new gesture has started.
     */
    fun reset() {
        mutex.tryLock() // If tryAwaitRelease wasn't called, this will be unlocked.
        isReleased = false
        isCanceled = false
    }

    override suspend fun awaitRelease() {
        if (!tryAwaitRelease()) {
            throw GestureCancellationException("The press gesture was canceled.")
        }
    }

    override suspend fun tryAwaitRelease(): Boolean {
        if (!isReleased && !isCanceled) {
            mutex.lock()
        }
        return isReleased
    }
}

/**
 * Consumes all pointer events until nothing is pressed and then returns. This method assumes
 * that something is currently pressed.
 */
private suspend fun AwaitPointerEventScope.consumeUntilUp() {
    do {
        val event = awaitPointerEvent()
        event.changes.fastForEach { it.consumeAllChanges() }
    } while (event.changes.fastAny { it.pressed })
}

/**
 * Waits for [ViewConfiguration.doubleTapTimeoutMillis] for a second press event. If a
 * second press event is received before the time out, it is returned or `null` is returned
 * if no second press is received.
 */
private suspend fun AwaitPointerEventScope.awaitSecondDown(
    firstUp: PointerInputChange
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
    do {
        change = awaitFirstDown()
    } while (change.uptimeMillis < minUptime)
    change
}

/**
 * Reads events until all pointers are up or the gesture was canceled. The gesture
 * is considered canceled when a pointer leaves the event region, a position change
 * has been consumed or a pointer down change event was consumed in the [PointerEventPass.Main]
 * pass. If the gesture was not canceled, the final up change is returned or `null` if the
 * event was canceled.
 */
private suspend fun AwaitPointerEventScope.waitForUpOrCancellationMouse(): PointerEvent? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.fastAll { it.changedToUp() }) {
            // All pointers are up
            return event
        }

        if (event.changes.fastAny {
                it.consumed.downChange || it.isOutOfBounds(size, extendedTouchPadding)
            }
        ) {
            return null // Canceled
        }

        // Check for cancel by position consumption. We can look on the Final pass of the
        // existing pointer event because it comes after the Main pass we checked above.
        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.fastAny { it.positionChangeConsumed() }) {
            return null
        }
    }
}

internal const val TapIndicationDelay: Long = 0L

internal suspend fun PressGestureScope.handlePressInteraction(
    pressPoint: Offset,
    interactionSource: MutableInteractionSource,
    pressedInteraction: MutableState<PressInteraction.Press?>,
    delayPressInteraction: State<() -> Boolean>
) {
    coroutineScope {
        val delayJob = launch {
            if (delayPressInteraction.value()) {
                delay(TapIndicationDelay)
            }
            val pressInteraction = PressInteraction.Press(pressPoint)
            interactionSource.emit(pressInteraction)
            pressedInteraction.value = pressInteraction
        }
        val success = tryAwaitRelease()
        if (delayJob.isActive) {
            delayJob.cancelAndJoin()
            // The press released successfully, before the timeout duration - emit the press
            // interaction instantly. No else branch - if the press was cancelled before the
            // timeout, we don't want to emit a press interaction.
            if (success) {
                val pressInteraction = PressInteraction.Press(pressPoint)
                val releaseInteraction = PressInteraction.Release(pressInteraction)
                interactionSource.emit(pressInteraction)
                interactionSource.emit(releaseInteraction)
            }
        } else {
            pressedInteraction.value?.let { pressInteraction ->
                val endInteraction = if (success) {
                    PressInteraction.Release(pressInteraction)
                } else {
                    PressInteraction.Cancel(pressInteraction)
                }
                interactionSource.emit(endInteraction)
            }
        }
        pressedInteraction.value = null
    }
}

/*
suspend fun PointerInputScope.detectTapMouseGestures(
    onDoubleTap: ((PointerEvent, Offset) -> Unit)? = null,
    onLongPress: ((PointerEvent, Offset) -> Unit)? = null,
    onPress: suspend PressGestureScope.(PointerEvent, Offset) -> Unit = NoPressGesture,
    onTap: ((PointerEvent, Offset) -> Unit)? = null
) = coroutineScope {
    // special signal to indicate to the sending side that it shouldn't intercept and consume
    // cancel/up events as we're only require down events
    val pressScope = PressGestureScopeImpl(this@detectTapMouseGestures)

    forEachGesture {
        awaitPointerEventScope {
            val down = awaitEventFirstDown().also {
                it.changes.forEach { it.consumeDownChange() }
            }
            //val down2: PointerInputChange = awaitFirstDown()
            //down.consumeDownChange()
            pressScope.reset()
            if (onPress !== NoPressGesture) launch {
                pressScope.onPress(down, down.changes.first().position)
            }
            val longPressTimeout = onLongPress?.let {
                viewConfiguration.longPressTimeoutMillis
            } ?: (Long.MAX_VALUE / 2)
            var upOrCancel: PointerEvent? = null
            var upOrCancelChange: PointerInputChange? = null
            try {
                // wait for first tap up or long press
                upOrCancel = withTimeout(longPressTimeout) {
                    waitForUpOrCancellationMouse()
                }
                upOrCancelChange = upOrCancel?.changes?.firstOrNull()
                if (upOrCancel == null) {
                    pressScope.cancel() // tap-up was canceled
                } else {
                    upOrCancelChange?.consumeDownChange()
                    pressScope.release()
                }
            } catch (_: PointerEventTimeoutCancellationException) {
                onLongPress?.invoke(down, down.changes.first().position)
                consumeUntilUp()
                pressScope.release()
            }

            if (upOrCancel != null && upOrCancelChange != null) {
                // tap was successful.
                if (onDoubleTap == null) {
                    onTap?.invoke(upOrCancel, upOrCancelChange.position) // no need to check for double-tap.
                } else {
                    // check for second tap
                    val secondDown = awaitSecondDown(upOrCancelChange)

                    if (secondDown == null) {
                        onTap?.invoke(upOrCancel, upOrCancelChange.position) // no valid second tap started
                    } else {
                        // Second tap down detected
                        pressScope.reset()
                        if (onPress !== NoPressGesture) {
                            launch { pressScope.onPress(upOrCancel, secondDown.position) }
                        }

                        try {
                            // Might have a long second press as the second tap
                            withTimeout(longPressTimeout) {
                                val secondUp = waitForUpOrCancellation()
                                if (secondUp != null) {
                                    secondUp.consumeDownChange()
                                    pressScope.release()
                                    onDoubleTap(upOrCancel, secondUp.position)
                                } else {
                                    pressScope.cancel()
                                    onTap?.invoke(upOrCancel, upOrCancelChange.position)
                                }
                            }
                        } catch (e: PointerEventTimeoutCancellationException) {
                            // The first tap was valid, but the second tap is a long press.
                            // notify for the first tap
                            onTap?.invoke(upOrCancel, upOrCancelChange.position)

                            // notify for the long press
                            onLongPress?.invoke(upOrCancel, secondDown.position)
                            consumeUntilUp()
                            pressScope.release()
                        }
                    }
                }
            }
        }
    }
}
 */