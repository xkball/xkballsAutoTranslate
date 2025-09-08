package com.xkball.auto_translate.llm;

import java.util.Map;

public record LLMRequest(String systemPrompt, String userPrompt, Map<String, String> params) {

    public LLMRequest(String systemPrompt, String userPrompt){
        this(systemPrompt,userPrompt,Map.of());
    }
}
