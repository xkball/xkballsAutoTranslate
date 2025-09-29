package com.xkball.auto_translate.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ClientUtils {
    
    public static ClientLanguage getClientLanguage(String lang){
        var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(lang);
        return ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), List.of(lang), langInfo != null && langInfo.bidirectional());
    }
    
    public static String getAsString(FormattedCharSequence charSequence){
        var s = new FormattedCharToString();
        charSequence.accept(s);
        return s.toString();
    }
    
    private static class FormattedCharToString implements FormattedCharSink{
        
        public final StringBuilder sb = new StringBuilder();
        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
            sb.appendCodePoint(codePoint);
            return true;
        }
        
        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
