package com.github.jan222ik.ui.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.decompose.ComponentContext
import com.github.jan222ik.di.AppComponent
import com.github.jan222ik.ui.feature.LocalJobHandler
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.navigation.Component
import javax.inject.Inject

class MainScreenComponent(
    appComponent: AppComponent,
    private val componentContext: ComponentContext,
) : Component, ComponentContext by componentContext {
    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var jobHandler: JobHandler

    init {
        appComponent.inject(this)
    }

    @Composable
    override fun render() {
        val scope = rememberCoroutineScope()
        LaunchedEffect(viewModel) {
            viewModel.init(scope)
        }

        CompositionLocalProvider(
            LocalJobHandler provides jobHandler
        ) {
            MainScreen(viewModel, jobHandler)
        }
    }
}