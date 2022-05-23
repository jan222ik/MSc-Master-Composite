package com.github.jan222ik.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Switch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import com.github.jan222ik.util.KeyHelpers.onKeyDown

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun BooleanToggleWithLabel(
    propViewElement: IPropertyViewElement,
    initialValue: Boolean,
    focusRequester: FocusRequester,
    focusOrderReceiver: FocusOrder.() -> Unit,
    isReadOnly: Boolean
) {
    var checked by remember(initialValue) { mutableStateOf(initialValue) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleWithTooltip(propViewElement)
        Switch(
            modifier = Modifier.focusOrder(focusRequester, focusOrderReceiver)
                .onPreviewKeyEvent {
                    onKeyDown(it) {
                        consumeOnKey(key = Key.Enter) {
                            checked = !checked
                        }
                    }
                }.onFocusChanged {
                    println("FOCUS: ${propViewElement.title} -> $it")
                },
            checked = checked,
            onCheckedChange = { checked = it },
            enabled = !isReadOnly
        )
    }
}