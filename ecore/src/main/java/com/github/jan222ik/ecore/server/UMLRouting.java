package com.github.jan222ik.ecore.server;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.eclipse.emfcloud.modelserver.common.ModelServerPathsV1;
import org.eclipse.emfcloud.modelserver.common.ModelServerPathsV2;
import org.eclipse.emfcloud.modelserver.common.Routing;
import org.eclipse.emfcloud.modelserver.emf.common.JsonResponse;
import org.eclipse.emfcloud.modelserver.emf.common.util.ContextResponse;
import org.eclipse.emfcloud.modelserver.jsonschema.Json;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static org.eclipse.emfcloud.modelserver.emf.common.util.ContextRequest.getIntegerParam;
import static org.eclipse.emfcloud.modelserver.emf.common.util.ContextRequest.getParam;

public class UMLRouting implements Routing {

    private final Javalin javalin;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Inject
    public UMLRouting(Javalin javalin) {
        this.javalin = javalin;
    }


    private void endpoints() {
        endpoints(ModelServerPathsV1.BASE_PATH);
        endpoints(ModelServerPathsV2.BASE_PATH);
    }

    private void endpoints(String basePath) {
        path(basePath, this::umlEnd);
    }

    private void umlEnd() {
        get("counter", this::handleCounter);
    }

    protected void handleCounter(final Context ctx) {
        Optional<String> operation = getParam(ctx, "operation");
        if (operation.isPresent() && List.of("add", "subtract").contains(operation.get())) {
            handleCustom(ctx, operation.get());
        } else {
            ContextResponse.error(ctx, HttpURLConnection.HTTP_BAD_REQUEST,
                    "Missing parameter 'operation': Please specify 'add' or 'subtract'.");
        }
    }

    protected void handleCustom(final Context ctx, final String operation) {
        Integer delta = getIntegerParam(ctx, "delta").orElse(1);
        boolean shouldIncrease = operation.contentEquals("add");
        int newValue = shouldIncrease
                ? counter.addAndGet(delta)
                : counter.addAndGet(-delta);
        ObjectNode data = Json.object(
                Json.prop("time", Json.text(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalDateTime.now()))),
                Json.prop("delta", IntNode.valueOf(delta)),
                Json.prop("counter", IntNode.valueOf(newValue)),
                Json.prop("increased", Json.bool(shouldIncrease)));
        ctx.json(JsonResponse.success(data));
    }

    @Override
    public void bindRoutes() {
        javalin.routes(this::endpoints);
    }
}

