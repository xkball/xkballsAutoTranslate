package com.xkball.auto_translate.mixin;

import com.mojang.datafixers.util.Pair;
import com.xkball.auto_translate.api.IXATQuestExtension;
import dev.ftb.mods.ftbquests.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ViewQuestPanel.class, remap = false)
public class MixinFTBViewQuestPanel {
    
    @Shadow private Quest quest;
    
    @Shadow @Final private List<Pair<Integer, Integer>> pageIndices;
    
    @Inject(method = "buildPageIndices",at = @At("RETURN"))
    public void onBuildPageIndex(CallbackInfo ci){
        if(this.quest == null) return;
        if(this.pageIndices.isEmpty()) return;
        var qExtension = IXATQuestExtension.asExtension(this.quest);
        if(qExtension.xkball_sAutoTranslate$getDescriptionCached().isEmpty()) return;
        var last = this.pageIndices.get(pageIndices.size()-1);
        this.pageIndices.set(pageIndices.size() - 1, Pair.of(last.getFirst(), qExtension.xkball_sAutoTranslate$getDescriptionCached().size()-1));
    }
}
