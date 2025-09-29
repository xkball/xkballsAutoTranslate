package com.xkball.auto_translate.event;

import com.xkball.auto_translate.api.ITranslatableFinder;
import com.xkball.auto_translate.client.ItemStackTooltipTranslator;
import com.xkball.auto_translate.crossmod.CrossModBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber()
public class XATGatherTranslateInputEvent extends Event {
    
    public final boolean force;
    
    public XATGatherTranslateInputEvent(boolean force) {
        this.force = force;
    }
    
    @SubscribeEvent
    public static void defaultHandler(XATGatherTranslateInputEvent event) {
        var screen = Minecraft.getInstance().screen;
        var force = event.force;
        if(screen instanceof ITranslatableFinder trf){
            trf.submit(force);
        }
        if(screen instanceof AbstractContainerScreen<?> acs && acs.hoveredSlot != null){
            ItemStackTooltipTranslator.submit(acs.hoveredSlot.getItem(),force);
        }
        ItemStackTooltipTranslator.submit(CrossModBridge.getHoverItemOnJEIOverlay(),force);
        CrossModBridge.tryTranslateFTBQuest(force);
    }
}
