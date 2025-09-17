package com.xkball.auto_translate.client.gui.frame.core;

public record WidgetBoundary(WidgetPos outer, WidgetPos inner) {
    public static final WidgetBoundary DEFAULT = new WidgetBoundary(new WidgetPos(0, 0, 0, 0), new WidgetPos(0, 0, 0, 0));
    
}
