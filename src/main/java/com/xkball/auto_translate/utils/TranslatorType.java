package com.xkball.auto_translate.utils;

import com.xkball.auto_translate.api.ITranslator;

import java.util.concurrent.CompletableFuture;

public enum TranslatorType {
    GOOGLE(GoogleTranslate.INSTANCE),
    LLM(LLMTranslate.INSTANCE),
    DEFAULT((s,l) -> CompletableFuture.completedFuture("You didn't choose any translator.Please edit the config file!!!"));
    
    private final ITranslator translator;
    
    TranslatorType(ITranslator translator) {
        this.translator = translator;
    }
    
    public ITranslator getTranslator() {
        return translator;
    }
}
