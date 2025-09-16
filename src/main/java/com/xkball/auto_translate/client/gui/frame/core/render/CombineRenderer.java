package com.xkball.auto_translate.client.gui.frame.core.render;

import com.xkball.auto_translate.client.gui.frame.core.WidgetBoundary;
import net.minecraft.client.gui.GuiGraphics;

public class CombineRenderer implements IGUIDecoRenderer {
    
    private final IGUIDecoRenderer left;
    private final IGUIDecoRenderer right;
    
    public CombineRenderer(IGUIDecoRenderer left, IGUIDecoRenderer right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, WidgetBoundary boundary, int mouseX, int mouseY, float partialTick) {
        this.left.render(guiGraphics, boundary, mouseX, mouseY, partialTick);
        this.right.render(guiGraphics, boundary, mouseX, mouseY, partialTick);
    }
}
