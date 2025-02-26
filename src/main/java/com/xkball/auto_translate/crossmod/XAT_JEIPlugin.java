package com.xkball.auto_translate.crossmod;

import com.xkball.auto_translate.utils.VanillaUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class XAT_JEIPlugin implements IModPlugin {
    
    @Nullable
    public static IJeiRuntime runtime;
    
    @Override
    public ResourceLocation getPluginUid() {
        return VanillaUtils.modRL("jei_plugin");
    }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
    
    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
