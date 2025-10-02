package com.xkball.auto_translate.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import javax.annotation.Nullable;
import java.util.Objects;

public class XATConfigUpdateEvent extends Event implements IModBusEvent {
    
    public boolean changed(){
        return true;
    }
    
    public static class Http extends XATConfigUpdateEvent {
        
        @Nullable
        public final String proxyHost;
        public final int proxyPort;
        @Nullable
        public final String oldProxyHost;
        public final int oldProxyPort;
        
        public final int maxRetries;
        public final int oldMaxRetries;
        
        public Http(@Nullable String proxyHost, int proxyPort, @Nullable String oldProxyHost, int oldProxyPort, int maxRetries, int oldMaxRetries) {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.oldProxyHost = oldProxyHost;
            this.oldProxyPort = oldProxyPort;
            this.maxRetries = maxRetries;
            this.oldMaxRetries = oldMaxRetries;
        }
        
        @Override
        public boolean changed() {
            return !Objects.equals(proxyHost, oldProxyHost) || !Objects.equals(proxyPort, oldProxyPort) || !Objects.equals(maxRetries, oldMaxRetries);
        }
    }
    
    public static class LLM extends XATConfigUpdateEvent {
        
        @Nullable
        public final String llmApiUrl;
        @Nullable
        public final String llmApiUrlOld;
        
        @Nullable
        public final String llmApiKey;
        @Nullable
        public final String llmApiKeyOld;
        
        @Nullable
        public final String llmModel;
        @Nullable
        public final String llmModelOld;
        
        @Nullable
        public final String llmSystemPrompt;
        @Nullable
        public final String llmSystemPromptOld;
        
        public final int maxRetries;
        public final int maxRetriesOld;
        
        public LLM(@Nullable String llmApiUrl, @Nullable String llmApiUrlOld, @Nullable String llmApiKey, @Nullable String llmApiKeyOld, @Nullable String llmModel, @Nullable String llmModelOld, @Nullable String llmSystemPrompt, @Nullable String llmSystemPromptOld, int maxRetries, int maxRetriesOld) {
            this.llmApiUrl = llmApiUrl;
            this.llmApiUrlOld = llmApiUrlOld;
            this.llmApiKey = llmApiKey;
            this.llmApiKeyOld = llmApiKeyOld;
            this.llmModel = llmModel;
            this.llmModelOld = llmModelOld;
            this.llmSystemPrompt = llmSystemPrompt;
            this.llmSystemPromptOld = llmSystemPromptOld;
            this.maxRetries = maxRetries;
            this.maxRetriesOld = maxRetriesOld;
        }
        
        @Override
        public boolean changed() {
            return !Objects.equals(llmApiUrl, llmApiUrlOld) || !Objects.equals(llmApiKey, llmApiKeyOld) || !Objects.equals(llmModel, llmModelOld) || !Objects.equals(llmSystemPrompt, llmSystemPromptOld) || !Objects.equals(maxRetries, maxRetriesOld);
        }
    }
}
