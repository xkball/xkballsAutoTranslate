package com.xkball.auto_translate.mixin;

import com.xkball.auto_translate.api.IXATQuestExtension;
import com.xkball.auto_translate.crossmod.CrossModBridge;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestObjectBase.class,remap = false)
@SuppressWarnings("ConstantValue")
public class MixinFTBQQuestObjectBase {
    
    @Shadow private Component cachedTitle;
    
    @Inject(method = "getTitle",at = @At("HEAD"))
    public void beforeGetTitle(CallbackInfoReturnable<Component> cir){
        if((Object)this instanceof Quest quest){
            if(IXATQuestExtension.asExtension(quest).xkball_sAutoTranslate$isInvalidTitleCache()) this.cachedTitle = null;
            if(this.cachedTitle == null) IXATQuestExtension.asExtension(quest).xkball_sAutoTranslate$invalidTitleCache();
        }
    }
    
    @Inject(method = "getTitle",at = @At("RETURN"))
    public void afterGetTitle(CallbackInfoReturnable<Component> cir){
        if((Object)this instanceof Quest quest && IXATQuestExtension.asExtension(quest).xkball_sAutoTranslate$isInvalidTitleCache()){
            IXATQuestExtension.asExtension(quest).xkball_sAutoTranslate$setValidTitleCache(false);
            var translation = CrossModBridge.FTBQ_CACHE.get(cachedTitle.getString());
            if(translation != null){
                this.cachedTitle = Component.empty().append(cachedTitle).append(Component.literal(translation).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
