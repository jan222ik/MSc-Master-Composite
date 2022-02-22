package com.github.jan222ik.test

import com.github.jan222ik.data.di.module.MyModule
import it.cosenonjaviste.daggermock.DaggerMockRule

class MyDaggerMockRule : DaggerMockRule<TestComponent>(
    TestComponent::class.java,
    MyModule()
    // TODO : Add your modules here
) {
    init {
        customizeBuilder<com.github.jan222ik.test.DaggerTestComponent.Builder> {
            it
        }
    }
}