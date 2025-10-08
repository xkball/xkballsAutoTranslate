package com.xkball.auto_translate.utils.translate;

import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ILangKeyTrContext;
import com.xkball.auto_translate.llm.LLMRequest;
import com.xkball.auto_translate.llm.LLMResponse;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.util.Map;

public class SingleLangKeyTranslateContext implements ILangKeyTrContext {
    
    private final LangKeyTranslateUnit unit;
    private final String key;
    private final String value;
    private final boolean fallback;
    private String result;
    
    public SingleLangKeyTranslateContext(LangKeyTranslateUnit unit, String key, String value) {
        this(unit, key, value, false);
    }
    
    public SingleLangKeyTranslateContext(LangKeyTranslateUnit unit, String key, String value, boolean fallback) {
        this.unit = unit;
        this.key = key;
        this.value = value;
        this.fallback = fallback;
    }
    
    @Override
    public Map<String, String> getRawMap() {
        return Map.of(key, value);
    }
    
    @Override
    public Map<String, String> getResultMap() {
        return Map.of(key, result);
    }
    
    @Override
    public LLMRequest createLLMRequest() {
        var systemPrompt = StrSubstitutor.replace(XATConfig.LLM_SYSTEM_PROMPT,Map.of("targetLanguage", XATConfig.TARGET_LANGUAGE));
        return new LLMRequest(systemPrompt,value);
    }
    
    @Override
    public boolean handle(LLMResponse response) {
        this.result = response.getContent();
        this.unit.submitResultFinished(this);
        return true;
    }
    
    @Override
    public void onRetriesExceeded() {
        this.unit.submitResultError(this);
    }
    
    @Override
    public boolean fallback() {
        return fallback;
    }
    
    @Override
    public String toString() {
        return "SingleLangKeyTranslateContext{" +
                "unit=" + unit +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
