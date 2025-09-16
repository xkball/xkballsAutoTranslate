package com.xkball.auto_translate.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xkball.auto_translate.api.IExtendedGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.LinkedList;

@Mixin(GuiGraphics.class)
public class MixinGuiGraphics implements IExtendedGuiGraphics {
    
    @Unique
    private final LinkedList<Vector2i> xkball_sAutoTranslate$offsetStack = new LinkedList<>();

    @WrapMethod(method = "containsPointInScissor")
    public boolean wrapContainsPointInScissor(int x, int y, Operation<Boolean> original){
        if(!xkball_sAutoTranslate$offsetStack.isEmpty()){
            var p = xkball_sAutoTranslate$offsetStack.peek();
            x -= p.x;
            y -= p.y;
        }
        return original.call(x, y);
    }
    
    @Override
    public void xat_pushOffset(int x, int y) {
        xkball_sAutoTranslate$offsetStack.push(new Vector2i(x, y));
    }
    
    @Override
    public void xat_popOffset() {
        xkball_sAutoTranslate$offsetStack.pop();
    }
}
