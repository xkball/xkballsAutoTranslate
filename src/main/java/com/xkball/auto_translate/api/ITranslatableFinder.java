package com.xkball.auto_translate.api;

import com.xkball.auto_translate.XATConfig;

import java.util.List;

public interface ITranslatableFinder {
    
    List<String> findTranslatable(boolean force);
    
    void consumeResult(String raw, String value);
    
    default void submit(boolean force) {
        var tr = this.findTranslatable(force);
        tr.forEach(str -> XATConfig.TRANSLATOR_TYPE.getTranslator().translate(str).whenCompleteAsync((result, t) -> this.consumeResult(str,result)));
    }
}
