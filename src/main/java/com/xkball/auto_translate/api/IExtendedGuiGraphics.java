package com.xkball.auto_translate.api;

public interface IExtendedGuiGraphics {
    
    void xat_pushOffset(int x, int y);
    
    void xat_popOffset();
    
    static IExtendedGuiGraphics cast(Object obj){
        return (IExtendedGuiGraphics) obj;
    }
}
