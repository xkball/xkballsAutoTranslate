package com.xkball.auto_translate;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;


@Mod(AutoTranslate.MODID)
public class AutoTranslate {

    public static final String MODID = "xkball_s_auto_translate";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AutoTranslate(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, XATConfig.SPEC);
    }
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
