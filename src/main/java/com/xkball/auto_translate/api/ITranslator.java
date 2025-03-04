package com.xkball.auto_translate.api;

import com.xkball.auto_translate.XATConfig;

import java.util.concurrent.CompletableFuture;

public interface ITranslator {
    
    String DEFAULT_TRANSLATOR_KEY = "xkball.translator.default_result";
    String TRANSLATING_KEY = "xkball.translator.translating";
    String ERROR_RESULT_KEY = "xkball.translator.error";
    
    CompletableFuture<String> translate(String text, String lang);
    
    default CompletableFuture<String> translate(String text) {
        return translate(text, XATConfig.TARGET_LANGUAGE);
    }
}
