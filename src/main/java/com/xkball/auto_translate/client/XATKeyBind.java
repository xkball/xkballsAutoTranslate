package com.xkball.auto_translate.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xkball.auto_translate.event.XATGatherTranslateInputEvent;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class XATKeyBind {
    
    public static final Lazy<KeyMapping> TRANSLATE_KEY = Lazy.of(() -> new KeyMapping("keys.xkball_s_auto_translate.translate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T,"key.categories.misc"));
    public static final Lazy<KeyMapping> RE_TRANSLATE_KEY = Lazy.of(() -> new KeyMapping("keys.xkball_s_auto_translate.retranslate", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T,"key.categories.misc"));
    
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TRANSLATE_KEY.get());
        event.register(RE_TRANSLATE_KEY.get());
    }
    
    @EventBusSubscriber( value = Dist.CLIENT)
    public static class GameEventHandler{
        
        @SubscribeEvent
        public static void onKeyInput(ScreenEvent.KeyPressed.Pre event){
            if(TRANSLATE_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))){
                NeoForge.EVENT_BUS.post(new XATGatherTranslateInputEvent(false));
            }
            if(RE_TRANSLATE_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))){
                NeoForge.EVENT_BUS.post(new XATGatherTranslateInputEvent(true));
            }
        }
    }
    
}
