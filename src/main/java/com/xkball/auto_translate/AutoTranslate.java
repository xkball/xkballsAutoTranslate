package com.xkball.auto_translate;

import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.llm.ILLMHandler;
import com.xkball.auto_translate.llm.LLMRequest;
import com.xkball.auto_translate.llm.LLMResponse;
import com.xkball.auto_translate.utils.LLMTranslate;
import com.xkball.auto_translate.utils.LegacyUtils;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Mod(AutoTranslate.MODID)
public class AutoTranslate {

    public static final String MODID = "xkball_s_auto_translate";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AutoTranslate(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, XATConfig.SPEC);
    }
    
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
        
        @SubscribeEvent
        public static void onResourceReload(AddClientReloadListenersEvent event) {
            event.addListener(VanillaUtils.modRL("update_language_map"),(ResourceManagerReloadListener) AutoTranslate::updateLanguageMap);
        }
    }
    
    public static final String DEFAULT_OPENAI_MULTIPLE_PROMPT = """
            You will be given a YAML formatted input containing entries with "id" and "text" fields. Here is the input:
            
            <yaml>
            ${yaml}
            </yaml>
            
            For each entry in the YAML, translate the contents of the "text" field into ${targetLanguage}. Write the translation back into the "text" field for that entry.
            
            Here is an example of the expected format:
            
            <example>
            Input:
              - id: 1
                text: Source
            Output:
              - id: 1
                text: Translation
            </example>
            
            Please return the translated YAML directly without wrapping <yaml> tag or include any additional information.""";
    
    public static void updateLanguageMap(ResourceManager resourceManager) {
        var map = new HashMap<String, ClientLanguage>();
        var list = new ArrayList<String>();
        list.add("en_us");
//        list.add("zh_cn");
        for (var key : list) {
            var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(key);
            map.put(key, ClientLanguage.loadFrom(resourceManager, List.of(key), langInfo != null && langInfo.bidirectional()));
        }
//        var sp = "Treat EACH LINE of user content as plain text input and translate it into ${targetLanguage}, output translation result line by line and translation ONLY. If translation is unnecessary (e.g. proper nouns, codes, etc.), return the original text. NO explanations. NO notes.";
        var sp = StrSubstitutor.replace(DEFAULT_OPENAI_MULTIPLE_PROMPT, Map.of("targetLanguage","zh_cn"));
        var entryList = map.get("en_us").getLanguageData().entrySet().stream()
                .filter(entry -> LangKeyTranslateContext.I18N_KEYS.get(entry.getKey()) == null)
                .toList();
        if(entryList.isEmpty()){
            injectLanguage();
            return;
        }
        var partitioned = IntStream.range(0, entryList.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> i / 20))
                .values().stream()
                .map(indexes -> indexes.stream().map(entryList::get).toList())
                .map(LangKeyTranslateContext::new)
                .toList();
        
        LangKeyTranslateContext.contextSize = partitioned.size();
        LangKeyTranslateContext.contextFinished = 0;
        LangKeyTranslateContext.contextError = 0;
        var llmClient = LLMTranslate.createLLMClient();
        for(var lktc : partitioned) {
            var request = new LLMRequest(sp,lktc.createLLMRequestUserPrompt());
            llmClient.addRequest(request,lktc);
        }
        llmClient.send();
    }
    
    public static void injectLanguage(){
        var map = LangKeyTranslateContext.I18N_KEYS.toMap();
        var defaultRightToLeft = Language.getInstance().isDefaultRightToLeft();
        var clientLang = new ClientLanguage(map,defaultRightToLeft,Map.of());
        I18n.setLanguage(clientLang);
        Language.inject(clientLang);
        Minecraft.getInstance().getLanguageManager().reloadCallback.accept(clientLang);
    }
    
    private static class LangKeyTranslateContext implements ILLMHandler {
        
        public static final TranslationCacheSlice I18N_KEYS = XATDataBase.INSTANCE.createSlice("i18n_keys");
        public static int contextSize;
        public static int contextFinished;
        public static int contextError;
        
        public List<String> keys = new ArrayList<>();
        public List<String> values = new ArrayList<>();
        
        public LangKeyTranslateContext(List<Map.Entry<String, String>> elt){
            for(Map.Entry<String, String> entry : elt){
                if(entry.getValue().isEmpty()){
                    LOGGER.debug("empty key value pair: {},{}", entry.getKey(), entry.getValue());
                    continue;
                }
                keys.add(entry.getKey());
                values.add(entry.getValue());
            }
        }
        
        public String createLLMRequestUserPrompt(){
            List<Map<String, Object>> items = new ArrayList<>();
            for(var i = 0; i < keys.size(); i++){
                items.add(Map.of("id",i,"text",values.get(i)));
            }
            return LegacyUtils.toYaml(items);
        }
        
        public void checkFinish(){
            if(contextFinished == contextSize){
                contextFinished = 0;
                contextSize = 0;
                contextError = 0;
                AutoTranslate.injectLanguage();
            }
        }
        
        @Override
        public boolean handle(LLMResponse response) {
            List<Map<String, Object>> translatedItems = LegacyUtils.parseYaml(response.getContent());
            List<String> translateResult = new ArrayList<>();
            for(var map : translatedItems){
                translateResult.add((Integer)map.get("id"), (String) map.get("text"));
            }
//            System.out.println(contextFinished);
//            System.out.println(translateResult);
            if(translateResult.size() != this.keys.size()){
                this.checkFinish();
                return false;
            }
            for(int i = 0; i < this.keys.size(); i++){
                I18N_KEYS.put(this.keys.get(i),translateResult.get(i));
            }
            contextFinished += 1;
            this.checkFinish();
            return true;
        }
        
        @Override
        public void onRetriesExceeded() {
            contextFinished += 1;
            contextError += 1;
            this.checkFinish();
        }
    }

}
