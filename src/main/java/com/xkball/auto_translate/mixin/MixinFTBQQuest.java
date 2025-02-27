package com.xkball.auto_translate.mixin;

import com.mojang.datafixers.util.Pair;
import com.xkball.auto_translate.api.IXATQuestExtension;
import com.xkball.auto_translate.crossmod.CrossModBridge;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(Quest.class)
public abstract class MixinFTBQQuest extends QuestObject implements IXATQuestExtension {
    
    @Shadow private Component cachedSubtitle;
    @Shadow private List<Component> cachedDescription;
    
    @Shadow public abstract List<String> getRawDescription();
    
    @Shadow public abstract String getRawSubtitle();
    
    @Unique
    private volatile boolean xkball_sAutoTranslate$invalidedTitleCache = false;
    @Unique
    private volatile boolean xkball_sAutoTranslate$invalidedSubtitleCache = false;
    @Unique
    private volatile boolean xkball_sAutoTranslate$invalidedDescriptionCache = false;
    
    private MixinFTBQQuest(long id) {
        super(id);
    }
    
    @Inject(method = "buildDescriptionIndex",at = @At("RETURN"))
    public void onBuildPageIndex(CallbackInfoReturnable<List<Pair<Integer, Integer>>> cir){
        if(cachedDescription == null) return;
        var list = cir.getReturnValue();
        if(list.isEmpty()) return;
        var last = list.getLast();
        list.set(list.size() - 1, Pair.of(last.getFirst(), cachedDescription.size()-1));
    }
    
    @Inject(method = "getSubtitle",at = @At("HEAD"))
    public void beforeGetSubtitle(CallbackInfoReturnable<Component> cir){
        if(this.xkball_sAutoTranslate$isInvalidSubtitleCache()){
            this.cachedSubtitle = null;
        }
        if(this.cachedSubtitle == null){
            this.xkball_sAutoTranslate$invalidSubtitleCache();
        }
    }
    
    @Inject(method = "getSubtitle",at = @At("RETURN"))
    public void afterGetSubtitle(CallbackInfoReturnable<Component> cir){
        if(this.xkball_sAutoTranslate$isInvalidSubtitleCache()){
            this.xkball_sAutoTranslate$setValidSubtitleCache(false);
            var translation = CrossModBridge.FTBQHandler.translationMappings.get(cachedSubtitle.getString());
            if(translation != null){
                this.cachedSubtitle = Component.empty().append(cachedSubtitle).append(Component.literal(translation).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
    
    @Inject(method = "getDescription",at = @At("HEAD"))
    public void beforeGetDescription(CallbackInfoReturnable<Component> cir){
        if(this.xkball_sAutoTranslate$isInvalidDescriptionCache()){
            this.cachedDescription = null;
        }
        if(this.cachedDescription == null){
            this.xkball_sAutoTranslate$invalidDescriptionCache();
        }
    }
    
    @Inject(method = "getDescription",at = @At("RETURN"))
    public void afterGetDescription(CallbackInfoReturnable<Component> cir){
        if(this.xkball_sAutoTranslate$isInvalidDescriptionCache()){
            this.xkball_sAutoTranslate$setValidDescriptionCache(false);
            assert cachedDescription != null;
            var list = new ArrayList<>(cachedDescription);
            list.add(Component.empty());
            for(var component : cachedDescription){
                var translation = CrossModBridge.FTBQHandler.translationMappings.get(component.getString());
                if(translation != null) list.add(Component.literal(translation).withStyle(component.getStyle()));
            }
            cachedDescription = Collections.unmodifiableList(list);
        }
    }
    
    @Unique
    public List<Component> xkball_sAutoTranslate$getDescriptionUnmodified(){
        return this.getRawDescription().stream().map((str) -> TextUtils.parseRawText(str, this.holderLookup())).toList();
    }
    
    @Unique
    public Component xkball_sAutoTranslate$getSubtitleUnmodified(){
        return TextUtils.parseRawText(this.getRawSubtitle(), this.holderLookup());
    }
    
    @Unique
    public Component xkball_sAutoTranslate$getTitleUnmodified(){
        if (!this.getRawTitle().isEmpty()) {
            return TextUtils.parseRawText(this.getRawTitle(), this.holderLookup());
        } else {
            return this.getAltTitle();
        }
    }
    
    
    @Override
    public void xkball_sAutoTranslate$invalidTitleCache() {
        xkball_sAutoTranslate$invalidedTitleCache = true;
    }
    
    @Override
    public boolean xkball_sAutoTranslate$isInvalidTitleCache() {
        return xkball_sAutoTranslate$invalidedTitleCache;
    }
    
    @Override
    public void xkball_sAutoTranslate$invalidSubtitleCache() {
        xkball_sAutoTranslate$invalidedSubtitleCache = true;
    }
    
    @Override
    public boolean xkball_sAutoTranslate$isInvalidSubtitleCache() {
        return xkball_sAutoTranslate$invalidedSubtitleCache;
    }
    
    @Override
    public void xkball_sAutoTranslate$invalidDescriptionCache() {
        xkball_sAutoTranslate$invalidedDescriptionCache = true;
    }
    
    @Override
    public boolean xkball_sAutoTranslate$isInvalidDescriptionCache() {
        return xkball_sAutoTranslate$invalidedDescriptionCache;
    }
    
    @Override
    public void xkball_sAutoTranslate$setValidTitleCache(boolean validCache) {
        xkball_sAutoTranslate$invalidedTitleCache = validCache;
    }
    
    @Override
    public void xkball_sAutoTranslate$setValidSubtitleCache(boolean validCache) {
        xkball_sAutoTranslate$invalidedSubtitleCache = validCache;
    }
    
    @Override
    public void xkball_sAutoTranslate$setValidDescriptionCache(boolean validCache) {
        xkball_sAutoTranslate$invalidedDescriptionCache = validCache;
    }
}
