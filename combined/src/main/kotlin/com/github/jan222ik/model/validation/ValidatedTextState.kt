package com.github.jan222ik.model.validation

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import com.github.jan222ik.model.validation.transformations.ITransformation
import com.github.jan222ik.model.validation.transformations.Transformer


class ValidatedTextState<out R>(
    private val initial: String,
    private val transformation: ITransformation<String, R>,
    private val onValidValue: ((R, Boolean) -> Unit)? = null
) {

    var tfv by mutableStateOf(TextFieldValue(initial))
        private set

    var errors by mutableStateOf<List<IValidationError>>(emptyList())
        private set

    init {
        // Initial Validation
        validate(nextTfv = tfv, notifyValid = false)
    }

    fun onValueChange(nextTfv: TextFieldValue) {
        tfv = nextTfv
        validate(nextTfv = nextTfv, notifyValid = true)
    }

    private fun validate(nextTfv: TextFieldValue, notifyValid: Boolean) {
        val validateAll = transformation.transform(nextTfv.text)
        validateAll.toEither()
            .tapLeft { errs ->
                errors = errs
            }
            .tap {
                errors = emptyList()
                if (notifyValid) {
                    onValidValue?.invoke(it, nextTfv.text == initial)
                }
            }
    }
}

@Composable
fun <T : Any> rememberTextState(
    text: String,
    transformation: Transformer<String, T>,
    vararg keys: Any? = emptyArray(),
    onValidValue: ((T, Boolean) -> Unit)? = null,
): ValidatedTextState<T> {
    return remember(text, transformation, onValidValue, *keys) {
        ValidatedTextState(
            initial = text,
            onValidValue = onValidValue,
            transformation = transformation
        )
    }
}