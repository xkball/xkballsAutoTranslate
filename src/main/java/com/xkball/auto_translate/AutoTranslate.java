package com.xkball.auto_translate;

import com.xkball.auto_translate.client.gui.frame.core.IPanel;
import com.xkball.auto_translate.client.gui.screen.XATConfigScreen;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.translate.LangKeyTranslateUnit;
import com.xkball.auto_translate.utils.translate.TranslatorType;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
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
@EventBusSubscriber()
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
    
    public static void updateLanguageMap(ResourceManager resourceManager) {
        if (XATDataBase.INSTANCE.isEnableInjectLang()){
            injectLanguage();
        }
    }
    
    @SubscribeEvent
    public static void onPlayerEnterWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        var player = event.getPlayer();
        var flag = false;
        if(XATConfig.TRANSLATOR_TYPE == TranslatorType.DEFAULT){
            flag = true;
            player.displayClientMessage(Component.translatable("xat.warn").withStyle(ChatFormatting.WHITE)
                    .append(Component.translatable("xat.warn.no_translator").withStyle(ChatFormatting.RED)),false);
        }
        if(!XATDataBase.INSTANCE.isEnableInjectLang()){
            flag = true;
            player.displayClientMessage(Component.translatable("xat.warn").withStyle(ChatFormatting.WHITE)
                    .append(Component.translatable("xat.warn.no_inject").withStyle(ChatFormatting.RED)),false);
        }
        if (flag) {
            player.displayClientMessage(Component.translatable("xat.warn").withStyle(ChatFormatting.WHITE)
                    .append(Component.translatable("xat.warn.open_config_screen")
                    .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GREEN)
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/xat open_config_screen")))),false);
        }
    }
    
    @SubscribeEvent
    public static void onRegClientCommand(RegisterClientCommandsEvent event){
        event.getDispatcher().register(
                Commands.literal("xat")
                        .then(Commands.literal("open_config_screen")
                        .executes(s -> {
                            Minecraft.getInstance().setScreen(new XATConfigScreen(null, null));
                            return 0;
                        })));
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
