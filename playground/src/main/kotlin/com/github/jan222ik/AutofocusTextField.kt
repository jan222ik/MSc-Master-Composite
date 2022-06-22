package com.github.jan222ik

import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection.*
import androidx.compose.ui.window.singleWindowApplication

enum class CursorSelectionBehaviour {
    START,
    END,
    SELECT_ALL
}

@Composable
fun AutofocusTextFieldExample(
    initValue: String,
    behaviour: CursorSelectionBehaviour = CursorSelectionBehaviour.END
) {
    val direction = LocalLayoutDirection.current
    var tfv by remember {
        val selection = when (behaviour) {
            CursorSelectionBehaviour.START -> {
                if (direction == Ltr) TextRange.Zero else TextRange(initValue.length)
            }
            CursorSelectionBehaviour.END -> {
                if (direction == Ltr) TextRange(initValue.length) else TextRange.Zero
            }
            CursorSelectionBehaviour.SELECT_ALL -> TextRange(0, initValue.length)
        }
        val textFieldValue = TextFieldValue(text = initValue, selection = selection)
        mutableStateOf(textFieldValue)
    }
    val focusRequester = remember { FocusRequester.Default }
    TextField(
        modifier = Modifier.focusRequester(focusRequester),
        value = tfv,
        onValueChange = { tfv = it }
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()

    }
}


fun main() {
    singleWindowApplication {
        CompositionLocalProvider(
            LocalLayoutDirection provides Rtl
        ) {

            AutofocusTextFieldExample("name", behaviour = CursorSelectionBehaviour.END)
        }

    }
}