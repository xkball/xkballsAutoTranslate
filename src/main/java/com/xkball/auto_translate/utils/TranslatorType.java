package com.xkball.auto_translate.utils;

import com.xkball.auto_translate.api.ITranslator;
import net.minecraft.client.resources.language.I18n;

import java.util.concurrent.CompletableFuture;

public enum TranslatorType {
    GOOGLE(GoogleTranslate.INSTANCE),
    LLM(LLMTranslate.INSTANCE),
    DEFAULT((s,l) -> CompletableFuture.completedFuture(I18n.get(ITranslator.DEFAULT_TRANSLATOR_KEY)));
    
    private final ITranslator translator;
    
    TranslatorType(ITranslator translator) {
        this.translator = translator;
    }
    
    public ITranslator getTranslator() {
        return translator;
    }
}
