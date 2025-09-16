package com.xkball.auto_translate.client.gui.frame.core.render;


import com.xkball.auto_translate.client.gui.frame.core.WidgetBoundary;
import com.xkball.auto_translate.utils.VanillaUtils;
import net.minecraft.client.gui.GuiGraphics;

public class SimpleBackgroundRenderer implements IGUIDecoRenderer {
    
    public static SimpleBackgroundRenderer GRAY = new SimpleBackgroundRenderer(VanillaUtils.GUI_GRAY);
    private final int bgColor;
    
    public SimpleBackgroundRenderer(int bgColor) {
        this.bgColor = bgColor;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, WidgetBoundary boundary, int mouseX, int mouseY, float partialTick) {
        var outer = boundary.outer();
        guiGraphics.fill(outer.x(), outer.y(), outer.maxX(), outer.maxY() + 1, bgColor);
    }
}
