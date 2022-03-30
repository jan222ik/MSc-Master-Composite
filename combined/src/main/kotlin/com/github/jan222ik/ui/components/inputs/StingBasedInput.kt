package com.github.jan222ik.ui.components.inputs


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusOrder
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.ITransformation
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement

@ExperimentalFoundationApi
@Composable
fun StingBasedInput(
    propViewElement: IPropertyViewElement,
    initialValue: String,
    iTransformation: ITransformation<String, String> = remember { NonTransformer() },
    focusRequester: FocusRequester,
    focusOrderReceiver: FocusOrder.() -> Unit
) {
    Column {
        TitleWithTooltip(propViewElement)
        val textState = remember(initialValue) {
            ValidatedTextState(
                initial = initialValue,
                transformation = iTransformation,
                onValidValue = null
            )
        }
        TextField(
            modifier = Modifier
                .focusOrder(
                focusRequester = focusRequester,
                focusOrderReceiver = focusOrderReceiver
            ).onFocusChanged {
                    println("FOCUS: ${propViewElement.title} -> $it")
                },
            value = textState.tfv,
            onValueChange = textState::onValueChange,
            isError = textState.errors.isNotEmpty(),
        )
    }
}