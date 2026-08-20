package com.cappleapple.panelsnotscreens.client;

import com.cappleapple.panelsnotscreens.PanelsNotScreens;
import com.cappleapple.panelsnotscreens.api.panel.PanelScreenInputEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Registers client-only panel input coordination. */
@Mod(value = PanelsNotScreens.MOD_ID, dist = Dist.CLIENT)
public final class PanelsNotScreensClient {
    public PanelsNotScreensClient() {
        NeoForge.EVENT_BUS.register(PanelScreenInputEvents.class);
    }
}
