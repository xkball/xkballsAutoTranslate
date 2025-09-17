package com.xkball.auto_translate.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import com.xkball.auto_translate.event.XATConfigUpdateEvent;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class GoogleTranslate implements ITranslator {
    
    private static final URI THE_URI = URI.create("https://translate.google.com");
    private static final URI INTERNAL_URI = URI.create("https://translate.google.com/_/TranslateWebserverUi/data/batchexecute");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CookieManager cookieManager = new CookieManager();
    private static final AtomicInteger cookieUsed = new AtomicInteger(0);
    private static volatile HttpClient CLIENT = createClient();
    
    public static final GoogleTranslate INSTANCE = new GoogleTranslate();
    
    private GoogleTranslate(){}
    
    public CompletableFuture<String> translate(String text, String lang) {
        LOGGER.debug(text);
        var strArray = text.split("\n");
        var strList = Arrays.stream(strArray).filter(s -> !s.isEmpty()).toList();
        if (strList.isEmpty()) return CompletableFuture.completedFuture("");
        var task = CompletableFuture.runAsync(() -> updateCookies(false))
                .thenApplyAsync((v) -> tryRunTranslate(strList.getFirst(),lang,0));
        for(var i = 1; i < strList.size(); i++) {
            int finalI = i;
            task = task.thenApplyAsync(str -> str + "\n" + tryRunTranslate(strList.get(finalI),lang,0));
        }
        task.exceptionallyAsync(t -> {
            LOGGER.error("Network error",t);
            return I18n.get(ERROR_RESULT_KEY);
        });
        return task;
    }
    
    private static synchronized void updateCookies(boolean force) {
        if (!force && !cookieManager.getCookieStore().getCookies().isEmpty()) return;
        cookieManager.getCookieStore().removeAll();
        var reqGetCookies = HttpRequest.newBuilder(THE_URI)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            var res = CLIENT.send(reqGetCookies, HttpResponse.BodyHandlers.ofString());
            //CLIENT.send(reqGetCookies, HttpResponse.BodyHandlers.ofString());
            res.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        cookieUsed.set(0);
    }
    
    private static String tryRunTranslate(String text, String lang, int retries) {
        if(retries > XATConfig.MAX_RETRIES) {
            throw new RuntimeException("Network error: exceeded maximum retry times. " + text);
        }
        if(cookieUsed.incrementAndGet() > 30){
            updateCookies(true);
        }
        var postContent = "[[[\"MkEWBc\",\"[[\\\""+text+"\\\",\\\"auto\\\",\\\""+lang+"\\\",true],[null]]\",null,\"generic\"]]]";
        var reqPost = HttpRequest.newBuilder(INTERNAL_URI)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("f.req=" + URLEncoder.encode(postContent, StandardCharsets.UTF_8)))
                .build();
        var resPost = CLIENT.sendAsync(reqPost,HttpResponse.BodyHandlers.ofString());
        return resPost.thenApplyAsync(res -> {
            if(res.statusCode() != 200){
                LockSupport.parkNanos(200000);
                return tryRunTranslate(text,lang,retries+1);
            }
            return getTranslateResult(res.body());
        }).join();
    }
    
    public static HttpClient createClient() {
        var builder = HttpClient.newBuilder();
        if(!XATConfig.HTTP_PROXY_HOST.isEmpty()){
            builder.proxy(ProxySelector.of(new InetSocketAddress(XATConfig.HTTP_PROXY_HOST,XATConfig.HTTP_PROXY_PORT)));
        }
        return builder.cookieHandler(cookieManager).build();
    }
    
    public static String getTranslateResult(String str){
        try {
            str = str.substring(4);
            var gson = new Gson();
            var array1 = gson.fromJson(str, JsonArray.class);
            var array2 = array1.get(0).getAsJsonArray();
            var innerStr = array2.get(2).getAsString();
            var array3 = gson.fromJson(innerStr, JsonArray.class);
            var array4 = array3.get(1).getAsJsonArray();
            var array5 = array4.get(0).getAsJsonArray();
            var array6 = array5.get(0).getAsJsonArray();
            var array7 = array6.get(5).getAsJsonArray();
            var array8 = array7.get(0).getAsJsonArray();
            return array8.get(0).getAsString();
        } catch (Exception e){
            LOGGER.error("Fail to parse translate result: {}",str,e);
            return I18n.get(ERROR_RESULT_KEY);
        }
    }
    
    @SubscribeEvent
    public static void onUpdateHttpConfig(XATConfigUpdateEvent.Http event){
        if(event.changed()){
            GoogleTranslate.CLIENT = createClient();
        }
    }
}
