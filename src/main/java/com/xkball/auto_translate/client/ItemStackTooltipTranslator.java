package com.xkball.auto_translate.client;

import com.mojang.datafixers.util.Either;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
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
    @Nullable
    private static ItemStack track = null;
    
    public static void submit(@Nullable ItemStack stack, GuiGraphics graphics) {
        if (stack == null) return;
        if(stack.isEmpty()) return;
        track = stack;
        graphics.renderTooltip(Minecraft.getInstance().font, stack, 0, 0);
        track = null;
    }
    
    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event){
        if(event.getItemStack().equals(track)) {
            submit(event.getTooltipElements());
        }
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
    
    private static void submit(List<Either<FormattedText, TooltipComponent>> components){
        for(var either : components) {
            either.ifLeft(text -> {
                var str = text.getString();
                if(str.isEmpty()) return;
                translationMappings.put(str, I18n.get(ITranslator.TRANSLATING_KEY));
                XATConfig.TRANSLATOR_TYPE.getTranslator().translate(str).whenCompleteAsync((result, t) -> translationMappings.put(str, result));
            });
        }
    }
}
