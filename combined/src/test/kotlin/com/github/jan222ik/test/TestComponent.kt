package com.github.jan222ik.test

import com.github.jan222ik.data.di.module.MyModule
import com.github.jan222ik.data.repo.MyRepo
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MyModule::class
        // Add your modules here
    ]
)
interface TestComponent {
    fun myRepo(): MyRepo
}