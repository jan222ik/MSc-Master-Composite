package com.github.jan222ik.playground.propertyview

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.ITransformation
import com.github.jan222ik.model.validation.transformations.NonTransformer

object Elements {
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

fun main() {
    singleWindowApplication {
        Column(modifier = Modifier.padding(24.dp)) {
            StingBasedInput(propViewElement = Elements.name, initialValue = "")
            BooleanToggleWithLabel(propViewElement = Elements.isDerived, initialValue = false)
            BooleanToggleWithLabel(propViewElement = Elements.isReadOnly, initialValue = false)
            BooleanToggleWithLabel(propViewElement = Elements.isUnique, initialValue = true)
            BooleanToggleWithLabel(propViewElement = Elements.isOrdered, initialValue = false)
            BooleanToggleWithLabel(propViewElement = Elements.isStatic, initialValue = false)

            VisibilityDropDownSelection(
                propViewElement = Elements.visibility,
                items = listOf(
                    "public", "private", "protected", "package"
                ),
                initialValue = "public",
                onSelectionChanged = {}
            )
            VisibilityDropDownSelection(
                propViewElement = Elements.aggregation,
                items = listOf(
                    "none", "shared", "composite"
                ),
                initialValue = "none",
                onSelectionChanged = {}
            )
            MultiplicitySelection(
                propViewElement = Elements.multiplicity
            )
        }
    }
}

interface IPropertyViewElement {
    val title: String
    val tooltip: String
}

data class PropertyViewDemoElement(override val title: String, override val tooltip: String) : IPropertyViewElement

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StingBasedInput(
    propViewElement: IPropertyViewElement,
    initialValue: String,
    iTransformation: ITransformation<String, String> = remember { NonTransformer() }
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
            value = textState.tfv,
            onValueChange = textState::onValueChange,
            isError = textState.errors.isNotEmpty(),
        )
    }
}

@Composable
fun TooltipSurface(content: @Composable () -> Unit) = Surface(elevation = 8.dp, content = content)

@OptIn(ExperimentalFoundationApi::class)
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
                modifier = Modifier.clickable { visibleState.value = !visibleState.value })
        }
    }
}


@Composable
fun BooleanToggleWithLabel(
    propViewElement: IPropertyViewElement,
    initialValue: Boolean
) {
    var checked by remember(initialValue) { mutableStateOf(initialValue) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleWithTooltip(propViewElement)
        Switch(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}


@Composable
fun VisibilityDropDownSelection(
    modifier: Modifier = Modifier,
    propViewElement: IPropertyViewElement?,
    items: List<String>,
    initialValue: String,
    onSelectionChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember(initialValue) { mutableStateOf(initialValue) }

    var dropDownWidth by remember { mutableStateOf(0) }

    val icon = if (expanded)
        Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown


    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                selectedText = it
                onSelectionChanged.invoke(it)
            },
            modifier = Modifier.width(IntrinsicSize.Min)
                .onSizeChanged {
                    dropDownWidth = it.width
                },
            label = propViewElement?.let { { TitleWithTooltip(propViewElement) } },
            trailingIcon = {
                Icon(icon, "open dropdown", Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() })
        ) {
            items.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedText = label
                }) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
fun MultiplicitySelection(propViewElement: IPropertyViewElement) {
    var isSimple by remember { mutableStateOf(true) }
    Column {
        TitleWithTooltip(propViewElement)
        Crossfade(isSimple) { simple ->
            if (simple) {
                VisibilityDropDownSelection(
                    propViewElement = null,
                    items = listOf(
                        "0..*", "1..*", "0..1", "1"
                    ),
                    initialValue = "1",
                    onSelectionChanged = {}
                )
            } else {
                Text("Other")
            }
        }
        Icon(
            imageVector = Icons.Filled.SwitchLeft,
            contentDescription = "Switch multiplicity selection",
            modifier = Modifier.clickable { isSimple = !isSimple })
    }
}