package com.github.jan222ik.ecore

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emfcloud.modelserver.client.Response
import org.eclipse.emfcloud.modelserver.client.v2.ModelServerClientV2
import org.eclipse.emfcloud.modelserver.common.ModelServerPathParametersV2

class TestKotlin {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val modelServerClientV2 = ModelServerClientV2("http://localhost:8081/api/v2/")
            val ping = modelServerClientV2.ping()
            ping.thenApply { it: Response<Boolean?> ->
                println("Response = $it")
                println("status-code:" + it.statusCode)
                it
            }
            val listResponse = modelServerClientV2.modelUris.get()
            println("listResponse = " + listResponse.body())

            val body = modelServerClientV2.all.get().body()
            println("allModels = $body")
            val content = body[0].content
            println("content = $content")
            val decode = modelServerClientV2.decode(content, ModelServerPathParametersV2.FORMAT_JSON_V2)
            decode.ifPresent { it: EObject ->
                println("Parsed = $it")
                if (it is EPackage) {
                    val p = it as EPackage
                    p.eAllContents()
                        .forEachRemaining { other: EObject -> println("item = $other") }
                }
            }
            modelServerClientV2.close()
        }
    }
}