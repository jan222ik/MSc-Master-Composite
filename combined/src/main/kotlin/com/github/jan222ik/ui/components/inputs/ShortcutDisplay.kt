package com.github.jan222ik.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.unit.dp
import java.awt.event.KeyEvent

@ExperimentalComposeUiApi
@Composable
fun ShortcutDisplay(keys: List<Key>, enabled: Boolean = true) {
    val textColor = Color.Black.copy(alpha = 1f.takeIf { enabled } ?: ContentAlpha.disabled)
    @Composable
    fun KeyDisplay(key: Key) {
        Box(
            modifier = Modifier
                .background(color = Color.LightGray.copy(alpha = 1f.takeIf { enabled } ?: ContentAlpha.disabled))
                .border(
                    width = 1.dp,
                    color = Color.DarkGray
                )
                .clip(shape = RoundedCornerShape(size = 16.dp))
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 3.dp).padding(bottom = 3.dp, top = 1.dp),
                color = Color.White,
                elevation = 5.dp
            ) {
                Text(
                    text = key.nativeKeyCode.takeIf { key != Key.Escape }
                        ?.let { KeyEvent.getKeyText(it) } ?: "Esc",
                    style = MaterialTheme.typography.caption,
                    color = textColor
                )
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEachIndexed { index, key ->
            if (index != 0) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.caption,
                    color = textColor
                )
            }
            KeyDisplay(key)
        }
    }
}