package com.github.jan222ik.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.transformations.ITransformation
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import com.github.jan222ik.util.KeyHelpers.onKeyDown

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun ExpandedDropDownSelection(
    modifier: Modifier = Modifier,
    propViewElement: IPropertyViewElement?,
    items: List<String>,
    initialValue: String,
    onSelectionChanged: (String) -> Unit,
    transformation: ITransformation<String, String>? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember(initialValue) { mutableStateOf(initialValue) }
    var errors by remember { mutableStateOf(emptyList<IValidationError>()) }

    var dropDownWidth by remember { mutableStateOf(0) }

    val icon = if (expanded)
        Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown

    val requester = remember { FocusRequester() }

    fun validate(s: String) {
        if (transformation != null) {
            val answer = transformation.transform(s)
            answer
                .tapInvalid { iValidationErrors -> errors = iValidationErrors }
                .tap { validatedString ->
                    errors = emptyList()
                    onSelectionChanged.invoke(validatedString)
                }
        } else {
            onSelectionChanged.invoke(s)
        }
    }


    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                selectedText = it
                validate(it)
            },
            modifier = Modifier.width(IntrinsicSize.Min)
                .onSizeChanged {
                    dropDownWidth = it.width
                }
                .focusRequester(requester)
                .onPreviewKeyEvent {
                    onKeyDown(it) {
                        consumeOnKey(key = Key.Enter) {
                            expanded = !expanded
                        }
                    }
                },
            label = propViewElement?.let { { TitleWithTooltip(propViewElement) } },
            trailingIcon = {
                Icon(
                    modifier = Modifier.clickable { expanded = !expanded },
                    imageVector = icon,
                    contentDescription = "open dropdown"
                )
            },
            isError = errors.isNotEmpty(),
        )
        errors.forEach {
            Text(text = it.msg, color = MaterialTheme.colors.error)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() })
        ) {
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShortcutDisplay(keys = listOf(Key.Escape))
                Text(text = "to cancel", style = MaterialTheme.typography.caption)
            }
            items.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedText = label
                    validate(label)
                    expanded = false
                }) {
                    Text(text = label)
                }
            }
        }
    }
}