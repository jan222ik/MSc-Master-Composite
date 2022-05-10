package com.github.jan222ik.ui.components.breadcrumbs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ui.value.Space
import kotlin.random.Random

@Composable
fun BreadcrumbsRow(
    activePath: Array<String>,
    root: DiagramBreadcrumbItem
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
    ) {
        Text("Diagrams")
        var parent = root
        for (pathPart in activePath) {
            val find = parent.children.find { it.name == pathPart } ?: break
            val showPopup = remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownMenu(
                    expanded = showPopup.value,
                    onDismissRequest = { showPopup.value = false },
                    focusable = true,
                ) {
                    parent.children.forEach {
                        DropdownMenuItem(
                            modifier = Modifier.scale(0.75f),
                            onClick = {
                                // TODO
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(it.name)
                        }
                    }
                }
                Text(find.name, modifier = Modifier.clickable {
                    showPopup.value = true
                })
                if (activePath.last() != pathPart) {
                    Spacer(Modifier.width(Space.dp8))
                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
            parent = find
        }
    }
}

class DiagramBreadcrumbItem(
    val name: String = Random.nextInt().toString(),
    val children: List<DiagramBreadcrumbItem> = emptyList()
)

fun main(args: Array<String>) {
    singleWindowApplication {
        BreadcrumbsRow(
            activePath = arrayOf("a", "b", "x"),
            root = DiagramBreadcrumbItem(
                "root", children = listOf(
                    DiagramBreadcrumbItem(
                        name = "a",
                        children = listOf(
                            DiagramBreadcrumbItem(
                                name = "b",
                                children = listOf(
                                    DiagramBreadcrumbItem(name = "x"),
                                    DiagramBreadcrumbItem(),
                                    DiagramBreadcrumbItem(),
                                    DiagramBreadcrumbItem()
                                ),
                            ),
                            DiagramBreadcrumbItem(),
                            DiagramBreadcrumbItem(),
                            DiagramBreadcrumbItem()
                        )
                    )
                )
            )
        )
    }
}