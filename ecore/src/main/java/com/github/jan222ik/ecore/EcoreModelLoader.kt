package com.github.jan222ik.ecore

import org.eclipse.emfcloud.modelserver.client.v2.ModelServerClientV2

object EcoreModelLoader {
    private val clientV2 = ModelServerClientV2("http://localhost:8081/api/v2/", UMLPackageConfiguration())

    fun open(name: String?): EcoreClientPerModel {
        val existingModels = clientV2.modelUris.get().body().also { println(it) }
        val find = existingModels.find { it == name }
        if (find != null) {
            return EcoreClientPerModel(
                generalClient = clientV2,
                modelId = find
            )
        }
        throw IllegalArgumentException("Model does not exist for file")
    }
}