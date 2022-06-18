package com.github.jan222ik.ui.components.tooltips

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement

@ExperimentalFoundationApi
@Composable
fun TitleWithTooltip(propViewElement: IPropertyViewElement, titleAdditions: String = "") {
    val visibleState = remember { mutableStateOf(false) }
    TooltipArea(
        tooltip = {
            TooltipSurface {
                Text(propViewElement.tooltip)
            }
        },
        visibleState = visibleState
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = propViewElement.title + titleAdditions)
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Show tooltip",
                tint = LocalContentColor.current.copy(alpha = TextFieldDefaults.IconOpacity),
                modifier = Modifier.clickable { visibleState.value = !visibleState.value }.focusProperties { canFocus = false })
        }
    }
}