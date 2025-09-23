package com.xkball.auto_translate.utils.translate;

import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.llm.ILLMHandler;
import com.xkball.auto_translate.llm.LLMResponse;
import com.xkball.auto_translate.utils.LegacyUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangKeyTranslateContext implements ILLMHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public final LangKeyTranslateUnit unit;
    public final List<String> keys = new ArrayList<>();
    public final List<String> values = new ArrayList<>();
    public final Map<String, String> result = new HashMap<>();
    
    public LangKeyTranslateContext(List<Map.Entry<String, String>> elt, LangKeyTranslateUnit unit) {
        this.unit = unit;
        for (Map.Entry<String, String> entry : elt) {
            if (entry.getValue().isEmpty()) {
                LOGGER.debug("empty key value pair: {},{}", entry.getKey(), entry.getValue());
                continue;
            }
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }
    }
    
    public String createLLMRequestUserPrompt() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (var i = 0; i < keys.size(); i++) {
            items.add(Map.of("id", i, "text", values.get(i)));
        }
        return LegacyUtils.toYaml(items);
    }
    
    @Override
    public boolean handle(LLMResponse response) {
        LOGGER.debug(response.getContent());
        List<String> translateResult = new ArrayList<>();
        try {
            List<Map<String, Object>> translatedItems = LegacyUtils.parseYaml(response.getContent());
            for (var map : translatedItems) {
                translateResult.add((Integer) map.get("id"), (String) map.get("text"));
            }
        } catch (Exception e) {
            return false;
        }
        if (translateResult.size() != this.keys.size()) {
            return false;
        }
        for (int i = 0; i < this.keys.size(); i++) {
            result.put(this.keys.get(i), translateResult.get(i));
        }
        this.unit.submitResultFinished(this);
        return true;
    }
    
    @Override
    public void onRetriesExceeded() {
        this.unit.submitResultError(this);
    }
}
