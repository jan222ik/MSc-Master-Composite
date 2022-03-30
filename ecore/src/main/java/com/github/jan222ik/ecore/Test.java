package com.github.jan222ik.ecore;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emfcloud.modelserver.client.Model;
import org.eclipse.emfcloud.modelserver.client.Response;
import org.eclipse.emfcloud.modelserver.client.v2.ModelServerClientV2;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.eclipse.emfcloud.modelserver.common.ModelServerPathParametersV2.FORMAT_JSON_V2;

public class Test {
    public static void main(String[] args) throws MalformedURLException, ExecutionException, InterruptedException {
        ModelServerClientV2 modelServerClientV2 = new ModelServerClientV2("http://localhost:8081/api/v2/");
        CompletableFuture<Response<Boolean>> ping = modelServerClientV2.ping();
        ping.thenApply(it -> {
            System.out.println("Response = " + it);
            System.out.println("status-code:" + it.getStatusCode());
            return it;
        });
        Response<List<String>> listResponse = modelServerClientV2.getModelUris().get();
        System.out.println("listResponse = " + listResponse.body());

        List<Model<String>> body = modelServerClientV2.getAll().get().body();
        System.out.println("allModels = " + body);
        String content = body.get(0).getContent();
        System.out.println("content = " + content);
        Optional<EObject> decode = modelServerClientV2.decode(content, FORMAT_JSON_V2);
        decode.ifPresent(it -> {
            System.out.println("Parsed = " + it);
            if (it instanceof EPackage) {
                var p = (EPackage) it;
                p.eAllContents().forEachRemaining(other -> System.out.println("item = " + other));
            }
        });
        modelServerClientV2.close();
    }
}
