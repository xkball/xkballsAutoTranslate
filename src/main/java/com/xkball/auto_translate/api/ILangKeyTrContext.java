package com.xkball.auto_translate.api;

import com.xkball.auto_translate.llm.ILLMHandler;
import com.xkball.auto_translate.llm.LLMRequest;

import java.util.Map;

public interface ILangKeyTrContext extends ILLMHandler {
    
    Map<String, String> getRawMap();
    
    Map<String, String> getResultMap();
    
    LLMRequest createLLMRequest();
    
    default boolean fallback(){
        return false;
    }
}
