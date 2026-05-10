package com.xkball.auto_translate.utils.translate;

import com.xkball.auto_translate.XATConfig;
import com.xkball.auto_translate.api.ITranslator;

public enum TranslatorType {
    GOOGLE(GoogleTranslate.INSTANCE),
    LLM(LLMTranslate.INSTANCE),
    DEFAULT(DefaultTranslate.INSTANCE);
    
    private final ITranslator translator;
    
    TranslatorType(ITranslator translator) {
        this.translator = translator;
    }
    
    public ITranslator getTranslator() {
        return translator;
    }
    
    public static ITranslator getCurrentTranslator() {
        return XATConfig.TRANSLATOR_TYPE.getTranslator();
    }
}
