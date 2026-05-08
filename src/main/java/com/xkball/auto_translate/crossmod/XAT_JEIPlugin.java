package com.xkball.auto_translate.crossmod;

import com.xkball.auto_translate.utils.VanillaUtils;
import com.xkball.xklib.resource.ResourceLocation;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class XAT_JEIPlugin implements IModPlugin {
    
    @Nullable
    public static IJeiRuntime runtime;
    
    @Override
    public Identifier getPluginUid() {
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
