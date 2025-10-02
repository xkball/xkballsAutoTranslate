package com.xkball.auto_translate.client;

import com.mojang.datafixers.util.Either;
import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemStackTooltipTranslator {
    
    public static final Map<String,String> translating = new ConcurrentHashMap<>();
    public static final TranslationCacheSlice tooltipCache = XATDataBase.INSTANCE.createSlice("tooltip");
    private static final Style DARK_GRAY = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
    @Nullable
    private static volatile ItemStack track = null;
    private static volatile boolean force = false;
    
    @SuppressWarnings("UnstableApiUsage")
    public static void submit(@Nullable ItemStack stack, boolean force) {
        if (stack == null) return;
        if(stack.isEmpty()) return;
        track = stack;
        ItemStackTooltipTranslator.force = force;
        var window = Minecraft.getInstance().getWindow();
        var textList = Screen.getTooltipFromItem(Minecraft.getInstance(),stack);
        ForgeHooksClient.gatherTooltipComponents(stack,textList,stack.getTooltipImage(),0,window.getScreenWidth(),window.getScreenHeight(),Minecraft.getInstance().font);
        track = null;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event){
        if(event.getItemStack().equals(track)) {
            submit(event.getTooltipElements(),force);
        }
        var newTooltips = new ArrayList<Either<FormattedText, TooltipComponent>>();
        for(var either : event.getTooltipElements()) {
            either.ifLeft(text ->{
                        newTooltips.add(either);
                        var str = text.getString();
                        if(str.isEmpty()) return;
                        var translating = ItemStackTooltipTranslator.translating.containsKey(str);
                        if(translating) {
                            newTooltips.add(Either.left(FormattedText.of(I18n.get(ITranslator.TRANSLATING_KEY), DARK_GRAY)));
                        }
                        else{
                            var trResult = tooltipCache.get(str);
                            if(trResult != null) {
                                newTooltips.add(Either.left(FormattedText.of(trResult, DARK_GRAY)));
                            }
                        }
            });
            either.ifRight(text -> newTooltips.add(either));
        }
        event.getTooltipElements().clear();
        event.getTooltipElements().addAll(newTooltips);
    }
    
    private static void submit(List<Either<FormattedText, TooltipComponent>> components,boolean force) {
        for(var either : components) {
            either.ifLeft(text -> {
                var str = text.getString();
                if(str.isEmpty() || (!force && tooltipCache.get(str) != null)) return;
                translating.put(str,"");
                XATConfig.TRANSLATOR_TYPE.getTranslator().translate(str).whenCompleteAsync((result, t) -> {
                    translating.remove(str);
                    tooltipCache.put(str,result);
                });
            });
        }
    }
}
