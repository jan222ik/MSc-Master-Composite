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
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
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
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Property
import org.eclipse.uml2.uml.VisibilityKind

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
                when (selectedElement) {
                    is ModelTreeItem.ClassItem -> renderConfig(selectedElement.tmmTClass.umlClass, PropertyViewConfigs.pvForClass)
                    is ModelTreeItem.ImportItem -> Text("Does not exist yet")
                    is ModelTreeItem.PackageItem -> renderConfig(
                        selectedElement.tmmTPackage.umlPackage,
                        PropertyViewConfigs.pvForPackage
                    )
                    is ModelTreeItem.PropertyItem -> renderConfig(
                        selectedElement.tmmTProperty.property,
                        PropertyViewConfigs.pvForProperty
                    )
                    is ModelTreeItem.ValueItem -> Text("Does not exist yet")
                }
            } else Text("Not properties available for selected item.")
        } else {
            Text(text = "No element selected.")
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : EObject> renderConfig(umlElement: T, config: PropertyViewConfig<T>) {
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
                        it.render(umlElement = umlElement, fReqSelf = requesters[idx])
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
            PlaceholderElementBool(DescriptiveElements.isAbstract),
            PlaceholderElementBool(DescriptiveElements.isActive),
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
            PlaceholderElementBool(DescriptiveElements.isDerived),
            PlaceholderElementBool(DescriptiveElements.isReadOnly),
            PlaceholderElementBool(DescriptiveElements.isUnique),
            PlaceholderElementBool(DescriptiveElements.isOrdered),
            PlaceholderElementBool(DescriptiveElements.isStatic),
            Visibility(DescriptiveElements.visibility),
            PlaceholderElement(DescriptiveElements.aggregation),
            PlaceholderElement(DescriptiveElements.multiplicity),
            PlaceholderElement(DescriptiveElements.defaultValue),
        )
    )
}

data class PropertyViewConfig<T : EObject>(val elements: List<IPropertyViewConfigElement<T>>)

interface IPropertyViewConfigElement<T : EObject> {
    val propViewElement: IPropertyViewElement

    @Composable
    fun render(umlElement: T, fReqSelf: FocusRequester)
}

data class PlaceholderElement<T : EObject>(
    override val propViewElement: IPropertyViewElement
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = "",
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            }
        )
    }
}

data class PlaceholderElementBool<T : EObject>(
    override val propViewElement: IPropertyViewElement,
    val extractValueAsBoolean: (T) -> Boolean = { false }
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        BooleanToggleWithLabel(
            propViewElement = propViewElement,
            initialValue = extractValueAsBoolean(umlElement),
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            }
        )
    }
}

data class ReadOnlyStringElement<T : EObject>(
    override val propViewElement: IPropertyViewElement,
    val extractValueAsString: (T) -> String = { it.toString() }
) : IPropertyViewConfigElement<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = extractValueAsString(umlElement),
            isReadOnly = true,
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            }
        )
    }
}

data class Nameable<T : NamedElement>(
    override val propViewElement: IPropertyViewElement
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = umlElement.name,
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            }
        )
    }

}

data class Labelable<T : NamedElement>(
    override val propViewElement: IPropertyViewElement
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        StingBasedInput(
            propViewElement = propViewElement,
            initialValue = umlElement.label,
            focusRequester = fReqSelf,
            focusOrderReceiver = {
                // TODO
            }
        )
    }

}

data class Visibility<T : NamedElement>(
    override val propViewElement: IPropertyViewElement
) : IPropertyViewConfigElement<T> {


    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun render(umlElement: T, fReqSelf: FocusRequester) {
        val kindByLiteral = VisibilityKind.values().associateBy { it.literal }
        val visibilityItems = kindByLiteral.keys.toList()
        ExpandedDropDownSelection(
            propViewElement = DescriptiveElements.visibility,
            items = visibilityItems,
            initialValue = umlElement.visibility.literal,
            onSelectionChanged = {},
            transformation = NonTransformer(validations = listOf(inCollection(list = visibilityItems)))
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

}
