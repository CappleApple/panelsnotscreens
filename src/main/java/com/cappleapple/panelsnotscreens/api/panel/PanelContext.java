package com.cappleapple.panelsnotscreens.api.panel;

import net.minecraft.client.gui.screens.Screen;

/** Per-frame context supplied to content and renderer callbacks. */
public record PanelContext(Panel panel, Screen screen, PanelLayout layout, float partialTick) {
}
