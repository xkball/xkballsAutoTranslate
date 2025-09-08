package com.xkball.auto_translate.utils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    
    public static CompletableFuture<HttpResponse<String>> sendWithRetry(
            HttpClient client, HttpRequest request, int maxRetries) {
        
        return sendRecursive(client,request, maxRetries, 100, 30000);
    }
    
    private static CompletableFuture<HttpResponse<String>> sendRecursive(HttpClient client,
            HttpRequest request, int remainingRetries, long delayMs, long maxDelayMs) {
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        return CompletableFuture.completedFuture(response);
                    } else if (remainingRetries > 0) {
                        return delayedRetry(client,request, remainingRetries - 1,
                                Math.min(delayMs * 2, maxDelayMs), delayMs, maxDelayMs);
                    } else {
                        throw new RuntimeException("Network error: exceeded maximum retry times. ");
                    }
                })
                .exceptionally(ex -> {
                    if (remainingRetries > 0) {
                        return delayedRetry(client,request, remainingRetries - 1,
                                Math.min(delayMs * 2, maxDelayMs), delayMs, maxDelayMs).join();
                    }
                    throw new RuntimeException("Network error: exceeded maximum retry times. ");
                });
    }
    
    private static CompletableFuture<HttpResponse<String>> delayedRetry(HttpClient client,
            HttpRequest request, int remainingRetries, long nextDelayMs, long currentDelayMs, long maxDelayMs) {
        
        Executor delayedExecutor = CompletableFuture.delayedExecutor(currentDelayMs, TimeUnit.MILLISECONDS);
        return CompletableFuture
                .supplyAsync(() -> null, delayedExecutor)
                .thenCompose(v -> sendRecursive(client,request, remainingRetries, nextDelayMs, maxDelayMs));
    }
}
