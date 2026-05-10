package com.xkball.auto_translate.utils.translate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import com.xkball.auto_translate.event.XATConfigUpdateEvent;
import com.xkball.auto_translate.llm.LLMClientImpl;
import com.xkball.auto_translate.utils.HttpUtils;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class DefaultTranslate implements ITranslator {
    
    public static final DefaultTranslate INSTANCE = new DefaultTranslate();
    private static final URI TRANSLATE_URI = URI.create("https://xkball.com/api/tr/translate");
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile HttpClient CLIENT = createClient();
    
    private DefaultTranslate() {
    }
    
    @Override
    public CompletableFuture<String> translate(String text, String lang) {
        LOGGER.debug(text);
        if (text.isEmpty()) {
            return CompletableFuture.completedFuture("");
        }
        var requestBody = new JsonObject();
        requestBody.addProperty("text", text);
        requestBody.addProperty("targetLanguage", lang);
        var request = HttpRequest.newBuilder(TRANSLATE_URI)
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
//                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        return HttpUtils.sendWithRetry(CLIENT, request, XATConfig.MAX_RETRIES)
                .thenApplyAsync(response -> getTranslateResult(response.body()))
                .exceptionallyAsync(t -> {
                    LOGGER.warn("Network error", t);
                    return I18n.get(ERROR_RESULT_KEY);
                });
    }
    
    public static HttpClient createClient() {
        return LLMClientImpl.HttpHandler.createClient();
    }
    
    private static String getTranslateResult(String response) {
        try {
            var json = GSON.fromJson(response, JsonObject.class);
            if (json != null && json.has("result") && !json.get("result").isJsonNull()) {
                return json.get("result").getAsString();
            }
        } catch (Exception e) {
            LOGGER.error("Fail to parse default translate result: {}", response, e);
        }
        return I18n.get(ERROR_RESULT_KEY);
    }
    
    @SubscribeEvent
    public static void onUpdateHttpConfig(XATConfigUpdateEvent.Http event) {
        if (event.changed()) {
            DefaultTranslate.CLIENT = createClient();
        }
    }
}
