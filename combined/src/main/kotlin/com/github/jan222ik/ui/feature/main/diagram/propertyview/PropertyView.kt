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
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.valudations.ListValidations.inCollection
import com.github.jan222ik.ui.components.inputs.*
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
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
                        it.render(data = selectedElement.umlClass, fReqSelf = foc)
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
                        it.render(data = selectedElement.property, fReqSelf = foc)
                    }

                    is TMM.ModelTree.Diagram -> renderConfig(
                        config = PropertyViewConfigs.pvForDiagram
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
            Visibility(DescriptiveElements.visibility),
            ReadOnlyStringElement(DescriptiveElements.location) { it.nestingPackage?.qualifiedName.toString() }
        )
    )

    val pvForClass: PropertyViewConfig<org.eclipse.uml2.uml.Class> = PropertyViewConfig(
        elements = listOf(
            Nameable(DescriptiveElements.name),
            Labelable(DescriptiveElements.label),
            ReadOnlyStringElement(DescriptiveElements.qualifiedName) { it.qualifiedName },
            PlaceholderElementBool(DescriptiveElements.isAbstract) { it.isAbstract },
            PlaceholderElementBool(DescriptiveElements.isActive) { it.isActive },
            Visibility(DescriptiveElements.visibility),
            // Owned attributes
            // Owned operations
            // Owned reception
        )
    )

    val pvForProperty: PropertyViewConfig<Property> = PropertyViewConfig(
        elements = listOf(
            Nameable(DescriptiveElements.name),
            Labelable(DescriptiveElements.label),
            PlaceholderElementBool(DescriptiveElements.isDerived) { it.isDerived },
            PlaceholderElementBool(DescriptiveElements.isReadOnly) { it.isReadOnly },
            PlaceholderElementBool(DescriptiveElements.isUnique) { it.isUnique },
            PlaceholderElementBool(DescriptiveElements.isOrdered) { it.isOrdered },
            PlaceholderElementBool(DescriptiveElements.isStatic) { it.isStatic },
            Visibility(DescriptiveElements.visibility),
            AggregationElement(DescriptiveElements.aggregation) { it.aggregation.literal },
            PlaceholderElement(DescriptiveElements.multiplicity) {
                val memberEnd = it.association?.memberEnds?.firstOrNull()
                memberEnd?.let { "[${it.lower}, ${it.upper}]" } ?: ""
            },
            PlaceholderElement(DescriptiveElements.defaultValue) {
                it.ownedElements.filterIsInstance<ValueSpecification>().firstOrNull()?.stringValue() ?: ""
            },
            PlaceholderElement(DescriptiveElements.type) {
                it.typeNameString()
            }
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
): IPropertyViewConfigElement<T> {
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

data class AggregationElement<T : EObject>(
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

data class PlaceholderElementBool<T : EObject>(
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

data class Visibility<T : NamedElement>(
    override val propViewElement: IPropertyViewElement
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(data: T, fReqSelf: FocusRequester) {
        val kindByLiteral = VisibilityKind.values().associateBy { it.literal }
        val visibilityItems = kindByLiteral.keys.toList()
        ExpandedDropDownSelection(
            propViewElement = DescriptiveElements.visibility,
            items = visibilityItems,
            initialValue = data.visibility.literal,
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
