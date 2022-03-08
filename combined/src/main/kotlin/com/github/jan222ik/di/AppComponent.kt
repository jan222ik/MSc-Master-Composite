package com.github.jan222ik.di

import com.github.jan222ik.di.module.JobHandlerModule
import com.github.jan222ik.ui.feature.main.MainScreenComponent
import com.github.jan222ik.ui.feature.splash.SplashScreenComponent
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        JobHandlerModule::class
    ]
)
interface AppComponent {
    fun inject(splashScreenComponent: SplashScreenComponent)
    fun inject(mainScreenComponent: MainScreenComponent)
}