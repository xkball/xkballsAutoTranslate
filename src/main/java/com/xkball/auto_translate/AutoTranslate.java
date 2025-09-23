package com.xkball.auto_translate;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.xkball.auto_translate.client.gui.screen.XATConfigScreen;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.VanillaUtils;
import com.xkball.auto_translate.utils.translate.LangKeyTranslateUnit;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mod(AutoTranslate.MODID)
public class AutoTranslate {

    public static final String MODID = "xkball_s_auto_translate";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean IS_DEBUG = SharedConstants.IS_RUNNING_WITH_JDWP;

    public AutoTranslate(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, XATConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, XATConfigScreen::new);
    }
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
        
        @SubscribeEvent
        public static void onResourceReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) AutoTranslate::updateLanguageMap);
        }
    }
    
    public static void updateLanguageMap(ResourceManager resourceManager) {
        if (XATDataBase.INSTANCE.isEnableInjectLang()){
            injectLanguage();
        }
    }
    
    public static void injectLanguage(){
        LOGGER.info("Injecting language.");
        var map = new HashMap<String, String>();
        map.putAll(Language.getInstance().getLanguageData());
        map.putAll(LangKeyTranslateUnit.I18N_KEYS.toMap());
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
    
}
