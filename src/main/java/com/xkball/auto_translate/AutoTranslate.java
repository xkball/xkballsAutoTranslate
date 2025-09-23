package com.xkball.auto_translate;

import com.xkball.auto_translate.client.gui.frame.core.IPanel;
import com.xkball.auto_translate.client.gui.screen.XATConfigScreen;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.llm.ILLMHandler;
import com.xkball.auto_translate.llm.LLMResponse;
import com.xkball.auto_translate.utils.LegacyUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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
import java.util.Objects;


@Mod(value = AutoTranslate.MODID,dist = Dist.CLIENT)
public class AutoTranslate {

    public static final String MODID = "xkball_s_auto_translate";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IS_DEBUG = SharedConstants.IS_RUNNING_WITH_JDWP;

    public AutoTranslate(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, XATConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, XATConfigScreen::new);
    }
    
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT,bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
        
        @SubscribeEvent
        public static void onResourceReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) AutoTranslate::updateLanguageMap);
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
        if (XATDataBase.INSTANCE.isEnableInjectLang()){
            injectLanguage();
        }
    }
    
    public static void injectLanguage(){
        LOGGER.info("Injecting language.");
        var map = new HashMap<String, String>();
        map.putAll(Language.getInstance().getLanguageData());
        map.putAll(LangKeyTranslateContext.I18N_KEYS.toMap());
        var defaultRightToLeft = Language.getInstance().isDefaultRightToLeft();
        var clientLang = new ClientLanguage(map,defaultRightToLeft,Map.of());
        I18n.setLanguage(clientLang);
        Language.inject(clientLang);
        Minecraft.getInstance().getLanguageManager().reloadCallback.accept(clientLang);
        IPanel.GLOBAL_UPDATE_MARKER.setNeedUpdate();
    }
    
    public static void cancelInjectLanguage(){
        LOGGER.info("Cancel Inject language.");
        var langManger = Minecraft.getInstance().getLanguageManager();
        var currentCode = langManger.getSelected();
        List<String> list = new ArrayList<>(2);
        var flag = Objects.requireNonNull(langManger.getLanguage("en_us")).bidirectional();
        list.add("en_us");
        if (!currentCode.equals("en_us")) {
            var languageinfo = langManger.getLanguage(currentCode);
            if (languageinfo != null) {
                list.add(currentCode);
                flag = languageinfo.bidirectional();
            }
        }
        ClientLanguage clientlanguage = ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), list, flag);
        I18n.setLanguage(clientlanguage);
        Language.inject(clientlanguage);
        Minecraft.getInstance().getLanguageManager().reloadCallback.accept(clientlanguage);
    }
    
    public static class LangKeyTranslateContext implements ILLMHandler {
        
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
            LOGGER.debug("Translating: {}/{}", contextFinished, contextSize);
            IPanel.GLOBAL_UPDATE_MARKER.setNeedUpdate();
            if(contextFinished == contextSize){
                contextFinished = 0;
                contextSize = 0;
                contextError = 0;
                AutoTranslate.injectLanguage();
            }
        }
        
        @Override
        public boolean handle(LLMResponse response) {
            LOGGER.debug(response.getContent());
            List<String> translateResult = new ArrayList<>();
            try {
                List<Map<String, Object>> translatedItems = LegacyUtils.parseYaml(response.getContent());
                for(var map : translatedItems){
                    translateResult.add((Integer)map.get("id"), (String) map.get("text"));
                }
            }catch(Exception e){
                return false;
            }
            if(translateResult.size() != this.keys.size()){
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
