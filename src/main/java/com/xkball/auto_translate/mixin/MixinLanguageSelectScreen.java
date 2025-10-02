package com.xkball.auto_translate.mixin;

import com.xkball.auto_translate.client.gui.screen.XATConfigScreen;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 使用事件的话 OptionsSubScreen在resize会吞ScreenEvent.Init
 */
@Mixin(LanguageSelectScreen.class)
public abstract class MixinLanguageSelectScreen extends OptionsSubScreen {
    
    public MixinLanguageSelectScreen(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }
    
    @Inject(method = "init",at = @At("RETURN"))
    public void onResize(CallbackInfo ci){
        this.children().stream().filter(
                w -> w instanceof Button btn && btn.getMessage().equals(CommonComponents.GUI_DONE)
        ).findFirst().ifPresent(btn -> {
            var pos = btn.getRectangle();
            var xat_btn = new ImageButton(pos.right() + 8, pos.top(),20, 20,0,0,0, VanillaUtils.modRL("icon/xat_icon"), 16, 16,b -> Minecraft.getInstance().setScreen(new XATConfigScreen(this)),Component.empty());
            xat_btn.setTooltip(Tooltip.create(Component.translatable("xat.gui.open_config_screen")));
            this.addRenderableWidget(xat_btn);
        
        });
    }
}
