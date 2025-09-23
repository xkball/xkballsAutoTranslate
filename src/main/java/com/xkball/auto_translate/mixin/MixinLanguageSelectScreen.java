package com.xkball.auto_translate.mixin;

import com.xkball.auto_translate.client.gui.screen.XATConfigScreen;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 使用事件的话 OptionsSubScreen在resize会吞ScreenEvent.Init
 */
@Mixin(LanguageSelectScreen.class)
public abstract class MixinLanguageSelectScreen extends OptionsSubScreen {
    
    @Unique
    private Button xat_btn;
    
    public MixinLanguageSelectScreen(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }
    
    @Inject(method = "repositionElements",at = @At("RETURN"))
    public void onResize(CallbackInfo ci){
        this.children().stream().filter(
                w -> w instanceof Button btn && btn.getMessage().equals(CommonComponents.GUI_DONE)
        ).findFirst().ifPresent(btn -> {
            var pos = btn.getRectangle();
            if(xat_btn != null) {
                xat_btn.setRectangle(20, 20, pos.right() + 8, pos.top());
            }
            else{
                xat_btn = SpriteIconButton.builder(Component.empty(), b -> Minecraft.getInstance().setScreen(new XATConfigScreen(null, this)), true)
                        .sprite(VanillaUtils.modRL("icon/xat_icon"), 16, 16)
                        .build();
                xat_btn.setTooltip(Tooltip.create(Component.translatable("xat.gui.open_config_screen")));
                xat_btn.setRectangle(20, 20, pos.right() + 8, pos.top());
                this.addRenderableWidget(xat_btn);
            }
        });
    }
}
