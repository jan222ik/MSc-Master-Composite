package com.github.jan222ik.ui.feature.main.diagram

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import com.github.jan222ik.ui.feature.LocalI18N
import com.github.jan222ik.ui.feature.LocalShortcutActionHandler
import com.github.jan222ik.ui.feature.main.keyevent.ShortcutAction
import com.github.jan222ik.ui.feature.stringResource
import com.github.jan222ik.ui.value.R
import de.comahe.i18n4k.Locale
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

@ExperimentalSplitPaneApi
@ExperimentalComposeUiApi
class DiagramCanvasComponent(
    val parent: DiagramAreaComponent
) {

    @Composable
    fun render() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val shortcutActionsHandler = LocalShortcutActionHandler.current
            var textValue by remember { mutableStateOf("") }
            shortcutActionsHandler.register(
                action = ShortcutAction.of(
                    key = Key.K,
                    modifierSum = ShortcutAction.KeyModifier.CTRL,
                    action = {
                        DiagramAreaComponent.logger.debug { "CTRL + K" }
                        textValue = "CTRL + K"
                        true
                    }
                )
            )
            TextField(
                value = textValue,
                onValueChange = { textValue = it }
            )
            val (localeState, switchLocale) = LocalI18N.current
            Button(onClick = {
                val lang = "de".takeIf { localeState.language == "en" } ?: "en"
                switchLocale(Locale(lang))
            }) {
                Text("Switch")
            }
            Text(text = stringResource(key = textValue) { "${R.string.mainWindow.title}: ${R.string.mainWindow.language} $textValue" })
        }
    }
}