package com.xkball.auto_translate.utils.translate;

import com.mojang.logging.LogUtils;
import com.xkball.auto_translate.AutoTranslate;
import com.xkball.auto_translate.api.ILangKeyTrContext;
import com.xkball.auto_translate.client.gui.frame.core.IPanel;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangKeyTranslateUnit {
    public static final TranslationCacheSlice I18N_KEYS = XATDataBase.INSTANCE.createSlice("i18n_keys");
    
    public static final String DEFAULT_OPENAI_MULTIPLE_PROMPT = """
            You will be given a YAML formatted input containing entries with "id" and "text" fields. Here is the input:
            
            <yaml>
            ${yaml}
            </yaml>
            
            For each entry in the YAML, translate the contents of the "text" field into ${targetLanguage}. Write the translation back into the "text" field for that entry.
            
            Here is an example of the expected format:
            
            <example>
            Input:
              - id: 1
                text: Source
            Output:
              - id: 1
                text: Translation
            </example>
            
            Please return the translated YAML directly without wrapping <yaml> tag or include any additional information.""";
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public final List<ILangKeyTrContext> contexts = new ArrayList<>();
    public final List<ILangKeyTrContext> contextFinished = new ArrayList<>();
    public final List<ILangKeyTrContext> contextError = new ArrayList<>();
    public boolean finished = true;
    
    public LangKeyTranslateUnit() {
    
    }
    
    public void submitRequest(List<Map.Entry<String, String>> elt){
        this.contexts.add(new GroupLangKeyTranslateContext(elt,this));
    }
    
    public void submitRequest(String key, String value){
        this.contexts.add(new SingleLangKeyTranslateContext(this,key,value));
    }
    
    public synchronized void checkFinish() {
        var contextSize = contexts.size();
        LOGGER.debug("Translating: {}/{}", contextFinished.size(), contextSize);
        IPanel.GLOBAL_UPDATE_MARKER.setNeedUpdate();
        if (contextFinished.size() == contextSize && this.resolveFailedRequest()) {
            finished = true;
            AutoTranslate.injectLanguage();
        }
    }
    
    public synchronized boolean resolveFailedRequest() {
        var flag = true;
        var newContext = new ArrayList<ILangKeyTrContext>();
        var iter = this.contextError.iterator();
        while (iter.hasNext()) {
            var failedReq = iter.next();
            if(!failedReq.fallback()){
                flag = false;
                iter.remove();
                var map = failedReq.getRawMap();
                for(var entry : map.entrySet()){
                    var req = new SingleLangKeyTranslateContext(this,entry.getKey(),entry.getValue(),true);
                    this.contexts.add(req);
                    newContext.add(req);
                }
            }
        }
        this.sendRequest(newContext);
        return flag;
    }
    
    private void sendRequest(List<ILangKeyTrContext> lt){
        if(lt.isEmpty()) return;
        var llmClient = LLMTranslate.createLLMClient();
        for(var lktc : lt) {
            var request = lktc.createLLMRequest();
            llmClient.addRequest(request,lktc);
        }
        llmClient.send();
    }
    
    public synchronized void start() {
        this.finished = false;
        this.sendRequest(this.contexts);
    }
    
    public synchronized void cancel() {
        this.finished = true;
    }
    
    public synchronized void reset() {
        LOGGER.warn("Failed request in last round: {}", contextError);
        this.finished = true;
        this.contextFinished.clear();
        this.contextError.clear();
        this.contexts.clear();
    }
    
    public synchronized void submitResultFinished(ILangKeyTrContext context) {
//        LOGGER.debug("Context result : {} {} {}",context.keys,context.values,context.result);
        if (finished) return;
        this.contextFinished.add(context);
        for(var entry : context.getResultMap().entrySet()) {
            I18N_KEYS.put(entry.getKey(), entry.getValue());
        }
        this.checkFinish();
    }
    
    public synchronized void submitResultError(ILangKeyTrContext context) {
        LOGGER.warn("Failed request : {}", context);
        if (finished) return;
        this.contextFinished.add(context);
        this.contextError.add(context);
        this.checkFinish();
    }
    
    public int size(){
        return contexts.size();
    }
    
    public int normalFinishedSize(){
        return contextFinished.size() - contextError.size();
    }
    
    public int errorSize(){
        return contextError.size();
    }
}
