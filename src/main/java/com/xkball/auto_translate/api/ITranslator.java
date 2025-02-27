package com.xkball.auto_translate.api;

import com.xkball.auto_translate.XATConfig;

import java.util.concurrent.CompletableFuture;

public interface ITranslator {
    
    CompletableFuture<String> translate(String text, String lang);
    
    default CompletableFuture<String> translate(String text) {
        return translate(text, XATConfig.TARGET_LANGUAGE);
    }
}
