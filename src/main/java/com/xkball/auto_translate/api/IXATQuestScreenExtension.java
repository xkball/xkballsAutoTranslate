package com.xkball.auto_translate.api;

public interface IXATQuestScreenExtension {
    
    static IXATQuestScreenExtension asExtension(Object panel){
        return (IXATQuestScreenExtension) panel;
    }
    
    void xkball_sAutoTranslate$markNeedRefresh();
}
