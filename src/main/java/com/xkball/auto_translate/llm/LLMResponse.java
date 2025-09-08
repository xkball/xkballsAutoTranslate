package com.xkball.auto_translate.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record LLMResponse(@Nullable JsonObject response) {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    
    public static LLMResponse fromString(String response){
        try {
            return new LLMResponse(GSON.fromJson(response, JsonObject.class));
        } catch (Exception e){
            LOGGER.error("Fail to parse llm result: {}", response, e);
            return new LLMResponse(null);
        }
    }
    
    public String getContent(){
        if(response == null){
            return "";
        }
        var jsonArray1 = response.getAsJsonArray("choices");
        var jsonObj2 = jsonArray1.get(0).getAsJsonObject();
        var jsonObj3 = jsonObj2.getAsJsonObject("message");
        return jsonObj3.get("content").getAsString();
    }
}
