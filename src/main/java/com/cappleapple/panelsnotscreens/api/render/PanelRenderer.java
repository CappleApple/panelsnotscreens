package com.cappleapple.panelsnotscreens.api.render;

import com.cappleapple.panelsnotscreens.api.panel.PanelContext;
import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import net.minecraft.client.gui.GuiGraphics;

/** Rendering strategy for panel chrome; content remains consumer-owned. */
public interface PanelRenderer {
    void renderPanel(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY);

    void renderHandle(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY,
                      boolean hovered, boolean pressed);

    void renderButton(PanelContext context, PanelButton button, GuiGraphics graphics,
                      int mouseX, int mouseY, boolean hovered, boolean pressed, boolean enabled);
}
