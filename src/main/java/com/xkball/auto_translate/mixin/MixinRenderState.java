package com.xkball.auto_translate.mixin;

import com.xkball.auto_translate.client.FullScreenTranslateManager;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiRenderState.class)
public abstract class MixinRenderState {

    @ModifyArg(method = "addText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState$Node;addText(Lnet/minecraft/client/renderer/state/gui/GuiTextRenderState;)V"))
    private GuiTextRenderState modifyTextArg(GuiTextRenderState original) {
        if (!FullScreenTranslateManager.isEnabled()) return original;
        return FullScreenTranslateManager.resolveAndSubmit(original);
    }
}
