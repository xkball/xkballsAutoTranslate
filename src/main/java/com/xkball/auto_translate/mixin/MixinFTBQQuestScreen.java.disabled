package com.xkball.auto_translate.mixin;

import com.xkball.auto_translate.api.IXATQuestScreenExtension;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

@Mixin(QuestScreen.class)
public abstract class MixinFTBQQuestScreen extends BaseScreen implements IXATQuestScreenExtension {
    
    @Unique
    private static final MethodHandle VIEW_QUEST_PANEL_BUILD_INDEX;
    
    static {
        try {
            var method = ViewQuestPanel.class.getDeclaredMethod("buildPageIndices");
            method.setAccessible(true);
            VIEW_QUEST_PANEL_BUILD_INDEX = MethodHandles.lookup().unreflect(method);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Shadow public abstract void refreshViewQuestPanel();
    
    @Shadow @Final public ViewQuestPanel viewQuestPanel;
    
    @Shadow public abstract void tick();
    
    @Shadow public abstract @Nullable Quest getViewedQuest();
    
    @Unique
    private volatile boolean xkball_sAutoTranslate$needRefresh = false;
    
    private MixinFTBQQuestScreen() {
        super();
    }
    
    @Override
    public void xkball_sAutoTranslate$markNeedRefresh() {
        xkball_sAutoTranslate$needRefresh = true;
    }
    
    @Inject(method = "tick",at = @At("HEAD"))
    public void onTick(CallbackInfo ci){
        if(xkball_sAutoTranslate$needRefresh){
            xkball_sAutoTranslate$needRefresh = false;
            if(this.getViewedQuest() == null) return;
            this.viewQuestPanel.getViewedQuest().getTitle();
            this.viewQuestPanel.getViewedQuest().getSubtitle();
            this.viewQuestPanel.getViewedQuest().getDescription();
            try {
                VIEW_QUEST_PANEL_BUILD_INDEX.invoke(this.viewQuestPanel);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            refreshViewQuestPanel();
        }
    }
}
