package com.github.jan222ik.ui.components.inputs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.valudations.ListValidations
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement

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
                    transformation = NonTransformer(validations = listOf(ListValidations.inCollection(list = multiplicityItems))),
                    isReadOnly = !EditorManager.allowEdit.value,
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
