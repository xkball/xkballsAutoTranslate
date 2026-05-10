package com.xkball.auto_translate.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.xkball.auto_translate.api.ITranslatableFinder;
import com.xkball.auto_translate.data.TranslationCacheSlice;
import com.xkball.auto_translate.data.XATDataBase;
import com.xkball.auto_translate.utils.ClientUtils;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.client.gui.screens.inventory.BookViewScreen.BOOK_LOCATION;

@Mixin(BookViewScreen.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class MixinBookViewScreen extends Screen implements ITranslatableFinder {
    
    @Shadow private List<FormattedCharSequence> cachedPageComponents;
    
    @Shadow
    protected abstract int backgroundLeft();
    
    @Shadow
    protected abstract int backgroundTop();
    
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
        var xat_btn = SpriteIconButton.builder(Component.empty(), b -> {
                    xat_tr.set(!xat_tr.get());
                    if(xat_tr.get()) this.submit(false);
                }, true)
                .sprite(VanillaUtils.modRL("icon/xat_icon"), 16, 16)
                .build();
        xat_btn.setTooltip(Tooltip.create(Component.translatable("xat.gui.toggle_translate")));
        xat_btn.setRectangle(20, 20, this.width / 2 + 100 + 8, 196);
        this.addRenderableWidget(xat_btn);
    }
    
    @Inject(method = "extractRenderState",at = @At("HEAD"))
    public void onRender(GuiGraphicsExtractor p_281997_, int p_281262_, int p_283321_, float p_282251_, CallbackInfo ci){
        xat_currentPaceCache.clear();
        xat_currentPaceCache.addAll(this.cachedPageComponents.stream().map(ClientUtils::getAsString).toList());
        if(xat_tr.get()){
            this.submit(false);
        }
    }

    @Inject(method = "visitText", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(IILnet/minecraft/util/FormattedCharSequence;)V",
            shift = At.Shift.AFTER))
    public void onRenderString(ActiveTextCollector collector, boolean clickableOnly, CallbackInfo ci, @Local(ordinal = 3) int i){
        if(xat_tr.get() && i < this.xat_currentPaceCache.size()){
            int left = this.backgroundLeft();
            int top = this.backgroundTop();
            var str = XAT_CACHE.get(this.xat_currentPaceCache.get(i));
            if(str == null) str = I18n.get("xkball.translator.translating");
            collector.accept(left + 146 + 36,top + 30 + i * 9, Component.literal(str).withStyle(ChatFormatting.BLACK).withStyle(Style::withoutShadow));
        }
    }
    
    @Inject(method = "extractBackground",at = @At("RETURN"))
    public void onRenderBg(GuiGraphicsExtractor guiGraphics, int p_296491_, int p_294260_, float p_294869_, CallbackInfo ci){
        if(xat_tr.get()){
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_LOCATION, (this.width - 192) / 2 + 146, 2, 0.0F, 0.0F, 192, 192, 256, 256);
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
