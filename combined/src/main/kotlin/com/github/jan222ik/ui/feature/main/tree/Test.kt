package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    var onAction: (() -> Unit)? = null
    singleWindowApplication(
        onPreviewKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.isCtrlPressed && it.key == Key.A) {
                println("Pressed")
                onAction?.invoke()
                true
            } else false
        }
    ) {
        val comp = remember {
            ComponentWithState(listOf("A", "B", "C", "D", "E", "F")).also { onAction = it::selectAll }
        }
        comp.render()
    }
}

class ComponentWithState(
    private val items: List<String>
) {
    private var selected by mutableStateOf(emptyList<Int>())

    fun selectAll() {
        println("Select all")
        selected = items.indices.toList()
    }

    private fun selectItem(idx: Int) {
        selected = if (selected.contains(idx)) {
            selected.toMutableList().apply { remove(element = idx) }
        } else {
            selected + idx
        }
    }

    @Composable
    fun render() {
        LazyColumn {
            items(count = items.size) { idx ->
                val item = items[idx]
                val isSelected = remember(idx, selected) {
                    selected.contains(idx)
                }
                val baseMod = Modifier.clickable {
                    selectItem(idx)
                }
                Text(
                    modifier = baseMod.takeUnless { isSelected } ?: baseMod.border(1.dp, Color.Cyan),
                    text = item
                )
            }
        }
    }
}