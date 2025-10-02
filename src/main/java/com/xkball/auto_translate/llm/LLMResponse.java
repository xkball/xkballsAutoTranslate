package com.xkball.auto_translate.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.data.XATDataBase;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public record LLMResponse(@Nullable JsonObject response) {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    
    public static LLMResponse fromString(String response){
        LLMResponse result;
        try {
            result = new LLMResponse(GSON.fromJson(response, JsonObject.class));
        } catch (Exception e){
            LOGGER.error("Fail to parse llm result: {}", response, e);
            result = new LLMResponse(null);
        }
        XATDataBase.INSTANCE.recordTokenCost(result.getTokenUsed());
        return result;
    }
    
    public String getContent(){
        if(response == null){
            return "";
        }
        var jsonArray1 = response.getAsJsonArray("choices");
        var jsonObj2 = jsonArray1.get(0).getAsJsonObject();
        var jsonObj3 = jsonObj2.getAsJsonObject("message");
        return jsonObj3.get("content").getAsString().trim();
    }
    
    public int getTokenUsed(){
        if(response == null){
            return 0;
        }
        var jsonObj1 = response.getAsJsonObject("usage");
        return jsonObj1.get("total_tokens").getAsInt();
    }
}
