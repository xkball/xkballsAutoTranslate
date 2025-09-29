package com.xkball.auto_translate.crossmod;

import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;
import com.xkball.auto_translate.api.IXATQuestExtension;
import com.xkball.auto_translate.api.IXATQuestScreenExtension;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CrossModBridge {
    
    @Nullable
    public static ItemStack getHoverItemOnJEIOverlay(){
        if(ModList.get().isLoaded("jei")){
            return JEIHandler.getHoverItemOnJEIOverlay();
        }
        return null;
    }
    
    public static void tryTranslateFTBQuest(boolean force){
        if(ModList.get().isLoaded("ftbquests")){
            FTBQHandler.tryTranslateQuest(force);
        }
    }
    
    private static class JEIHandler{
        
        @Nullable
        public static ItemStack getHoverItemOnJEIOverlay(){
            if(XAT_JEIPlugin.runtime==null) return null;
            var runtime = XAT_JEIPlugin.runtime;
            var is = runtime.getIngredientListOverlay().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
            if(is != null) return is;
            is = runtime.getBookmarkOverlay().getIngredientUnderMouse(VanillaTypes.ITEM_STACK);
            if(is != null) return is;
            return runtime.getRecipesGui().getIngredientUnderMouse(VanillaTypes.ITEM_STACK).orElse(null);
        }
    }
    
    public static class FTBQHandler{
        
        public static final Map<String,String> translationMappings = new ConcurrentHashMap<>();
        
        public static void tryTranslateQuest(boolean force){
            if(!(event.getScreen() instanceof IScreenWrapper isw)) return;
            if(!(isw.getGui() instanceof QuestScreen questScreen)) return;
            var quest = questScreen.getViewedQuest();
            if(quest == null) return;
            var qExtension = IXATQuestExtension.asExtension(quest);
            var qsExtension = IXATQuestScreenExtension.asExtension(questScreen);
            translate(qExtension.xkball_sAutoTranslate$getTitleUnmodified().getString()).thenRunAsync(() -> {
                    qExtension.xkball_sAutoTranslate$invalidTitleCache();
                    qsExtension.xkball_sAutoTranslate$markNeedRefresh();
            });
            translate(qExtension.xkball_sAutoTranslate$getSubtitleUnmodified().getString()).thenRunAsync(() -> {
                qExtension.xkball_sAutoTranslate$invalidSubtitleCache();
                qsExtension.xkball_sAutoTranslate$markNeedRefresh();
            });
            CompletableFuture.allOf(qExtension.xkball_sAutoTranslate$getDescriptionUnmodified().stream().map(Component::getString).map(FTBQHandler::translate).toArray(CompletableFuture[]::new))
                    .thenRunAsync(() -> {
                        qExtension.xkball_sAutoTranslate$invalidDescriptionCache();
                        qsExtension.xkball_sAutoTranslate$markNeedRefresh();
                    });
            qExtension.clearAllCache();
            qsExtension.xkball_sAutoTranslate$markNeedRefresh();
        }
        
        private static CompletableFuture<Void> translate(String str){
            if(str.isEmpty()) return CompletableFuture.completedFuture(null);
            translationMappings.put(str, I18n.get(ITranslator.TRANSLATING_KEY));
            return XATConfig.TRANSLATOR_TYPE.getTranslator().translate(str).thenAcceptAsync(result -> translationMappings.put(str,result));
        }
    }
}
