package com.xkball.auto_translate.utils;

import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import com.xkball.auto_translate.event.XATConfigUpdateEvent;
import com.xkball.auto_translate.llm.LLMClientImpl;
import com.xkball.auto_translate.llm.LLMRequest;
import com.xkball.auto_translate.llm.LLMResponse;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class LLMTranslate implements ITranslator {
    
    public static final LLMTranslate INSTANCE = new LLMTranslate();
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
    public static final String CONTENT_TEMPLE = """
            {
                "model": "${model}",
                ${modelConfig}
                "messages": [
                    {
                        "role": "system",
                        "content": "${systemPrompt}"
                    },
                    {
                        "role": "user",
                        "content": "${content}"
                    }
                ]
            }
            """;
    private static final String INVALID_CONFIG_KEY = "xkball.translator.invalid_config_key";
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile LLMClientImpl client = createLLMClient();
    
    private LLMTranslate() {
    }
    
    private static LLMClientImpl createLLMClient(){
        return new LLMClientImpl.Builder()
                .setUrl(XATConfig.LLM_API_URL)
                .setApiKey(XATConfig.LLM_API_KEY)
                .setModel(XATConfig.LLM_MODEL)
                .setMaxRetries(XATConfig.MAX_RETRIES)
                .build();
    }
    
    private static LLMRequest createPostRequest(String text, String lang) {
        var systemPrompt = StrSubstitutor.replace(XATConfig.LLM_SYSTEM_PROMPT,Map.of("targetLanguage", lang));
        return new LLMRequest(systemPrompt,text);
    }
    
    public CompletableFuture<String> translate(String text, String lang) {
        LOGGER.debug(text);
        //todo: 会阻塞
//        if (!this.client.valid()) return CompletableFuture.completedFuture(I18n.get(INVALID_CONFIG_KEY));
        var reqPost = createPostRequest(text,lang);
        return client.sendAsync(reqPost, LLMResponse::getContent).exceptionallyAsync(t -> {
            LOGGER.warn("Network error", t);
            return I18n.get(ERROR_RESULT_KEY);
        });
    }
    
    @SubscribeEvent
    public static void onUpdateHttpConfig(XATConfigUpdateEvent.LLM event){
        if(event.changed()){
            LLMTranslate.INSTANCE.client = createLLMClient();
        }
    }
    
}
