package com.xkball.auto_translate.client.gui.frame.screen;

import com.xkball.auto_translate.AutoTranslate;
import com.xkball.auto_translate.client.gui.frame.core.HorizontalAlign;
import com.xkball.auto_translate.client.gui.frame.core.IPanel;
import com.xkball.auto_translate.client.gui.frame.core.IUpdateMarker;
import com.xkball.auto_translate.client.gui.frame.core.PanelConfig;
import com.xkball.auto_translate.client.gui.frame.core.VerticalAlign;
import com.xkball.auto_translate.client.gui.frame.core.render.GuiDecorations;
import com.xkball.auto_translate.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.auto_translate.client.gui.frame.widget.BlankWidget;
import com.xkball.auto_translate.client.gui.frame.widget.Label;
import com.xkball.auto_translate.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.auto_translate.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.auto_translate.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.auto_translate.client.gui.widget.ObjectInputBox;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FrameScreen extends Screen implements IUpdateMarker {
    
    public static final float THE_SCALE = 0.3731f;
    
    protected volatile boolean needUpdate = false;
    protected final Queue<Runnable> renderTasks = new ConcurrentLinkedQueue<>();
    
    public FrameScreen(Component title) {
        super(title);
    }
    
    public void updateScreen() {
        for (var child : this.children()) {
            if (child instanceof IPanel panel) {
                panel.update(this);
            }
        }
    }
    
    @Override
    public void setNeedUpdate() {
        this.needUpdate = true;
    }
    
    @Override
    public boolean needUpdate() {
        return needUpdate;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (needUpdate) {
            needUpdate = false;
            updateScreen();
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        IPanel.DebugLine.drawAllDebugLines(guiGraphics);
        while (!renderTasks.isEmpty()) {
            renderTasks.poll().run();
        }
        if (AutoTranslate.IS_DEBUG && Minecraft.getInstance().options.renderDebug){
            guiGraphics.drawString(font,mouseX + ":" + mouseY, 10, 10, -1);
        }
    }
    
    public void submitRenderTask(Runnable runnable) {
        renderTasks.add(runnable);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public <T extends AbstractWidget & IPanel> VerticalPanel screenFrame(String titleTransKey, T content) {
        var screen = PanelConfig.of()
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(VerticalPanel.of(this));
        screen.addWidget(PanelConfig.of(1, 0.04f)
                        .align(HorizontalAlign.CENTER, VerticalAlign.CENTER)
                        .sizeLimitYMin(25)
                        .decoRenderer(SimpleBackgroundRenderer.GRAY)
                        .decoRenderer(GuiDecorations.BOTTOM_DARK_BORDER_LINE)
                        .apply(new HorizontalPanel()
                                .addWidget(PanelConfig.of()
                                        .trim()
                                        .apply(Label.of(Component.translatable(titleTransKey), 1.5f)))
                                .addWidget(
                                        PanelConfig.of()
                                                .fixHeight(20)
                                                .fixWidth(20)
                                                .tooltip("xat.gui.open_config_file")
                                                .apply(iconButton(btn -> Util.getPlatform().openFile(FMLPaths.CONFIGDIR.get().resolve("xkball_s_auto_translate-common.toml").toFile()),
                                                        ResourceLocation.withDefaultNamespace("toast/recipe_book"))))
                        ))
                .addWidget(PanelConfig.of(1, 0.92f)
                        .sizeLimitYMax(height - 40)
                        .decoRenderer(GuiDecorations.BOTTOM_DARK_BORDER_LINE)
                        .apply(AutoResizeWidgetWrapper.of(content)))
                .addWidget(PanelConfig.of(1, 0.04f)
                        .sizeLimitYMin(15)
                        .decoRenderer(SimpleBackgroundRenderer.GRAY)
                        .apply(new BlankWidget()));
        return screen;
    }
    
    public AutoResizeWidgetWrapper createEditBox(Supplier<String> valueGetter, Consumer<String> valueSetter) {
        return createEditBox(() -> new EditBox(font, 0, 0, 0, 0, Component.empty()), valueGetter, valueSetter);
    }
    
    public AutoResizeWidgetWrapper createEditBox(Supplier<? extends EditBox> editBoxSupplier, Supplier<String> valueGetter, Consumer<String> valueSetter) {
        var editBox = editBoxSupplier.get();
        setupSimpleEditBox(editBox);
        editBox.setValue(valueGetter.get());
        editBox.displayPos = 0;
        editBox.setResponder(str -> {
            valueSetter.accept(str);
            setNeedUpdate();
        });
        return new AutoResizeWidgetWrapper(editBox);
    }
    
    public <T> AutoResizeWidgetWrapper createObjInputBox(Predicate<String> validator, Function<String, T> responder, Consumer<T> valueSetter) {
        var editBox = new ObjectInputBox<>(font, 0, 0, 0, 0, Component.empty(), validator, responder);
        setupSimpleEditBox(editBox);
        editBox.setResponder(str -> {
            valueSetter.accept(editBox.get());
            setNeedUpdate();
        });
        return new AutoResizeWidgetWrapper(editBox);
    }
    
    public static void setupSimpleEditBox(EditBox editBox) {
        editBox.setMaxLength(114514);
        editBox.setCanLoseFocus(true);
        editBox.moveCursorToStart();
    }
    
    public static AutoResizeWidgetWrapper iconButton(Button.OnPress onPress, ResourceLocation sprite) {
        var btn = new ImageButton(0,0,0,0,0,0,0,sprite,16,16,onPress,Component.empty());
        return AutoResizeWidgetWrapper.of(btn);
    }
    
    public static AutoResizeWidgetWrapper createButton(String message, Runnable onPress) {
        var button = Button.builder(Component.translatable(message), btn -> onPress.run()).build();
        return AutoResizeWidgetWrapper.of(button);
    }
}
