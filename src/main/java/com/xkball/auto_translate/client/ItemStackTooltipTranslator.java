package com.xkball.auto_translate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.xkball.auto_translate.XATConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemStackTooltipTranslator {
    
    public static final Map<String,String> translationMappings = new ConcurrentHashMap<>();
    private static final Style DARK_GRAY = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
    
    public static void submit(@Nullable ItemStack stack, Screen graphics) {
        if (stack == null) return;
        if(stack.isEmpty()) return;
        submit(graphics.getTooltipFromItem(stack));
    }
    
    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event){
        var newTooltips = new ArrayList<Either<FormattedText, TooltipComponent>>();
        for(var either : event.getTooltipElements()) {
            either.ifLeft(text ->{
                        newTooltips.add(either);
                        var str = text.getString();
                        if(str.isEmpty()) return;
                        var translation = translationMappings.get(str);
                        if(translation != null) {
                            newTooltips.add(Either.left(FormattedText.of(translation, DARK_GRAY)));
                        }
            });
            either.ifRight(text -> newTooltips.add(either));
        }
        event.getTooltipElements().clear();
        event.getTooltipElements().addAll(newTooltips);
    }
    
    private static void submit(List<Component> components){
        for(var c : components) {
            var str = c.getString();
            if(str.isEmpty()) return;
            translationMappings.put(str, "翻译中...");
            XATConfig.TRANSLATOR_TYPE.getTranslator().translate(str).whenCompleteAsync((result, t) -> translationMappings.put(str, result));
        }
    }
}
