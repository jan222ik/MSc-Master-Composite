package com.github.jan222ik.ecore

import org.eclipse.emf.ecore.EObject
import org.eclipse.emfcloud.modelserver.client.v2.ModelServerClientV2
import org.eclipse.emfcloud.modelserver.common.ModelServerPathParametersV2
import org.eclipse.uml2.uml.Model
import java.util.*

class EcoreClientPerModel(
    val generalClient: ModelServerClientV2,
    val modelId: String,
) {
    val modelIdx: Int
        get() = generalClient.modelUris.get().body().indexOfFirst { it == modelId }

    val model: Model
        get() = decode(generalClient.all.get().body()[modelIdx].content)!!.get() as Model


    fun decode(payload: String): Optional<EObject>? {
        return generalClient.decode(payload, ModelServerPathParametersV2.FORMAT_JSON_V2)
    }
}