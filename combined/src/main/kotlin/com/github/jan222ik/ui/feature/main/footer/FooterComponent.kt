package com.github.jan222ik.ui.feature.main.footer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.jan222ik.ui.feature.main.footer.progress.BackgroundJobComponent
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class)
class FooterComponent @Inject constructor(
    private val jobHandler: JobHandler
) {

    @Composable
    fun render(modifier: Modifier) {
        Box(
            modifier = modifier
                .background(Color.Cyan),
            contentAlignment = Alignment.Center
        ) {
            Text("Footer")
            BackgroundJobIntegration()
        }
    }

    @Composable
    fun BoxScope.BackgroundJobIntegration() {
        val component = remember(jobHandler) { BackgroundJobComponent(jobHandler) }
        component.render(this)
    }
}
