package com.github.jan222ik.ui.components.breadcrumbs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.TMMPath
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.value.Space

@Composable
fun BreadcrumbsRow(
    modifier: Modifier = Modifier,
    activePath: TMMPath<*>,
) {
    val changedPath = remember(activePath) { mutableStateOf(activePath) }
    val commandStackHandler = LocalCommandStackHandler.current
    val activePathChangeBehaviour = remember { mutableStateOf<Int?>(null) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.dp4)
    ) {

        @Composable
        fun BreadCrumbItem(parent: TMM.IHasChildren<*>?, pathIndex: Int) {
            // Recursion Stop condition
            if (pathIndex > changedPath.value.nodes.lastIndex) return
            val showPopup = remember { mutableStateOf(false) }
            val showArrowPopup = remember { mutableStateOf(false) }
            LaunchedEffect(activePathChangeBehaviour.value) {
                showArrowPopup.value = activePathChangeBehaviour.value == pathIndex
            }
            val activeItem =
                remember(pathIndex, parent, activePath, changedPath.value) { changedPath.value.nodes[pathIndex] }
            if (activeItem is TMM.ModelTree.Diagram) {
                BreadCrumbIcon(activeItem)
            }
            Text(
                text = activeItem.displayName,
                modifier = Modifier.clickable {
                    showPopup.value = true
                },
                color = if (activeItem is TMM.IBreadCrumbDisplayableMarker && activeItem.isActive()) {
                    Color.Unspecified
                } else LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            )
            parent?.let {
                BreadCrumbDropDown(
                    visible = showPopup.value,
                    options = it.children.filterIsInstance<TMM.IBreadCrumbDisplayableMarker>(),
                    onDismissRequest = { showPopup.value = false },
                    onSelectionChanged = { tmm ->
                        if (tmm is TMM.ModelTree.Diagram) {
                            EditorManager.moveToOrOpenDiagram(
                                tmmDiagram = tmm,
                                commandStackHandler = commandStackHandler
                            )
                            showPopup.value = false
                            changedPath.value =
                                TMMPath(nodes = changedPath.value.nodes.subList(0, pathIndex) + tmm, tmm)
                        } else {
                            changedPath.value =
                                TMMPath(nodes = changedPath.value.nodes.subList(0, pathIndex) + tmm, tmm)
                            activePathChangeBehaviour.value = changedPath.value.nodes.lastIndex
                            showPopup.value = false
                        }
                    }
                )
            }
            if (activeItem is TMM.IHasChildren<*> && activeItem.children.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        showArrowPopup.value = true
                    }
                )
                BreadCrumbDropDown(
                    visible = showArrowPopup.value,
                    options = activeItem.children.filterIsInstance<TMM.IBreadCrumbDisplayableMarker>(),
                    onDismissRequest = { showArrowPopup.value = false },
                    onSelectionChanged = { tmm ->
                        if (tmm is TMM.ModelTree.Diagram) {
                            EditorManager.moveToOrOpenDiagram(
                                tmmDiagram = tmm,
                                commandStackHandler = commandStackHandler
                            )
                            showArrowPopup.value = false
                            changedPath.value =
                                TMMPath(nodes = changedPath.value.nodes.subList(0, pathIndex.inc()) + tmm, tmm)
                        } else {
                            changedPath.value =
                                TMMPath(nodes = changedPath.value.nodes.subList(0, pathIndex.inc()) + tmm, tmm)
                            activePathChangeBehaviour.value = changedPath.value.nodes.lastIndex
                            showPopup.value = false
                        }
                    }
                )
                BreadCrumbItem(
                    parent = activeItem,
                    pathIndex = pathIndex.inc()
                )
            }
        }
        Text("Diagrams:")

        BreadCrumbItem(
            parent = null,
            pathIndex = 0
        )

        /*
        if (root !is TMM.ITMMHasChildren<*>) {

        } else {
            var parent: TMM.ITMMHasChildren<*> = root
            for (pathPart in activePath.nodes) {
                val find = parent.children.find { it == pathPart } ?: break
                val showPopup = remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BreadCrumbDropDown(
                        visible = showPopup.value,
                        options = parent.children,
                        onDismissRequest = { showPopup.value = false },
                        onSelectionChanged = {

                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                    ) {
                        if (find is TMM.ModelTree.Diagram) {
                            Icon(
                                painter = find.initDiagram.diagramType.iconAsPainter(),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                        Text(find.toString(), modifier = Modifier.clickable {
                            showPopup.value = true
                        })
                        if (find is TMM.ITMMHasChildren<*> && (activePath.target != pathPart || find.children.isNotEmpty())) {
                            Spacer(Modifier.width(Space.dp8))
                            val next = remember { mutableStateOf(false) }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null, modifier = Modifier.clickable {
                                    next.value = true
                                })
                            BreadCrumbDropDown(
                                options = find.children,
                                visible = next.value,
                                onDismissRequest = { next.value = false },
                                onSelectionChanged = {}
                            )
                        }
                    }
                }
                parent = find
            }
        }

         */
    }
}

@Composable
fun BreadCrumbDropDown(
    visible: Boolean,
    options: List<TMM.IBreadCrumbDisplayableMarker>,
    onDismissRequest: () -> Unit,
    onSelectionChanged: (TMM) -> Unit
) {
    Box(Modifier.size(0.dp)) {
        DropdownMenu(
            expanded = visible,
            onDismissRequest = onDismissRequest,
            focusable = true,
        ) {
            ProvideTextStyle(TextStyle(fontSize = 12.sp)) {
                Column() {
                    options.forEach { it ->
                        val tmm = it.markerAsTMM()
                        DropdownMenuItem(
                            onClick = {
                                if (it.isActive()) {
                                    onSelectionChanged.invoke(tmm)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = Space.dp8)
                        ) {
                            ProvideTextStyle(TextStyle(fontSize = 12.sp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.dp8)
                                ) {
                                    BreadCrumbIcon(tmm)
                                    Text(
                                        text = tmm.displayName,
                                        color = when {
                                            it.isActive() -> Color.Unspecified
                                            else -> LocalContentColor.current.copy(ContentAlpha.disabled)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadCrumbIcon(tmm: TMM) {
    if (tmm is TMM.ModelTree.Diagram) {
        Icon(
            modifier = Modifier.size(Space.dp16),
            painter = tmm.initDiagram.diagramType.iconAsPainter(),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
    if (tmm is TMM.ModelTree.Ecore.TModel) {
        Icon(
            modifier = Modifier.size(Space.dp16),
            painter = painterResource("drawables/uml_icons/Model.gif"),
            contentDescription = null,
            tint = Color.Unspecified
        )
    } else {
        if (tmm is TMM.ModelTree.Ecore.TPackage) {
            Icon(
                modifier = Modifier.size(Space.dp16),
                painter = painterResource("drawables/uml_icons/Package.gif"),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

