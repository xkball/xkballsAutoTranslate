package com.xkball.auto_translate.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xkball.auto_translate.crossmod.CrossModBridge;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class XATKeyBind {
    
    public static final Lazy<KeyMapping> TRANSLATE_KEY = Lazy.of(() -> new KeyMapping("keys.xkball_s_auto_translate.translate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T,"key.categories.misc"));
    public static boolean markItemNextFrame = false;
    
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TRANSLATE_KEY.get());
    }
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class GameEventHandler{
        @SubscribeEvent
        public static void beforeRender(ScreenEvent.Render.Pre event) {
            if(!markItemNextFrame) return;
            markItemNextFrame = false;
            if(event.getScreen() instanceof AbstractContainerScreen<?> acs && acs.hoveredSlot != null){
                ItemStackTooltipTranslator.submit(acs.hoveredSlot.getItem(),event.getGuiGraphics());
            }
            ItemStackTooltipTranslator.submit(CrossModBridge.getHoverItemOnJEIOverlay(),event.getGuiGraphics());
            CrossModBridge.tryTranslateFTBQuest(event);
        }
        
        @SubscribeEvent
        public static void onKeyInput(ScreenEvent.KeyPressed.Pre event){
            if(!TRANSLATE_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) return;
            markItemNextFrame = true;
        }
    }
    
}
