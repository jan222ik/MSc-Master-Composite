package com.github.jan222ik.ui.components.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ensureNeverFrozen
import com.github.jan222ik.ui.feature.main.MainScreenScaffoldConstants
import com.github.jan222ik.ui.value.EditorColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    onClick: () -> Unit,
    isCloseBtn: Boolean = false,
    content: @Composable RowScope.(isHover: Boolean) -> Unit
) {
    val isHover: MutableState<Boolean> = remember { mutableStateOf(false) }
    ActionIconButton(
        modifier = Modifier
            .height(MainScreenScaffoldConstants.menuToolBarHeight)
            .background(when (isCloseBtn) {
                true -> EditorColors.backgroundGray.takeUnless { isHover.value } ?: EditorColors.closeBtn
                else -> EditorColors.backgroundGray.takeUnless { isHover.value } ?: EditorColors.dividerGray
            })
            .onPointerEvent(PointerEventType.Enter) { isHover.value = true }
            .onPointerEvent(PointerEventType.Exit) { isHover.value = false },
        onClick = onClick
    ) {
        Row {
            content.invoke(this, isHover.value)
        }
    }
}

@Composable
private fun ActionIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .width(48.dp)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}