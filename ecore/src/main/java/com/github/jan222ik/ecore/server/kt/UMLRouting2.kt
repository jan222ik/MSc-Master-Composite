package com.github.jan222ik.ecore.server.kt

import com.fasterxml.jackson.databind.node.IntNode
import com.google.inject.Inject
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Context
import org.eclipse.emfcloud.modelserver.common.ModelServerPathsV1
import org.eclipse.emfcloud.modelserver.common.ModelServerPathsV2
import org.eclipse.emfcloud.modelserver.common.Routing
import org.eclipse.emfcloud.modelserver.emf.common.JsonResponse
import org.eclipse.emfcloud.modelserver.emf.common.util.ContextRequest
import org.eclipse.emfcloud.modelserver.emf.common.util.ContextResponse
import org.eclipse.emfcloud.modelserver.jsonschema.Json
import java.net.HttpURLConnection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger


class UMLRouting2 @Inject constructor(
    val javlin: Javalin
) : Routing {

    private val counter = AtomicInteger(0)

    override fun bindRoutes() {
        javlin.routes(this::endpoints)
    }

    private fun endpoints() {
        endpoints(ModelServerPathsV1.BASE_PATH)
        endpoints(ModelServerPathsV2.BASE_PATH)
    }

    private fun endpoints(basePath: String) {
        path(basePath) {
            ApiBuilder.get("counter") { ctx: Context? -> handleCounter(ctx!!) }
        }
    }

    private fun handleCounter(ctx: Context) {
        val operation = ContextRequest.getParam(ctx, "operation")
        if (operation.isPresent && listOf("add", "subtract").contains(operation.get())) {
            handleCustom(ctx, operation.get())
        } else {
            ContextResponse.error(
                ctx, HttpURLConnection.HTTP_BAD_REQUEST,
                "Missing parameter 'operation': Please specify 'add' or 'subtract'."
            )
        }
    }

    private fun handleCustom(ctx: Context, operation: String) {
        val delta = ContextRequest.getIntegerParam(ctx, "delta").orElse(1)
        val shouldIncrease = operation.contentEquals("add")
        val newValue = if (shouldIncrease) counter.addAndGet(delta) else counter.addAndGet(-delta)
        val data = Json.`object`(
            Json.prop("time", Json.text(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalDateTime.now()))),
            Json.prop("delta", IntNode.valueOf(delta)),
            Json.prop("counter", IntNode.valueOf(newValue)),
            Json.prop("increased", Json.bool(shouldIncrease))
        )
        ctx.json(JsonResponse.success(data))
    }
}