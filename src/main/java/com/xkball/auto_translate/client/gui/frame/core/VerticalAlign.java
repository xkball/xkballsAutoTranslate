package com.xkball.auto_translate.client.gui.frame.core;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum VerticalAlign implements StringRepresentable {
    TOP(Component.translatable("let_me_see_see.gui.align_top")),
    CENTER(Component.translatable("let_me_see_see.gui.align_center")),
    BOTTOM(Component.translatable("let_me_see_see.gui.align_bottom"));
    
    public static final Codec<VerticalAlign> CODEC = StringRepresentable.fromEnum(VerticalAlign::values);
    
    private static final VerticalAlign[] VALUES = values();
    public final Component displayName;
    
    VerticalAlign(Component displayName) {
        this.displayName = displayName;
    }
    
    
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
