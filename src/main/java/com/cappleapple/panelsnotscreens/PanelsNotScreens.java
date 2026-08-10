package com.cappleapple.panelsnotscreens;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** NeoForge entry point for the client-side panel library. */
@Mod(PanelsNotScreens.MOD_ID)
public final class PanelsNotScreens {
    public static final String MOD_ID = "panelsnotscreens";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PanelsNotScreens(IEventBus ignored) {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
