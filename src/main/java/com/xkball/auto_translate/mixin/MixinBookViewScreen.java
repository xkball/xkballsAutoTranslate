package com.xkball.auto_translate.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.xkball.auto_translate.api.ITranslatableFinder;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.ClientUtils;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.client.gui.screens.inventory.BookViewScreen.BOOK_LOCATION;

@Mixin(BookViewScreen.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
@ParametersAreNonnullByDefault
public class MixinBookViewScreen extends Screen implements ITranslatableFinder {
    
    @Shadow private List<FormattedCharSequence> cachedPageComponents;
    @Unique
    private static final TranslationCacheSlice XAT_CACHE = XATDataBase.INSTANCE.createSlice("books");
    
    @Unique
    private final AtomicBoolean xat_tr = new AtomicBoolean(false);
    @Unique
    private final List<String> xat_currentPaceCache = new ArrayList<>();
    
    protected MixinBookViewScreen(Component title) {
        super(title);
    }
    
    @Inject(method = "init",at = @At("RETURN"))
    public void onInit(CallbackInfo ci){
        var xat_btn = new ImageButton(this.width / 2 + 100 + 8, 196,20, 20,
                0,0,0,VanillaUtils.modRL("icon/xat_icon"), 16, 16,
                 b -> {
                    xat_tr.set(!xat_tr.get());
                    if(xat_tr.get()) this.submit(false);
                },Component.empty());
        xat_btn.setTooltip(Tooltip.create(Component.translatable("xat.gui.toggle_translate")));
        this.addRenderableWidget(xat_btn);
    }
    
    @Inject(method = "render",at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    public void onRender(GuiGraphics p_281997_, int p_281262_, int p_283321_, float p_282251_, CallbackInfo ci){
        xat_currentPaceCache.clear();
        xat_currentPaceCache.addAll(this.cachedPageComponents.stream().map(ClientUtils::getAsString).toList());
        if(xat_tr.get()){
            this.submit(false);
        }
    }
    
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I",
            shift = At.Shift.AFTER))
    public void onRenderString(GuiGraphics guiGraphics, int p_281262_, int p_283321_, float p_282251_, CallbackInfo ci, @Local(ordinal = 2) int i, @Local(ordinal = 6) int l){
        if(xat_tr.get()){
            var str = XAT_CACHE.get(this.xat_currentPaceCache.get(l));
            if(str == null) str = I18n.get("xkball.translator.translating");
            guiGraphics.drawString(font,str,i + 146 + 36,32 + l * 9, 0, false);
        }
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        if(xat_tr.get()){
            guiGraphics.blit(BOOK_LOCATION, (this.width - 192) / 2 + 146, 2, 0.0F, 0.0F, 192, 192, 256, 256);
        }
    }
    
    @Override
    public List<String> findTranslatable(boolean force) {
        this.xat_tr.set(true);
        if(force) return this.xat_currentPaceCache;
        return this.xat_currentPaceCache.stream().filter( str -> XAT_CACHE.get(str) == null).toList();
    }
    
    @Override
    public void consumeResult(String raw, String value) {
        XAT_CACHE.put(raw, value);
    }
}
