package com.github.jan222ik.playground.contextmenu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun main() {
    singleWindowApplication {
        val items = remember { List(100) { DemoDataItem(name = "demoItmeNR$it") } }
        var contextFor by remember { mutableStateOf<DemoDataItem?>(null) }
        val state = rememberLazyListState()
        val menuItems = listOf(newFileMenuEntry2, newFileContextMenuEntry, newPackageContextMenuEntry)
        contextFor?.let { item ->
            MenuPopup(
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val indexOf = items.indexOf(item)
                        val find = state.layoutInfo.visibleItemsInfo.find { it.index == indexOf }
                        return IntOffset(
                            x = anchorBounds.width,
                            y = find?.let { it.offset.plus(it.size.div(2).minus(popupContentSize.height.div(2))) } ?: 0
                        )

                    }
                },
                onDismissRequest = {
                    println("Dismiss")
                    contextFor = null
                },
                item = contextFor!!,
                menuItems = menuItems
            )
        }


        LazyColumn(
            modifier = Modifier.wrapContentWidth().fillMaxHeight(),
            state = state
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(5.dp)
                        .mouseCombinedClickable { evt ->
                            if (buttons.isSecondaryPressed) {
                                contextFor = item
                            }
                        }
                        .border(width = 1.dp, color = Color.Cyan)
                ) {
                    Text("Name: ${item.name}")
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun MenuPopup(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    item: DemoDataItem,
    menuItems: List<MenuContribution>
) {
    val hasDirectHover = remember { mutableStateOf(true) }
    val hasIndirectHover = remember { mutableStateOf(false) }
    val delayedDismiss = remember { mutableStateOf(false) }

    remember(hasDirectHover.value, hasIndirectHover.value) {
        /*
        print("hasDirectHover = ${hasDirectHover.value} ")
        print("hasIndirectHover = ${hasIndirectHover.value} ")
        println("!a||b = ${!(hasDirectHover.value || hasIndirectHover.value)}")
         */
        delayedDismiss.value = !(hasDirectHover.value || hasIndirectHover.value)
    }

    LaunchedEffect(delayedDismiss.value) {
        if (delayedDismiss.value) {
            withContext(Dispatchers.IO) {
                delay(500)
                if (delayedDismiss.value) {
                    onDismissRequest.invoke()
                }
            }
        }
    }

    MenuPopupNested(
        popupPositionProvider, onDismissRequest, item, menuItems, hasDirectHover, hasIndirectHover
    )
}

@ExperimentalComposeUiApi
@Composable
fun MenuPopupNested(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: () -> Unit,
    item: DemoDataItem,
    menuItems: List<MenuContribution>,
    hasDirectHover: MutableState<Boolean>,
    hasIndirectHover: MutableState<Boolean>
) {
    var showNestedPopupFor by remember { mutableStateOf<MenuContribution.Contentful.NestedMenuItem?>(null) }
    var onceEnterOrTimeout by remember { mutableStateOf(false) }

    val childPopupHover = remember { mutableStateOf(false) }

    val childHoverDirect = remember { mutableStateOf(false) }
    val childHoverIndirect = remember { mutableStateOf(false) }

    remember(onceEnterOrTimeout, childHoverDirect.value, childHoverIndirect.value, childPopupHover.value) {
        /*
        print("onceEnter = $onceEnterOrTimeout ")
        print("childPopupHover = ${childPopupHover.value} ")
        print("childHoverDirect = ${childHoverDirect.value} ")
        print("childHoverIndirect = ${childHoverIndirect.value} ")
        println(
            "hasIndirectHover = !a||b||c||d = ${
                !onceEnterOrTimeout || childPopupHover.value || childHoverDirect.value || childHoverIndirect.value
            }"
        )
         */
        hasIndirectHover.value =
            !onceEnterOrTimeout || childPopupHover.value || childHoverDirect.value || childHoverIndirect.value
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        focusable = true,
        onDismissRequest = onDismissRequest
    ) {
        val scope = rememberCoroutineScope()
        Surface(modifier = Modifier
            .width(200.dp)
            .border(width = 1.dp, color = Color.Magenta)
            .onPointerEvent(PointerEventType.Enter) {
                hasDirectHover.value = true
                onceEnterOrTimeout = true
            }.onPointerEvent(PointerEventType.Exit) {
                hasDirectHover.value = false
            }
            .border(width = 1.dp, color = MaterialTheme.colors.onSurface)
        ) {
            Column(Modifier.width(200.dp)) {
                Text("Context for ${item.name}")
                menuItems.forEach { menuEntry ->
                    when (menuEntry) {
                        MenuContribution.Separator -> TODO()
                        is MenuContribution.Contentful -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(when (menuEntry) {
                                        is MenuContribution.Contentful.MenuItem -> {
                                            Modifier.clickable(enabled = menuEntry.isActive()) {
                                                scope.launch(Dispatchers.IO) {
                                                    menuEntry.command?.execute()
                                                }
                                            }
                                        }
                                        is MenuContribution.Contentful.NestedMenuItem -> {
                                            Modifier
                                                .onPointerEvent(PointerEventType.Enter) {
                                                    showNestedPopupFor = menuEntry
                                                    childPopupHover.value = true
                                                }
                                                .onPointerEvent(PointerEventType.Exit) {
                                                    childPopupHover.value = false
                                                }
                                                .clickable {
                                                    childPopupHover.value = !childPopupHover.value
                                                }
                                        }
                                    })
                            ) {
                                if (showNestedPopupFor == menuEntry && (childPopupHover.value || childHoverDirect.value || childHoverIndirect.value)) {
                                    MenuPopupNested(
                                        popupPositionProvider = object : PopupPositionProvider {
                                            override fun calculatePosition(
                                                anchorBounds: IntRect,
                                                windowSize: IntSize,
                                                layoutDirection: LayoutDirection,
                                                popupContentSize: IntSize
                                            ): IntOffset {
                                                return anchorBounds.topRight
                                            }
                                        },
                                        onDismissRequest = onDismissRequest,
                                        item = item,
                                        menuItems = showNestedPopupFor!!.nestedItems,
                                        hasDirectHover = childHoverDirect,
                                        hasIndirectHover = childHoverIndirect
                                    )
                                }
                                Box(modifier = Modifier.size(24.dp)) {
                                    menuEntry.icon?.let { Icon(it, null) }
                                }
                                Text(text = menuEntry.displayName)
                                Spacer(Modifier.weight(1f))
                                Box(modifier = Modifier.size(24.dp)) {
                                    if (menuEntry is MenuContribution.Contentful.NestedMenuItem && menuEntry.nestedItems.isNotEmpty()) {
                                        Icon(Icons.Filled.ChevronRight, null)
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

data class DemoDataItem(val name: String)