package com.github.jan222ik.ui.feature.main.diagram.propertyview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.github.jan222ik.model.validation.rememberTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.valudations.Rules
import com.github.jan222ik.ui.feature.main.tree.ModelTreeItem
import com.github.jan222ik.ui.feature.main.tree.TreeDisplayableItem

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
                    is ModelTreeItem.ClassItem -> {
                        Text("Name:")
                        val trans = remember { NonTransformer<String>(validations = listOf(Rules.StringBased.checkForContainedWhiteSpace)) }
                        val state = rememberTextState(selectedElement.umlClass.name, trans)
                        TextField(
                            value = state.tfv,
                            onValueChange = state::onValueChange
                        )
                    }
                    is ModelTreeItem.ImportItem -> TODO()
                    is ModelTreeItem.PackageItem -> TODO()
                    is ModelTreeItem.PropertyItem -> TODO()
                    is ModelTreeItem.ValueItem -> TODO()
                }
            }
        } else {
            Text(text = "No element selected.")
        }
    }
}