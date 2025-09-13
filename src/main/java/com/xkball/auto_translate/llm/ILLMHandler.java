package com.xkball.auto_translate.llm;

public interface ILLMHandler {
    
    boolean handle(LLMResponse response);
    
    default void onRetriesExceeded() {}
}
