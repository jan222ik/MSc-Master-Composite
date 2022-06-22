package com.github.jan222ik.ecore

import org.eclipse.emf.ecore.EObject
import org.eclipse.emfcloud.modelserver.client.Response
import org.eclipse.emfcloud.modelserver.client.v2.ModelServerClientV2
import org.eclipse.emfcloud.modelserver.common.ModelServerPathParametersV2
import org.eclipse.uml2.uml.internal.impl.PackageImpl

class TestKotlin {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val client = ModelServerClientV2("http://localhost:8081/api/v2/", UMLPackageConfiguration())
            val ping = client.ping()
            ping.thenApply { it: Response<Boolean?> ->
                println("Response = $it")
                println("status-code:" + it.statusCode)
                it
            }
            val listResponse = client.modelUris.get()
            println("listResponse = " + listResponse.body())

            val body = client.all.get().body()
            println("allModels = $body")
            val content = body[0].content
            println("content = $content")
            val decode = client.decode(content, ModelServerPathParametersV2.FORMAT_JSON_V2)
            decode.ifPresent {
                println("Parsed = $it")
                if (it is PackageImpl) {
                    it.eAllContents()
                        .forEachRemaining { other: EObject -> println("item = $other") }
                }
            }
            client.close()
        }
    }
}

fun <T> Response<T>.println() {
    println("Response: status=${statusCode}, ${this.message}")
}