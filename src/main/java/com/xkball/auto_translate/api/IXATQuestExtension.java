package com.xkball.auto_translate.api;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IXATQuestExtension {
    
    static IXATQuestExtension asExtension(Object quest){
        return (IXATQuestExtension) quest;
    }
    
    default void clearAllCache(){
        xkball_sAutoTranslate$invalidTitleCache();
        xkball_sAutoTranslate$invalidSubtitleCache();
        xkball_sAutoTranslate$invalidDescriptionCache();
    }
    
    Component xkball_sAutoTranslate$getTitleUnmodified();
    Component xkball_sAutoTranslate$getSubtitleUnmodified();
    List<Component> xkball_sAutoTranslate$getDescriptionUnmodified();
    
    void xkball_sAutoTranslate$invalidTitleCache();
    void xkball_sAutoTranslate$invalidSubtitleCache();
    void xkball_sAutoTranslate$invalidDescriptionCache();
    
    
    void xkball_sAutoTranslate$setValidTitleCache(boolean validCache);
    void xkball_sAutoTranslate$setValidSubtitleCache(boolean validCache);
    void xkball_sAutoTranslate$setValidDescriptionCache(boolean validCache);
    
    boolean xkball_sAutoTranslate$isInvalidTitleCache();
    boolean xkball_sAutoTranslate$isInvalidSubtitleCache();
    boolean xkball_sAutoTranslate$isInvalidDescriptionCache();
}
