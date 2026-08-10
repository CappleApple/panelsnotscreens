package com.cappleapple.panelsnotscreens.api.render;

import net.minecraft.resources.ResourceLocation;

/**
 * Resource-pack-compatible GUI sprite IDs for a textured panel. Null entries fall back to the
 * procedural renderer. GUI sprites may use Minecraft's scalable/nine-slice metadata.
 */
public record PanelTextureSet(
        ResourceLocation background,
        ResourceLocation frame,
        ResourceLocation handle,
        ResourceLocation expandedHandle,
        ResourceLocation handleHover,
        ResourceLocation handlePressed) {

    public static PanelTextureSet panel(ResourceLocation panel) {
        return new PanelTextureSet(panel, null, null, null, null, null);
    }
}
