package com.xkball.auto_translate.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.event.XATConfigUpdateEvent;
import com.xkball.auto_translate.utils.HttpUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class LLMClientImpl {
    
    private static volatile HttpClient CLIENT = HttpHandler.createClient();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore semaphore = new Semaphore(8);
    private final String url;
    private final String apiKey;
    private final String model;
    private final int maxRetries;
    private Boolean valid = null;
    
    private final Queue<Pair<LLMRequest,ILLMHandler>> requests = new ConcurrentLinkedQueue<>();
    
    protected LLMClientImpl(String url, String apiKey, String model, int maxRetries) {
        this.url = url;
        this.apiKey = apiKey;
        this.model = model;
        this.maxRetries = maxRetries;
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean valid(){
        if(valid == null){
            this.validification();
        }
        return valid != null && valid;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public boolean validification() {
        var flag = false;
        var result = this.sendSync(new LLMRequest("","Checking your state. Please return true. ONLY FOUR LETTERS."),
                response -> !response.getContent().isEmpty());
        if(result.left().isPresent()) flag = result.left().get();
        this.valid = flag;
        return flag;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public LLMClientImpl addRequest(LLMRequest request,ILLMHandler handler){
        this.requests.add(Pair.of(request,handler));
        return this;
    }
    
    private HttpRequest createHttpRequest(LLMRequest request){
        var json = new JsonObject();
        json.addProperty("model", model);
        for(var entry : request.params().entrySet()){
            json.addProperty(entry.getKey(), entry.getValue());
        }
        var messages = new JsonArray();
        var system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", request.systemPrompt());
        var user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", request.userPrompt());
        messages.add(system);
        messages.add(user);
        json.add("messages", messages);
        var builder = HttpRequest.newBuilder(URI.create(this.url));
        if (!this.apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + this.apiKey);
        }
        return builder.header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString().replace('\n', ' ')))
                .timeout(Duration.ofSeconds(20))
                .build();
    }
    
    public void send(){
        CompletableFuture.runAsync(() -> {
            while(!this.requests.isEmpty()){
                var request = this.requests.poll();
                this.sendAsyncWithRetry(request.getFirst(),request.getSecond(), 0);
            }
        },EXECUTOR);
    }
    
    private void sendAsyncWithRetry(LLMRequest req,ILLMHandler handler,int retries){
        if(retries > this.maxRetries){
            LOGGER.warn("LLM Request Exceeded Max Retries: {}", retries);
            handler.onRetriesExceeded();
        }
        else {
            if(retries > 0){
                LockSupport.parkNanos(2000000);
            }
            CompletableFuture.runAsync(semaphore::acquireUninterruptibly,EXECUTOR)
                    .thenCompose(ignore -> sendAsync(req,handler::handle))
                    .handle((b,ex) -> {
                        if(ex != null){
                            LOGGER.error("LLM Request Error", ex);
                            b = false;
                        }
                        semaphore.release();
                        return b;
                    }).thenAccept(res -> {
                        if(!res) sendAsyncWithRetry(req,handler,retries+1);
                    });
        }
    }
    
    public <T> CompletableFuture<T> sendAsync(LLMRequest req,Function<LLMResponse, T> overrideHandler){
        return HttpUtils.sendWithRetry(CLIENT,createHttpRequest(req), XATConfig.MAX_RETRIES)
                .thenApplyAsync((response) -> overrideHandler.apply(LLMResponse.fromString(response.body())),EXECUTOR);
    }
    
    public <T> Either<T,Throwable> sendSync(LLMRequest req, Function<LLMResponse, T> overrideHandler){
        try {
            var t = HttpUtils.sendWithRetry(CLIENT,createHttpRequest(req), XATConfig.MAX_RETRIES)
                    .thenApplyAsync((response) -> overrideHandler.apply(LLMResponse.fromString(response.body())),EXECUTOR)
                    .get();
            return Either.left(t);
        } catch (ExecutionException | InterruptedException e) {
            return Either.right(e);
        }
    }
    
    @EventBusSubscriber
    public static class HttpHandler {
        
        public static HttpClient createClient() {
            var builder = HttpClient.newBuilder();
            if (!XATConfig.HTTP_PROXY_HOST.isEmpty()) {
                builder.proxy(ProxySelector.of(new InetSocketAddress(XATConfig.HTTP_PROXY_HOST, XATConfig.HTTP_PROXY_PORT)));
            }
            builder.connectTimeout(Duration.ofSeconds(30));
            return builder.build();
        }
        
        @SubscribeEvent
        public static void onUpdateHttpConfig(XATConfigUpdateEvent.Http event){
            if(event.changed()){
                LLMClientImpl.CLIENT = createClient();
            }
        }
    }
    
    public static class Builder {
        
        private String url;
        private String apiKey;
        private String model;
        private int maxRetries = 1;
        
        public Builder() {}
        
        public Builder setUrl(String url){
            this.url = url;
            return this;
        }
        
        public Builder setApiKey(String key){
            this.apiKey = key;
            return this;
        }
        
        public Builder setMaxRetries(int maxRetries){
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder setModel(String model){
            this.model = model;
            return this;
        }
        
        public LLMClientImpl build(){
            return new LLMClientImpl(url, apiKey, model, maxRetries);
        }
    }
}
