package com.cappleapple.panelsnotscreens.api.widget;

import net.minecraft.resources.ResourceLocation;

/** Resource-pack-compatible GUI sprites for all supported button states. */
public record PanelButtonTextures(
        ResourceLocation normal,
        ResourceLocation hovered,
        ResourceLocation pressed,
        ResourceLocation disabled) {
}
