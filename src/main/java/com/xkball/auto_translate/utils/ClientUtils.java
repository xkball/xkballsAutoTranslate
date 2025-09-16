package com.xkball.auto_translate.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;

import java.util.List;

public class ClientUtils {
    
    public static ClientLanguage getClientLanguage(String lang){
        var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(lang);
        return ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), List.of(lang), langInfo != null && langInfo.bidirectional());
    }
}
