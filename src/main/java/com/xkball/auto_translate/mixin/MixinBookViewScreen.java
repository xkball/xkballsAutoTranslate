package com.xkball.auto_translate.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.screens.inventory.BookViewScreen.BOOK_LOCATION;

@Mixin(BookViewScreen.class)
public class MixinBookViewScreen extends Screen {
    
    
    protected MixinBookViewScreen(Component title) {
        super(title);
    }
    
    @Inject(method = "renderBackground",at = @At("RETURN"))
    public void onRenderBg(GuiGraphics p_295678_, int p_296491_, int p_294260_, float p_294869_, CallbackInfo ci){
        p_295678_.blit(RenderType::guiTextured, BOOK_LOCATION, (this.width - 192) / 2 + 146, 2, 0.0F, 0.0F, 192, 192, 256, 256);
    }
}
