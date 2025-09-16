package com.xkball.auto_translate.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ObjectInputBox<T> extends EditBox implements Renderable {
    
    public static final Predicate<String> PASS_VALIDATOR = (str) -> true;
    
    public static final Predicate<String> LONG_VALIDATOR = (str) -> {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static final Predicate<String> TIMESTAMP_VALIDATOR = LONG_VALIDATOR.and(
            (str) -> {
                try {
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(str)), ZoneOffset.UTC);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
    );
    
    public static final Predicate<String> INT_VALIDATOR = (str) -> {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    };
    
    public static final Predicate<String> LOCAL_TIME_VALIDATOR = INT_VALIDATOR.and(
            (str) -> {
                if (str.length() != 9) return false;
                var time = Integer.parseInt(str);
                if (time < 0 || time > 240000000) return false;
                if ((time % 10_000_000) / 100_000 > 60) return false;
                return (time % 100_000) / 1000 <= 60;
            }
    );
    
    public static final Predicate<String> FLOAT_VALIDATOR = (str) -> {
        try {
            Float.parseFloat(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    };
    
    public static final Predicate<String> NORMALIZED_FLOAT_VALIDATOR = ObjectInputBox.FLOAT_VALIDATOR.and(
            (str) -> Float.parseFloat(str) >= 0.0f && Float.parseFloat(str) <= 1.0f
    );
    
    public static final Predicate<String> RGB_COLOR_VALIDATOR = (str) -> {
        try {
            VanillaUtils.parseColorHEX(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    };
    
    @SuppressWarnings("deprecation")
    public static final Predicate<String> TEXTURE_VALIDATOR = (str) -> {
        var rl = ResourceLocation.tryParse(str);
        if (rl == null) return false;
        var texture = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(rl);
        return !VanillaUtils.MISSING_TEXTURE.equals(texture.atlasLocation());
    };
    
    public static final Function<String, String> PASS_RESPONDER = str -> str;
    public static final Function<String, Integer> INT_RESPONDER = Integer::parseInt;
    public static final Function<String, Long> LONG_RESPONDER = Long::parseLong;
    public static final Function<String, Float> FLOAT_RESPONDER = Float::parseFloat;
    public static final Function<String, Integer> RGB_COLOR_RESPONDER = VanillaUtils::parseColorHEX;
    public static final Function<String, ResourceLocation> TEXTURE_RESPONDER = (str) -> Objects.requireNonNullElse(ResourceLocation.tryParse(str), VanillaUtils.MISSING_TEXTURE);
    
    protected final Predicate<String> validator;
    protected final Function<String, T> responder;
    protected boolean renderState = true;
    @Nullable
    private Consumer<ObjectInputBox<T>> onLoseFocused;
    
    public static ObjectInputBox<String> createStringInput(){
        return new ObjectInputBox<>(ObjectInputBox.PASS_VALIDATOR, ObjectInputBox.PASS_RESPONDER);
    }
    
    public ObjectInputBox(Predicate<String> validator, Function<String, T> responder) {
        this(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty(), validator, responder);
    }
    
    public ObjectInputBox(Font font, int x, int y, int width, int height, Component message, Predicate<String> validator, Function<String, T> responder) {
        super(font, x, y, width, height, message);
        this.validator = validator;
        this.responder = responder;
        this.setFocused(false);
        this.setCanLoseFocus(true);
    }
    
    @Nullable
    public T get() {
        if (validator.test(getValue())) {
            return responder.apply(getValue());
        }
        return null;
    }
    
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (onLoseFocused != null && !focused) onLoseFocused.accept(this);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isFocused() && !isMouseOver(mouseX, mouseY)) {
            setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if(!this.visible) return;
        var font = Minecraft.getInstance().font;
        var rec = guiGraphics.scissorStack.stack.isEmpty() ? null : guiGraphics.scissorStack.stack.peekLast();
        if (rec != null) {
            RenderSystem.disableScissor();
//            guiGraphics.disableScissor();
            guiGraphics.applyScissor(new ScreenRectangle(rec.position().x() - 2, rec.position().y(), rec.width() + 2, rec.height()));
        }
        if (renderState) {
            if (validator.test(getValue())) {
                guiGraphics.fill(getX() - 2, getY(), getX(), getY() + getHeight(), VanillaUtils.getColor(0, 255, 0, 255));
            } else {
                guiGraphics.fill(getX() - 2, getY(), getX(), getY() + getHeight(), VanillaUtils.getColor(255, 0, 0, 255));
            }
        }
        if (rec != null) {
//            guiGraphics.disableScissor();
            guiGraphics.applyScissor(rec);
//            guiGraphics.enableScissor(rec.position().x(), rec.position().y(), rec.position().x() + rec.width(), rec.position().y() + rec.height());
        }
        var title = this.getMessage().getString();
        if (!title.isEmpty()) {
            guiGraphics.drawString(font, title, getX() - font.width(title) - (renderState ? 12 : 2), getY() + 2, 0xFFFFFF);
        }
    }
    
    public boolean isRenderState() {
        return renderState;
    }
    
    public void setRenderState(boolean renderState) {
        this.renderState = renderState;
    }
    
    public void setOnLoseFocused(Consumer<ObjectInputBox<T>> onLoseFocused) {
        this.onLoseFocused = onLoseFocused;
    }
}
