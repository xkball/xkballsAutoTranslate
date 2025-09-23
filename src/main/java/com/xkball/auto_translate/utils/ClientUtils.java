package com.xkball.auto_translate.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public class ClientUtils {
    
    public static ClientLanguage getClientLanguage(String lang){
        var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(lang);
        return ClientLanguage.loadFrom(Minecraft.getInstance().getResourceManager(), List.of(lang), langInfo != null && langInfo.bidirectional());
    }
    
    public static ScreenRectangle screenRectangleTransformAxisAligned(ScreenRectangle rect, Matrix4f pose) {
        if ((pose.properties() & 4) != 0) {
            return rect;
        } else {
            Vector3f vector3f = pose.transformPosition((float)rect.left(), (float)rect.top(), 0.0F, new Vector3f());
            Vector3f vector3f1 = pose.transformPosition((float)rect.right(), (float)rect.bottom(), 0.0F, new Vector3f());
            return new ScreenRectangle(Mth.floor(vector3f.x), Mth.floor(vector3f.y), Mth.floor(vector3f1.x - vector3f.x), Mth.floor(vector3f1.y - vector3f.y));
        }
    }
}
