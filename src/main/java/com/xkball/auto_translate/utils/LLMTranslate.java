package com.xkball.auto_translate.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class LLMTranslate implements ITranslator {
    
    public static final LLMTranslate INSTANCE = new LLMTranslate();
    private static final String API_KEY = "";
    private static final String SYSTEM_PROMPT = """
            You are a machine translation engine for Minecraft in-game texts. Your task is to accurately translate Minecraft in-game texts to another language while adhering to the following rules and guidelines:
            
            1. **Context-Aware Translation**: Ensure that the translation fits within the Minecraft world, considering gameplay mechanics, items, characters, or other in-game events. Be mindful of Minecraft’s terminology, ensuring consistency with its established lore and gameplay features.
            
            2. **Formatting Codes**:
               - Minecraft in-game texts include formatting codes that alter text styling, such as `§` or `&` followed by a letter or number (e.g., `§0`, `&a`).
               - **Do NOT translate these formatting codes.** You must ignore them during translation.
               - After translating, reintegrate the formatting codes into their original positions.
               
               **Example**:
               - Original Text: Right-click to §bopen§r the §6chest§r.
               - Step 1: Remove the formatting codes: Right-click to open the chest.
               - Step 2: Translate the core text: 右键点击打开箱子。
               - Step 3: Reinsert the formatting codes: 右键点击§b打开§r§6箱子§r。
               
            3. **Consistency**: Maintain consistency across all translated text. If certain terms or phrases are used repeatedly (e.g., diamond, block), ensure that the same translated text is used each time.
            
            4. **Clarity and Accuracy**: Ensure the translated text is grammatically correct, easy to understand, and appropriate for the game’s context. Avoid translations that sound awkward or unnatural in the target language.
            
            5. **Tone and Style**: Minecraft’s in-game texts are typically casual, friendly, and engaging. Maintain a light and approachable tone that is consistent with the overall feel of the game.
            
            6. **Input and Output Format**: User Content only contains texts need translate.If translation is unnecessary (e.g. proper nouns, codes, etc.), return the original text. You should output the translation **ONLY**.NO explanations. NO notes.
            
            7. **Translate Target**: Translate the user content to Chinese.""";
    private static final String CONTENT_TEMPLE = """
            {
                "model": "%s",
                %s
                "messages": [
                    {
                        "role": "system",
                        "content": "%s"
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ]
            }
            """;
    private static final String INVALID_CONFIG_KEY = "xkball.translator.invalid_config_key";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    public static volatile HttpClient CLIENT = createClient();
    
    private LLMTranslate() {
    }
    
    private static boolean validLLMConfig() {
        try {
            //noinspection ResultOfMethodCallIgnored
            URI.create(XATConfig.LLM_API_URL);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return !XATConfig.LLM_MODEL.isEmpty() && !XATConfig.LLM_SYSTEM_PROMPT.isEmpty();
    }
    
    private static HttpRequest createPostRequest(String text, String lang) {
        var postContent = CONTENT_TEMPLE.formatted(XATConfig.LLM_MODEL, XATConfig.LLM_MODEL_CONFIGURATION, XATConfig.LLM_SYSTEM_PROMPT.formatted(lang), text);
        postContent = postContent.replace('\n', ' ');
        var builder = HttpRequest.newBuilder(URI.create(XATConfig.LLM_API_URL));
        if (!XATConfig.LLM_API_KEY.isEmpty()) {
            builder.header("Authorization", "Bearer " + XATConfig.LLM_API_KEY);
        }
        return builder.header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(postContent))
                .build();
    }
    
    private static String tryRunTranslate(String text, String lang, int retries) {
        if (retries > XATConfig.MAX_RETRIES) {
            throw new RuntimeException("Network error: exceeded maximum retry times. " + text);
        }
        var reqPost = createPostRequest(text,lang);
        return CLIENT.sendAsync(reqPost, HttpResponse.BodyHandlers.ofString()).thenApplyAsync(
                res -> {
                    if (res.statusCode() != 200) {
                        LockSupport.parkNanos(200000);
                        return tryRunTranslate(res.body(), lang, retries + 1);
                    }
                    return getTranslateResult(res.body());
                }
        ).join();
    }
    
    public static String getTranslateResult(String str) {
        try {
            var jsonObj1 = GSON.fromJson(str, JsonObject.class);
            var jsonArray1 = jsonObj1.getAsJsonArray("choices");
            var jsonObj2 = jsonArray1.get(0).getAsJsonObject();
            var jsonObj3 = jsonObj2.getAsJsonObject("message");
            return jsonObj3.get("content").getAsString();
        } catch (Exception e) {
            LOGGER.error("Fail to parse translate result: {}", str, e);
            return I18n.get(ERROR_RESULT_KEY);
        }
    }
    
    public static HttpClient createClient() {
        var builder = HttpClient.newBuilder();
        if (!XATConfig.HTTP_PROXY_HOST.isEmpty()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(XATConfig.HTTP_PROXY_HOST, XATConfig.HTTP_PROXY_PORT)));
        }
        return builder.build();
    }
    
    public CompletableFuture<String> translate(String text, String lang) {
        LOGGER.debug(text);
        if (!validLLMConfig()) return CompletableFuture.completedFuture(I18n.get(INVALID_CONFIG_KEY));
        return CompletableFuture.supplyAsync(() -> tryRunTranslate(text, lang, 0)).exceptionallyAsync(t -> {
            LOGGER.error("Network error", t);
            return I18n.get(ERROR_RESULT_KEY);
        });
    }
}
