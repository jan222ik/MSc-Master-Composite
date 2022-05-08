package com.github.jan222ik.ui.uml

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.*
import java.io.File

class DiagramsLoader(
    val file: File
) {
    val mapper =  JsonMapper().registerKotlinModule()
    data class LoadError(val e: Exception)

    fun loadFromFile(): Validated<LoadError, List<DiagramHolder>> {
        if (!file.exists()) {
            return emptyList<DiagramHolder>().valid()
        }
        return try {
           mapper.readValue<List<DiagramHolder>>(file).valid()
        } catch (e: Exception) {
            LoadError(e).invalid()
        }
    }

    fun writeToFile(data: List<DiagramHolder>) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        mapper.writeValue(file, data)
    }
}