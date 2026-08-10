package com.cappleapple.panelsnotscreens.api.panel;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Consumer-owned panel contents. Coordinates are normal absolute GUI coordinates; use
 * {@link PanelContext#layout()} to resolve the clipped content area.
 */
public interface PanelContent {
    PanelContent EMPTY = new PanelContent() { };

    default void render(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY) { }

    /** Renders consumer-specific handle decoration after the configured handle renderer. */
    default void renderHandle(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY) { }

    default boolean mouseClicked(PanelContext context, double mouseX, double mouseY, int button) { return false; }

    default boolean mouseReleased(PanelContext context, double mouseX, double mouseY, int button) { return false; }

    default boolean mouseScrolled(PanelContext context, double mouseX, double mouseY, double amount) { return false; }

    default boolean keyPressed(PanelContext context, int keyCode, int scanCode, int modifiers) { return false; }

    default boolean keyReleased(PanelContext context, int keyCode, int scanCode, int modifiers) { return false; }

    default boolean characterTyped(PanelContext context, char codePoint, int modifiers) { return false; }
}
