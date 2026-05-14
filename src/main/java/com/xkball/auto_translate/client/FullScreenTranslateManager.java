package com.xkball.auto_translate.client;

import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.ClientUtils;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT)
public class FullScreenTranslateManager {

    private static volatile boolean enabled = false;
    private static final TranslationCacheSlice CACHE = XATDataBase.INSTANCE.createSlice("full_screen");
    private static final Map<String, Boolean> IN_FLIGHT = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> SKIP_TRANSLATE = ThreadLocal.withInitial(() -> false);

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (enabled) {
            enabled = false;
        }
    }

    public static GuiTextRenderState resolveAndSubmit(GuiTextRenderState original) {
        if (SKIP_TRANSLATE.get()) return original;
        var raw = ClientUtils.getAsString(original.text);
        if (raw.isBlank()) return original;
        var cached = CACHE.get(raw);
        if (cached != null) {
            var newText = Component.literal(cached).getVisualOrderText();
            return new GuiTextRenderState(
                    original.font, newText, original.pose,
                    original.x, original.y, original.color, original.backgroundColor,
                    original.dropShadow, false, original.scissor
            );
        }
        if (!IN_FLIGHT.containsKey(raw)) {
            IN_FLIGHT.put(raw, Boolean.TRUE);
            XATConfig.TRANSLATOR_TYPE.getTranslator().translate(raw).whenCompleteAsync((result, t) -> {
                IN_FLIGHT.remove(raw);
                CACHE.put(raw, result);
            });
        }
        return original;
    }

    public static void renderOverlay(GuiGraphicsExtractor guiGraphics, int screenHeight) {
        if (!enabled) return;
        var font = Minecraft.getInstance().font;
        var statusText = Component.translatable("xat.gui.full_screen_translate.enabled",
                XATKeyBind.FULL_SCREEN_TRANSLATE_KEY.get().getTranslatedKeyMessage());
        int iconSize = 16;
        int x = 5;
        int y = screenHeight - 22;

        SKIP_TRANSLATE.set(true);
        try {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, VanillaUtils.modRL("icon/xat_icon"),x, y, iconSize, iconSize);
            guiGraphics.text(font, statusText, x + iconSize + 4, y + 4, 0xFF55FF55);
        } finally {
            SKIP_TRANSLATE.set(false);
        }
    }
}
