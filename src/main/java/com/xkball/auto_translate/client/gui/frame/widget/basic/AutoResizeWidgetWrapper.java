package com.xkball.auto_translate.client.gui.frame.widget.basic;

import com.xkball.auto_translate.client.gui.frame.core.IPanel;
import com.xkball.auto_translate.client.gui.frame.core.WidgetBoundary;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoResizeWidgetWrapper extends BaseContainerWidget {
    
    public final AbstractWidget inner;
    private final List<? extends GuiEventListener> child;
    
    public static AutoResizeWidgetWrapper of(AbstractWidget inner) {
        return new AutoResizeWidgetWrapper(inner);
    }
    
    public AutoResizeWidgetWrapper(AbstractWidget inner) {
        super(inner.getMessage());
        this.inner = inner;
        this.child = List.of(inner);
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        inner.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        inner.updateNarration(narrationElementOutput);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && this.getBoundary().inner().inside(mouseX, mouseY);
    }
    
    @Override
    @SuppressWarnings("DuplicatedCode")
    public void resize() {
        if (inner instanceof IPanel widget) {
            var parentPos = widgetBoundary.inner();
            IPanel.calculateBoundary(widget, parentPos, parentPos.x(), parentPos.y());
            widget.resize();
        }
    }
    
    @Override
    public List<IPanel> getChildren() {
        if (inner instanceof IPanel widget) {
            return List.of(widget);
        }
        return EMPTY;
    }
    
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        inner.setFocused(focused);
        
    }
    
    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        inner.setTooltip(tooltip);
    }
    
    public AutoResizeWidgetWrapper setTooltip_(@Nullable Tooltip tooltip){
        inner.setTooltip(tooltip);
        return this;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return child;
    }
    
    @Override
    public void setBoundary(WidgetBoundary boundary) {
        this.widgetBoundary = boundary;
        this.inner.setPosition(boundary.inner().x(), boundary.inner().y());
        this.inner.setSize(boundary.inner().width(), boundary.inner().height());
        this.width = boundary.inner().width();
        this.height = boundary.inner().height();
    }
}
