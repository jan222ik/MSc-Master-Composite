package com.github.jan222ik.di.module

import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
class JobHandlerModule(
    val coroutineScope: CoroutineScope
) {
    @Provides
    @Singleton
    fun provideJobHandler(): JobHandler {
        return JobHandler(coroutineScope)
    }
}