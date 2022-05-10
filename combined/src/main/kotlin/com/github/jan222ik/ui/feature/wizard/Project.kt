package com.github.jan222ik.ui.feature.wizard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KLogging
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import java.util.prefs.Preferences

class Project private constructor(
    val root: File,
    val projectFile: File
) {
    private val properties: MutableState<Properties> = mutableStateOf(Properties())
    val name: String
        get() = properties.value.getProperty(PROJECT_NAME_KEY)

    companion object : KLogging() {
        private const val PROJECT_FILE_NAME = ".modellingsolution"
        private const val PROJECT_NAME_KEY = "project.name"
        fun create(root: File, name: String): Project {
            logger.debug { "Create new project with name \"$name\" at $root" }
            if (!root.exists()) {
                if (!root.mkdirs()) throw Error("Project could not be created")
            }
            val projectFile = File(root, PROJECT_FILE_NAME)
            projectFile.createNewFile()

            return Project(root = root, projectFile = projectFile).apply {
                setProperty(PROJECT_NAME_KEY, name)
            }
        }

        fun load(root: File) : Project? {
            logger.debug { "Load project at $root" }
            val projectFile = File(root, PROJECT_FILE_NAME)
            return if (projectFile.exists()) {
                Project(root = root, projectFile = projectFile).apply {
                    loadProperties()
                }.also {
                    Preferences.userRoot().node("com.github.jan222ik.msc.modeller").put("lastProjects", root.absolutePath)
                }
            } else null
        }
    }

    fun propertiesAsState() : State<Properties> = properties

    operator fun set(name: String, value: String) = setProperty(name, value)

    fun setProperty(name: String, value: String) {
        val clone = properties.value.clone()
        clone as Properties
        clone.setProperty(name, value)
        properties.value = clone
        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
            pushPropsToFile(clone)
        }
    }

    private fun pushPropsToFile(clone: Properties) {
        FileOutputStream(projectFile).use {
            clone.store(it, null)
        }
    }

    private fun loadProperties() {
        FileInputStream(projectFile).use(properties.value::load)
    }

    override fun toString(): String {
        return "Project(root='$root', name='$name')"
    }


}
