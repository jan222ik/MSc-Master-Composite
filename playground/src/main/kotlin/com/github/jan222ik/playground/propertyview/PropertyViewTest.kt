package com.github.jan222ik.playground.propertyview

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import arrow.core.invalidNel
import arrow.core.validNel
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.ValidationItem
import com.github.jan222ik.model.validation.transformations.ITransformation
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.playground.propertyview.KeyHelpers.consumeOnKey
import com.github.jan222ik.playground.propertyview.KeyHelpers.onKeyDown

object DescriptiveElements {
    val multiplicity = PropertyViewDemoElement(
        title = "Multiplicity",
        tooltip = "Multiplicity: is the active logical association when the cardinality of a class in relation to another is being depicted."
    )
    val name = PropertyViewDemoElement(title = "Name:", tooltip = "name: The name of the NamedElement")
    val isDerived = PropertyViewDemoElement(
        title = "Is derived:",
        tooltip = "isDerived: If isDerived is true, the value of the attribute is derived from information elsewhere. Specifies whether the property is derived, i.e., whether its values can be computed from other information."
    )
    val isReadOnly = PropertyViewDemoElement(
        title = "Is read only:",
        tooltip = "isReadOnly: States whether the feature's value may be modified by a client."
    )
    val isUnique = PropertyViewDemoElement(
        title = "Is unique:",
        tooltip = "isUnique: For a multivalued multiplicity, this attributes specifies whether the values in an instantiation of this element are unique."
    )
    val isOrdered = PropertyViewDemoElement(
        title = "Is ordered:",
        tooltip = "isOrdered: For a multivalued multiplicity, this attribute specifies whether the values in an instantiation of this element are sequentially ordered."
    )
    val isStatic = PropertyViewDemoElement(
        title = "Is static:",
        tooltip = "isStatic: Specifies whether this feature characterises individual instances classified by the classifier (false) or the classifier itself (true)"
    )
    val visibility = PropertyViewDemoElement(
        title = "Visibility:",
        tooltip = "visibility: Determines where the NamedElement appears within different Namespaces within the overall model, and its accessibility."
    )
    val type = PropertyViewDemoElement(
        title = "Type:",
        tooltip = "type: This information is derived from the result for this Operation. The type of the TypedElement."
    )
    val defaultValue = PropertyViewDemoElement(
        title = "Default Value:",
        tooltip = "defaultValue: A ValueSpecification that is evaluated to give a default value for the Property when an object of the owning Classifier is instantiated."
    )
    val aggregation = PropertyViewDemoElement(
        title = "Aggregation",
        tooltip = "aggregation: Specifies the kind of aggregation that applies to the Property"
    )
}

class NotAOption(text: String, override val msg: String = "'$text' is not a valid option") : IValidationError

fun <T> inCollection(list: List<T>) = ValidationItem<T, T> {
    this.takeIf { list.contains(this) }?.validNel() ?: NotAOption(text = this.toString()).invalidNel()
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
fun main() {
    var keyListener: ((e: KeyEvent) -> Boolean)? = null
    singleWindowApplication(
        onPreviewKeyEvent = {
            keyListener?.invoke(it) ?: false
        }
    ) {
        val focusManager = LocalFocusManager.current
        LaunchedEffect(Unit) {
            keyListener = {onKeyDown(it) {
                consumeOnKey(Key.Tab) {
                    if (isShiftPressed) {
                        focusManager.moveFocus(FocusDirection.Up)
                    } else {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                }
            }
            }

        }
        val lazyListState = rememberLazyListState()
        val adapter = rememberScrollbarAdapter(lazyListState)
        Row {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                state = lazyListState
            ) {
                item {
                    StingBasedInput(propViewElement = DescriptiveElements.name, initialValue = "")
                }
                item {
                    BooleanToggleWithLabel(
                        propViewElement = DescriptiveElements.isDerived,
                        initialValue = false
                    )
                }
                item {
                    BooleanToggleWithLabel(
                        propViewElement = DescriptiveElements.isReadOnly,
                        initialValue = false
                    )
                }
                item {
                    BooleanToggleWithLabel(
                        propViewElement = DescriptiveElements.isUnique,
                        initialValue = true
                    )
                }
                item {
                    BooleanToggleWithLabel(
                        propViewElement = DescriptiveElements.isOrdered,
                        initialValue = false
                    )
                }
                item {
                    BooleanToggleWithLabel(
                        propViewElement = DescriptiveElements.isStatic,
                        initialValue = false
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
}

interface IPropertyViewElement {
    val title: String
    val tooltip: String
}

data class PropertyViewDemoElement(override val title: String, override val tooltip: String) : IPropertyViewElement


@ExperimentalFoundationApi
@Composable
fun StingBasedInput(
    propViewElement: IPropertyViewElement,
    initialValue: String,
    iTransformation: ITransformation<String, String> = remember { NonTransformer() },
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
        val requester = remember { FocusRequester() }
        TextField(
            modifier = Modifier.focusRequester(requester),
            value = textState.tfv,
            onValueChange = textState::onValueChange,
            isError = textState.errors.isNotEmpty(),
        )
    }
}

@Composable
fun TooltipSurface(content: @Composable () -> Unit) = Surface(elevation = 8.dp, content = content)


@ExperimentalFoundationApi
@Composable
fun TitleWithTooltip(propViewElement: IPropertyViewElement) {
    val visibleState = remember { mutableStateOf(false) }
    TooltipArea(
        tooltip = {
            TooltipSurface {
                Text(propViewElement.tooltip)
            }
        },
        visibleState = visibleState
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = propViewElement.title)
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Show tooltip",
                tint = LocalContentColor.current.copy(alpha = TextFieldDefaults.IconOpacity),
                modifier = Modifier.clickable(role = Role.Image) { visibleState.value = !visibleState.value })
        }
    }
}



@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun BooleanToggleWithLabel(
    propViewElement: IPropertyViewElement,
    initialValue: Boolean,
) {
    var checked by remember(initialValue) { mutableStateOf(initialValue) }
    val requester = remember { FocusRequester() }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleWithTooltip(propViewElement)
        Switch(
            modifier = Modifier.focusRequester(requester)
                .onPreviewKeyEvent {
                    onKeyDown(it) {
                        consumeOnKey(key = Key.Enter) {
                            checked = !checked
                        }
                    }
                },
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}


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

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun MultiplicitySelection(propViewElement: IPropertyViewElement) {
    var isSimple by remember { mutableStateOf(true) }
    Column {
        Crossfade(isSimple) { simple ->
            if (simple) {
                val multiplicityItems = listOf(
                    "0..*", "1..*", "0..1", "1"
                )
                ExpandedDropDownSelection(
                    propViewElement = propViewElement,
                    items = multiplicityItems,
                    initialValue = multiplicityItems.last(),
                    onSelectionChanged = {},
                    transformation = NonTransformer(validations = listOf(inCollection(list = multiplicityItems))),
                )
            } else {
                Column {
                    TitleWithTooltip(propViewElement)
                    Text("Other")
                }
            }
        }
        Icon(
            imageVector = Icons.Filled.SwitchLeft,
            contentDescription = "Switch multiplicity selection",
            modifier = Modifier.clickable { isSimple = !isSimple })
    }
}

enum class ValueSpecificationType {
    LiteralBoolean, LiteralInteger, LiteralReal, LiteralString, LiteralNull
}

@ExperimentalFoundationApi
@Composable
fun ValueSpecificationSelection(
    propViewElement: IPropertyViewElement?
) {
    Column {
        propViewElement?.let { TitleWithTooltip(it) }
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralBoolean, true)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralInteger, 23)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralReal, 2.3)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralString, "test")
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralNull, null)
    }
}

@Composable
fun ValueSpecificationWithValue(type: ValueSpecificationType, value: Any?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = type.name)
        Text(text = "=${value.toString()}")
    }
}


@ExperimentalComposeUiApi
@Composable
fun ShortcutDisplay(keys: List<Key>) {
    @Composable
    fun KeyDisplay(key: Key) {
        Box(
            modifier = Modifier
                .background(color = Color.LightGray)
                .border(
                    width = 1.dp,
                    color = Color.DarkGray
                )
                .clip(shape = RoundedCornerShape(size = 16.dp))
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 3.dp).padding(bottom = 3.dp, top = 1.dp),
                color = Color.White,
                elevation = 5.dp
            ) {
                Text(
                    text = key.nativeKeyCode.takeIf { key != Key.Escape }
                        ?.let { java.awt.event.KeyEvent.getKeyText(it) } ?: "Esc",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEachIndexed { index, key ->
            if (index != 0) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.caption
                )
            }
            KeyDisplay(key)
        }
    }
}