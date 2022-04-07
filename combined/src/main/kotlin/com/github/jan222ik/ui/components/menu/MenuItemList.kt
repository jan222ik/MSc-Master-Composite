package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.github.jan222ik.ui.components.inputs.ShortcutDisplay
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuItemList(items: List<MenuContribution>, jobHandler: JobHandler, width: Dp) {
    val spacerMod = Modifier.height(4.dp)
    val iconSpace = 16.dp
    var showSubFor by remember { mutableStateOf(emptyList<MenuContribution>()) }
    var subLocation by remember { mutableStateOf<LayoutCoordinates?>(null) }
    Column(
        modifier = Modifier
            .width(width)
            .padding(8.dp),
    ) {
        items.forEach { item ->
            when (item) {
                MenuContribution.Separator -> {
                    Divider(Modifier.fillMaxWidth(), startIndent = iconSpace)
                    Spacer(modifier = spacerMod)
                }
                is MenuContribution.Contentful -> {
                    val enabled = item.isActive()
                    val scope = rememberCoroutineScope()
                    var loc by remember { mutableStateOf<LayoutCoordinates?>(null) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    when (item) {
                                        is MenuContribution.Contentful.NestedMenuItem -> {
                                            showSubFor = item.nestedItems
                                            subLocation = loc
                                        }
                                        else -> item.command?.let {
                                            scope.launch { it.execute(jobHandler) }
                                        }
                                    }
                                }
                            )
                            .onGloballyPositioned { loc = it }
                            .then(
                                Modifier.takeUnless { loc == subLocation } ?: Modifier.background(Color.LightGray)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(iconSpace)
                        ) {
                            item.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = "Execute: ${item.displayName}"
                                )
                            }
                        }
                        Box(Modifier.fillMaxWidth()) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = item.displayName
                            )
                            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                                if (item is MenuContribution.Contentful.NestedMenuItem) {
                                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
                                } else {
                                    ShortcutDisplay(item.keyShortcut)
                                }
                            }
                        }
                    }
                    Spacer(spacerMod)
                }
            }

        }
        if (showSubFor.isNotEmpty()) {
            Popup(
                focusable = true,
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        return IntOffset(
                            x = anchorBounds.topRight.x + 16.dp.value.toInt(),
                            y = subLocation?.positionInWindow()?.y?.toInt()?.minus(16.dp.value.toInt()) ?: anchorBounds.topRight.y
                        )
                        /*
                        return IntOffset(
                            x = anchorBounds.topRight.x + 16.dp.value.toInt(),
                            y = anchorBounds.topRight.y + 16.dp.value.toInt()
                        )

                         */
                    }

                },
                onDismissRequest = {
                    showSubFor = emptyList()
                },
            ) {
                Card {
                    Column(
                        modifier = Modifier.padding(8.dp),
                    ) {
                        MenuItemList(items = showSubFor, jobHandler = jobHandler, width)
                    }
                }
            }
        }
    }
}