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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.github.jan222ik.ui.feature.main.diagram.canvas.DiagramType
import com.github.jan222ik.ui.value.Space
import kotlin.random.Random

@Composable
fun BreadcrumbsRow(
    modifier: Modifier = Modifier,
    activePath: Array<String>,
    root: BreadCrumbItem
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
    ) {
        Text("Diagrams:")
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                ) {
                    if (find is BreadCrumbItem.Diagram && find.type != null) {
                        Icon(
                            painter = find.type.iconAsPainter(),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                    Text(find.name, modifier = Modifier.clickable {
                        showPopup.value = true
                    })
                }
                if (activePath.last() != pathPart) {
                    Spacer(Modifier.width(Space.dp8))
                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
            parent = find
        }
    }
}

sealed class BreadCrumbItem(
    val name: String,
    val children: List<BreadCrumbItem>
) {
    class Diagram(
        name: String = Random.nextInt().toString(),
        val type: DiagramType? = null,
    ) : BreadCrumbItem(
        name = name,
        children = emptyList()
    )

    class Package(
        name: String = Random.nextInt().toString(),
        children: List<BreadCrumbItem> = emptyList()
    ) : BreadCrumbItem(
        name = name,
        children = children
    )
}


fun main(args: Array<String>) {
    singleWindowApplication {
        BreadcrumbsRow(
            activePath = arrayOf("a", "b", "x"),
            root = BreadCrumbItem.Package(
                name = "root", children = listOf(
                    BreadCrumbItem.Package(
                        name = "a",
                        children = listOf(
                            BreadCrumbItem.Package(
                                name = "b",
                                children = listOf(
                                    BreadCrumbItem.Package(name = "x"),
                                    BreadCrumbItem.Package(),
                                    BreadCrumbItem.Package(),
                                    BreadCrumbItem.Package()
                                ),
                            ),
                            BreadCrumbItem.Package(),
                            BreadCrumbItem.Package(),
                            BreadCrumbItem.Package()
                        )
                    )
                )
            )
        )
    }
}