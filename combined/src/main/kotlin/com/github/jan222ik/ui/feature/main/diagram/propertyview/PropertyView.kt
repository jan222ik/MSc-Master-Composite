package com.github.jan222ik.ui.feature.main.diagram.propertyview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.valudations.ListValidations.inCollection
import com.github.jan222ik.ui.components.inputs.*
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.main.tree.ModelTreeItem
import com.github.jan222ik.ui.feature.main.tree.TreeDisplayableItem
import com.github.jan222ik.ui.value.descriptions.DescriptiveElements
import com.github.jan222ik.util.KeyHelpers.consumeOnKey
import com.github.jan222ik.util.KeyHelpers.onKeyDown

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PropertyView(
    selectedElement: TreeDisplayableItem?
) {
    Column {
        Text("Properties:")
        if (selectedElement != null) {
            Text(text = "Current Element:$selectedElement")
            if (selectedElement is ModelTreeItem) {
                demo()
            } else demo()
        } else {
            Text(text = "No element selected.")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun demo() {
    val shortcutActionsHandler = LocalShortcutActionHandler.current
    val focusManager = LocalFocusManager.current
    DisposableEffect(shortcutActionsHandler, focusManager) {
        val focusDownAction = shortcutActionsHandler.register(
            ShortcutAction.of(
                Key.Tab,
                ShortcutAction.KeyModifier.NO_MODIFIERS
            ) {
                println("Down")
                focusManager.moveFocus(FocusDirection.Down)
                true
            })
        val focusUpAction = shortcutActionsHandler.register(
            ShortcutAction.of(
                Key.Tab,
                ShortcutAction.KeyModifier.SHIFT
            ) {
                println("Up")
                focusManager.moveFocus(FocusDirection.Previous)
                true
            })
        onDispose {
            shortcutActionsHandler.deregister(focusUpAction)
            shortcutActionsHandler.deregister(focusDownAction)
        }
    }
    val lazyListState = rememberLazyListState()
    val adapter = rememberScrollbarAdapter(lazyListState)
    val (focusName, focusIsDerived, focusIsReadOnly, focusIsUnique, focusIsOrdered, focusIsStatic) = FocusRequester.createRefs()
    Row {
        LazyColumn(
            modifier = Modifier.padding(24.dp),
            state = lazyListState
        ) {
            item {
                StingBasedInput(
                    propViewElement = DescriptiveElements.name,
                    initialValue = "",
                    focusRequester = focusName,
                    focusOrderReceiver = {
                        this.
                        next = focusIsDerived
                        down = focusIsDerived
                        previous = focusIsStatic
                    }
                )
            }
            item {
                BooleanToggleWithLabel(
                    propViewElement = DescriptiveElements.isDerived,
                    initialValue = false,
                    focusRequester = focusIsDerived,
                    focusOrderReceiver = {
                        next = focusIsReadOnly
                        down = focusIsReadOnly
                        previous = focusName
                    }
                )
            }
            item {
                BooleanToggleWithLabel(
                    propViewElement = DescriptiveElements.isReadOnly,
                    initialValue = false,
                    focusRequester = focusIsReadOnly,
                    focusOrderReceiver = {
                        next = focusIsUnique
                        down = focusIsUnique
                    }
                )
            }
            item {
                BooleanToggleWithLabel(
                    propViewElement = DescriptiveElements.isUnique,
                    initialValue = true,
                    focusRequester = focusIsUnique,
                    focusOrderReceiver = {
                        next = focusIsOrdered
                        down = focusIsOrdered
                    }
                )
            }
            item {
                BooleanToggleWithLabel(
                    propViewElement = DescriptiveElements.isOrdered,
                    initialValue = false,
                    focusRequester = focusIsOrdered,
                    focusOrderReceiver = {
                        next = focusIsStatic
                        down = focusIsStatic
                    }
                )
            }
            item {
                BooleanToggleWithLabel(
                    propViewElement = DescriptiveElements.isStatic,
                    initialValue = false,
                    focusRequester = focusIsStatic,
                    focusOrderReceiver = {
                        next = focusName
                        down = focusName
                    }
                )
            }

            item {
                val visibilityItems = listOf(
                    "public", "private", "protected", "package"
                )
                ExpandedDropDownSelection(
                    propViewElement = DescriptiveElements.visibility,
                    items = visibilityItems,
                    initialValue = visibilityItems.first(),
                    onSelectionChanged = {},
                    transformation = NonTransformer(validations = listOf(inCollection(list = visibilityItems)))
                )
            }
            item {
                val aggregationItems = listOf(
                    "none", "shared", "composite"
                )
                ExpandedDropDownSelection(
                    propViewElement = DescriptiveElements.aggregation,
                    items = aggregationItems,
                    initialValue = aggregationItems.first(),
                    onSelectionChanged = {},
                    transformation = NonTransformer(validations = listOf(inCollection(list = aggregationItems)))
                )
            }
            item {
                MultiplicitySelection(
                    propViewElement = DescriptiveElements.multiplicity
                )
            }
            item {
                ValueSpecificationSelection(DescriptiveElements.defaultValue)
            }
        }
        VerticalScrollbar(adapter = adapter)
    }

}
