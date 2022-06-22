package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import com.github.jan222ik.ui.feature.LocalCommandStackHandler
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuItemList(items: List<MenuContribution>, jobHandler: JobHandler, width: Dp) {
    val commandStackHandler = LocalCommandStackHandler.current
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
                    val colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface)
                    val bgColor = colors.backgroundColor(enabled)
                    val contentColor = colors.contentColor(enabled)
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
                                            scope.launch {
                                                if (it.pushToStack()) {
                                                    commandStackHandler.add(it)
                                                } else {
                                                    it.execute(jobHandler)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            .onGloballyPositioned { loc = it }
                            .background(color = bgColor.value)
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
                                when (it) {
                                    is DrawableIcon.viaImgVector -> Icon(
                                        imageVector = it.vector,
                                        contentDescription = "Execute: ${item.displayName}",
                                        tint = contentColor.value
                                    )
                                    is DrawableIcon.viaPainterConstruction -> Icon(
                                        painter = it.painter.invoke(),
                                        contentDescription = "Execute: ${item.displayName}",
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                        Box(Modifier.fillMaxWidth()) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = item.displayName,
                                color = contentColor.value
                            )
                            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                                if (item is MenuContribution.Contentful.NestedMenuItem) {
                                    Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = contentColor.value)
                                } else {
                                    ShortcutDisplay(
                                        keys = item.keyShortcut,
                                        enabled = enabled
                                    )
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