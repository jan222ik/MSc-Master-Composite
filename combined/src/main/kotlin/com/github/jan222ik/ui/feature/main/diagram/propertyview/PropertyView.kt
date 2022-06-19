package com.github.jan222ik.ui.feature.main.diagram.propertyview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.valudations.ListValidations
import com.github.jan222ik.model.validation.valudations.ListValidations.inCollection
import com.github.jan222ik.ui.components.inputs.*
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.ui.value.descriptions.DescriptiveElements
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.impl.ClassImpl
import org.eclipse.uml2.uml.internal.impl.EnumerationImpl
import org.eclipse.uml2.uml.internal.impl.PrimitiveTypeImpl
import org.eclipse.uml2.uml.internal.impl.PropertyImpl

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PropertyView(
    selectedElement: TMM?
) {
    Column {
        if (selectedElement != null) {
            if (selectedElement is TMM.ModelTree) {
                when (selectedElement) {
                    is TMM.ModelTree.Ecore.TClass -> renderConfig(
                        PropertyViewConfigs.pvForClass
                    ) { it, foc ->
                        it.render(data = selectedElement, fReqSelf = foc)
                    }

                    is TMM.ModelTree.Ecore.TPackage -> renderConfig(
                        PropertyViewConfigs.pvForPackage
                    ) { it, foc ->
                        it.render(data = selectedElement.umlPackage, fReqSelf = foc)
                    }

                    is TMM.ModelTree.Ecore.TPackageImport -> Text("Does not exist yet")
                    is TMM.ModelTree.Ecore.TProperty -> renderConfig(
                        PropertyViewConfigs.pvForProperty,
                    ) { it, foc ->
                        it.render(data = selectedElement, fReqSelf = foc)
                    }

                    is TMM.ModelTree.Diagram -> renderConfig(
                        config = PropertyViewConfigs.pvForDiagram
                    ) { it, foc ->
                        it.render(data = selectedElement, fReqSelf = foc)
                    }

                    is TMM.ModelTree.Ecore.TAssociation -> renderConfig(
                        config = PropertyViewConfigs.pvForAssociation
                    ) { it, foc ->
                        it.render(data = selectedElement, fReqSelf = foc)
                    }

                    else -> {
                        Text("Not properties available for selected uml-item.")
                    }
                }
            } else {
                Text("Not properties available for selected item.")
            }
        } else {
            Text(text = "No element selected.")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> renderConfig(
    config: PropertyViewConfig<T>,
    renderCall: @Composable (IPropertyViewConfigElement<T>, FocusRequester) -> Unit
) {
    Column {
        val shortcutActionsHandler = LocalShortcutActionHandler.current
        val focusManager = LocalFocusManager.current
        DisposableEffect(shortcutActionsHandler, focusManager) {
            val focusDownAction = shortcutActionsHandler.register(
                ShortcutAction.of(
                    Key.Tab,
                    ShortcutAction.KeyModifier.NO_MODIFIERS
                ) {
                    println("Down")
                    focusManager.moveFocus(FocusDirection.Next)
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
        val requesters = remember(config.elements.size) { List(config.elements.size) { FocusRequester() } }

        Row {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                state = lazyListState
            ) {
                config.elements.forEachIndexed { idx, it ->
                    item {
                        renderCall.invoke(it, requesters[idx])
                    }
                }
            }
            VerticalScrollbar(adapter = adapter)
        }
    }
}

object PropertyViewConfigs {

    val pvForPackage: PropertyViewConfig<Package> = PropertyViewConfig(
        elements = listOf(
            Nameable(DescriptiveElements.name),
            Labelable(DescriptiveElements.label),
            PlaceholderElement(DescriptiveElements.uri),
            Visibility(DescriptiveElements.visibility, convert = { it }),
            ReadOnlyStringElement(DescriptiveElements.location) { it.nestingPackage?.qualifiedName.toString() }
        )
    )

    val pvForAssociation: PropertyViewConfig<TMM.ModelTree.Ecore.TAssociation> = PropertyViewConfig(
        elements = listOf(
            MemberEnd(idx = 0),
            MemberEnd(idx = 1),
        )
    )

    val pvForClass: PropertyViewConfig<TMM.ModelTree.Ecore.TClass> = PropertyViewConfig(
        elements = listOf(
            ExternalValidatedState(DescriptiveElements.name, externalValidatedState = { it.name }),
            //Labelable(DescriptiveElements.label),
            ReadOnlyStringElement(DescriptiveElements.qualifiedName) { it.umlClass.qualifiedName },
            PlaceholderElementBool(DescriptiveElements.isAbstract) { it.umlClass.isAbstract },
            PlaceholderElementBool(DescriptiveElements.isActive) { it.umlClass.isActive },
            Visibility(DescriptiveElements.visibility, convert = { it.umlClass }),
            // Owned attributes
            // Owned operations
            // Owned reception
        )
    )

    val pvForProperty: PropertyViewConfig<TMM.ModelTree.Ecore.TProperty> = PropertyViewConfig(
        elements = listOf(
            ExternalValidatedState(DescriptiveElements.name, externalValidatedState = { it.name }),
            //Labelable(DescriptiveElements.label),
            PlaceholderElementBool(DescriptiveElements.isDerived) { it.property.isDerived },
            PlaceholderElementBool(DescriptiveElements.isReadOnly) { it.property.isReadOnly },
            PlaceholderElementBool(DescriptiveElements.isUnique) { it.property.isUnique },
            PlaceholderElementBool(DescriptiveElements.isOrdered) { it.property.isOrdered },
            PlaceholderElementBool(DescriptiveElements.isStatic) { it.property.isStatic },
            Visibility(DescriptiveElements.visibility, convert = { it.property }),
            AggregationElement(DescriptiveElements.aggregation) { it.property.aggregation.literal },
            PlaceholderElement(DescriptiveElements.multiplicity) {
                val memberEnd = it.property.association?.memberEnds?.firstOrNull()
                memberEnd?.let { "[${it.lower}, ${it.upper}]" } ?: ""
            },
            PlaceholderElement(DescriptiveElements.defaultValue) {
                it.property.ownedElements.filterIsInstance<ValueSpecification>().firstOrNull()?.stringValue() ?: ""
            },
            ExternalValidatedStateDropDown(
                DescriptiveElements.type,
                externalValidatedState = {
                    it.type
                }
            )
        )
    )

    val pvForDiagram: PropertyViewConfig<TMM.ModelTree.Diagram> = PropertyViewConfig(
        elements = listOf(
            ExternalValidatedState(
                propViewElement = DescriptiveElements.diagramName,
                externalValidatedState = {
                    it.observed.value.diagramName
                }
            ),
            ReadOnlyStringElement(DescriptiveElements.diagramType) { it.getType() }
        )
    )
}


fun org.eclipse.uml2.uml.Property.typeNameString(): String {
    val t = this.type
    val s = when (t) {
        is PrimitiveTypeImpl -> {
            t.toString().split("#").lastOrNull()?.removeSuffix(")")
        }

        is EnumerationImpl -> t.name ?: t.label
        is ClassImpl -> t.name ?: t.label
        else -> "undefined"
    }
    return s ?: "undefined"
}

data class PropertyViewConfig<T>(val elements: List<IPropertyViewConfigElement<T>>)

interface IPropertyViewConfigElement<T> {
    val propViewElement: IPropertyViewElement

    @Composable
    fun render(data: T, fReqSelf: FocusRequester)
}

data class ExternalValidatedState<T>(
    override val propViewElement: IPropertyViewElement,
    val externalValidatedState: (T) -> ValidatedTextState<String>,
    val isReadOnly: Boolean = false
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        val state = externalValidatedState.invoke(data)
        Column {
            TitleWithTooltip(propViewElement)
            TextField(
                modifier = Modifier
                    .focusOrder(
                        focusRequester = fReqSelf,
                        focusOrderReceiver = {
                            // TODO
                        }
                    ).onFocusChanged {
                        println("FOCUS: ${propViewElement.title} -> $it")
                    },
                value = state.tfv,
                onValueChange = state::onValueChange,
                isError = state.errors.isNotEmpty(),
                readOnly = isReadOnly
            )
        }
    }
}

data class ExternalValidatedStateDropDown<T>(
    override val propViewElement: IPropertyViewElement,
    val externalValidatedState: (T) -> ValidatedTextState<String>,
    val isReadOnly: Boolean = false
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        val state = externalValidatedState.invoke(data)
        val showPopup = remember { mutableStateOf(false) }
        Column {
            val options = listOf(
                "String", "Integer", "Real", "Boolean"
            )
            ExpandedDropDownSelection(
                propViewElement = propViewElement,
                items = options + "Show more",
                initialValue = state.tfv.text,
                onSelectionChanged = { state.onValueChange(TextFieldValue(text = it)) },
                transformation = NonTransformer(validations = listOf(ListValidations.inCollection(list = options))),
                isReadOnly = !EditorManager.allowEdit.value,
                clickableOptions = mapOf(
                    "Show more" to {
                        showPopup.value = true
                    }
                )
            )
            if (showPopup.value) {
                PopupAlertDialogProvider.AlertDialog(
                    onDismissRequest = { showPopup.value = false }
                ) {
                    Surface {
                        Column {
                            Text("The selection for a special type not implemented.")
                        }
                    }
                }
            }
        }
    }
}

data class MemberEnd(
    override val propViewElement: IPropertyViewElement = DescriptiveElements.memberEnd,
    val idx: Int
) : IPropertyViewConfigElement<TMM.ModelTree.Ecore.TAssociation> {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(data: TMM.ModelTree.Ecore.TAssociation, fReqSelf: FocusRequester) {
        val validatedTextState = if (idx == 0) data.memberEnd0MultiplicityString else data.memberEnd1MultiplicityString
        Card(border = BorderStroke(0.dp, color = Color.Black)) {
            Column(modifier = Modifier.padding(vertical = Space.dp8, horizontal = Space.dp4)) {
                TitleWithTooltip(propViewElement, titleAdditions = " $idx")
                StingBasedInput(
                    isReadOnly = true,
                    propViewElement = DescriptiveElements.name,
                    initialValue = data.association.memberEnds[idx].name,
                    focusRequester = fReqSelf,
                    focusOrderReceiver = {
                        // TODO
                    }
                )
                MultiplicitySelection(
                    propViewElement = DescriptiveElements.multiplicity,
                    multiplicityItems = defaultMultiplicityStrings,
                    value = validatedTextState.tfv.text,
                    hideOtherSwitch = true,
                    onValueChanged = {
                        if (idx == 0) {
                            data.memberEnd0MultiplicityString
                        } else {
                            data.memberEnd1MultiplicityString
                        }.onValueChange(TextFieldValue(text = it))
                    }
                )
            }
        }
        Spacer(Modifier.height(Space.dp4))
    }
}

data class PlaceholderElement<T>(
    override val propViewElement: IPropertyViewElement,
    val onValidValue: ((T, String, Boolean) -> Unit)? = null,
    val extractValueAsString: (T) -> String = { "" }
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            isReadOnly = !EditorManager.allowEdit.value,
            propViewElement = propViewElement,
            initialValue = extractValueAsString(data),
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            },
            onValidValue = onValidValue?.let { { v, b -> onValidValue.invoke(data, v, b) } }
        )
    }
}

data class AggregationElement<T>(
    override val propViewElement: IPropertyViewElement,
    val extractValueAsString: (T) -> String
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        val kindByLiteral = AggregationKind.values().associateBy { it.literal }
        val visibilityItems = kindByLiteral.keys.toList()
        ExpandedDropDownSelection(
            propViewElement = DescriptiveElements.aggregation,
            items = visibilityItems,
            initialValue = extractValueAsString(data),
            onSelectionChanged = {},
            transformation = NonTransformer(validations = listOf(inCollection(list = visibilityItems))),
            isReadOnly = !EditorManager.allowEdit.value
        )
    }
}

data class PlaceholderElementBool<T>(
    override val propViewElement: IPropertyViewElement,
    val extractValueAsBoolean: (T) -> Boolean
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        BooleanToggleWithLabel(
            propViewElement = propViewElement,
            initialValue = extractValueAsBoolean(data),
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            },
            isReadOnly = !EditorManager.allowEdit.value
        )
    }
}

data class ReadOnlyStringElement<T>(
    override val propViewElement: IPropertyViewElement,
    val extractValueAsString: (T) -> String = { it.toString() }
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = extractValueAsString(data),
            isReadOnly = true,
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            },
            onValidValue = null
        )
    }
}

data class Nameable<T : NamedElement>(
    override val propViewElement: IPropertyViewElement,
    val onValidValue: ((String, Boolean) -> Unit)? = null
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = data.name ?: "null",
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            },
            isReadOnly = !EditorManager.allowEdit.value,
            onValidValue = onValidValue
        )
    }

}

data class Labelable<T : NamedElement>(
    override val propViewElement: IPropertyViewElement,
    val onValidValue: ((String, Boolean) -> Unit)? = null,
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = data.label ?: "null",
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            },
            isReadOnly = !EditorManager.allowEdit.value,
            onValidValue = onValidValue
        )
    }

}

data class Visibility<T, N : NamedElement>(
    override val propViewElement: IPropertyViewElement,
    val convert: (T) -> N
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        val kindByLiteral = VisibilityKind.values().associateBy { it.literal }
        val visibilityItems = kindByLiteral.keys.toList()
        ExpandedDropDownSelection(
            propViewElement = DescriptiveElements.visibility,
            items = visibilityItems,
            initialValue = convert(data).visibility.literal,
            onSelectionChanged = {},
            transformation = NonTransformer(validations = listOf(inCollection(list = visibilityItems))),
            isReadOnly = !EditorManager.allowEdit.value
        )
    }

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun demo() {
    Column {
        val shortcutActionsHandler = LocalShortcutActionHandler.current
        val focusManager = LocalFocusManager.current
        DisposableEffect(shortcutActionsHandler, focusManager) {
            val focusDownAction = shortcutActionsHandler.register(
                ShortcutAction.of(
                    Key.Tab,
                    ShortcutAction.KeyModifier.NO_MODIFIERS
                ) {
                    println("Down")
                    focusManager.moveFocus(FocusDirection.Next)
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
        val (focusName, focusIsDerived, focusIsReadOnly, focusIsUnique, focusIsOrdered, focusIsStatic) = remember { FocusRequester.createRefs() }

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
                        },
                        isReadOnly = !EditorManager.allowEdit.value
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
                        },
                        isReadOnly = !EditorManager.allowEdit.value
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
                        },
                        isReadOnly = !EditorManager.allowEdit.value
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
                        },
                        isReadOnly = !EditorManager.allowEdit.value
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
                        },
                        isReadOnly = !EditorManager.allowEdit.value
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
                        transformation = NonTransformer(validations = listOf(inCollection(list = visibilityItems))),
                        isReadOnly = !EditorManager.allowEdit.value
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
                        transformation = NonTransformer(validations = listOf(inCollection(list = aggregationItems))),
                        isReadOnly = !EditorManager.allowEdit.value
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
