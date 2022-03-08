package com.github.jan222ik


import com.github.jan222ik.model.AppArgs
import com.github.jan222ik.ui.feature.MainActivity
import com.theapache64.cyclone.core.Application
import mu.KLogging


class App(
    appArgs: AppArgs,
) : Application() {

    companion object : KLogging() {
        lateinit var appArgs: AppArgs
    }

    init {
        App.appArgs = appArgs
    }

    override fun onCreate() {
        super.onCreate()

        logger.debug { "Starting app..." }

        val splashIntent = MainActivity.getStartIntent()
        startActivity(splashIntent)
    }
}

/**
 * The magic begins here
 */
fun main() {

    val appArgs = AppArgs(
        appName = "ASID", // To show on title bar
        version = "v0.0.0", // To show on title inside brackets
        versionCode = 100 // To compare with latest version code (in case if you want to prompt update)
    )

    App(appArgs).onCreate()
}