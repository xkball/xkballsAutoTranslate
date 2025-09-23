package com.xkball.auto_translate.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xkball.auto_translate.api.IExtendedGuiGraphics;
import com.xkball.auto_translate.client.gui.frame.screen.FrameScreen;
import com.xkball.auto_translate.utils.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedList;

@Mixin(GuiGraphics.class)
public class MixinGuiGraphics implements IExtendedGuiGraphics {
    
    @Shadow @Final private PoseStack pose;
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
    
    @WrapOperation(method = "enableScissor",
        at = @At(value = "NEW", target = "(IIII)Lnet/minecraft/client/gui/navigation/ScreenRectangle;"))
    public ScreenRectangle wrapNewScreenRect(int x, int y, int w, int h, Operation<ScreenRectangle> original){
        var result = original.call(x,y,w,h);
        if(!(Minecraft.getInstance().screen instanceof FrameScreen)) return result;
        result = ClientUtils.screenRectangleTransformAxisAligned(result,this.pose.last().pose());
        return result;
    }
}
