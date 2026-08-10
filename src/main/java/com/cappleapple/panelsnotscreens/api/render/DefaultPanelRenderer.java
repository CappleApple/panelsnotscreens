package com.cappleapple.panelsnotscreens.api.render;

import com.cappleapple.panelsnotscreens.api.panel.PanelBounds;
import com.cappleapple.panelsnotscreens.api.panel.PanelContext;
import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import net.minecraft.client.gui.GuiGraphics;

/** Texture-free renderer matching the gray frame and blue expanded handle of the original browser. */
public class DefaultPanelRenderer implements PanelRenderer {
    @Override
    public void renderPanel(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY) {
        PanelBounds bounds = context.layout().panel();
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 0xF0C6C6C6);
        graphics.fill(bounds.x() + 2, bounds.y() + 2,
                bounds.x() + bounds.width() - 2, bounds.y() + bounds.height() - 2, 0xF0202020);
    }

    @Override
    public void renderHandle(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY,
                             boolean hovered, boolean pressed) {
        PanelBounds bounds = context.layout().handle();
        int border = pressed ? 0xFFFFFFFF : hovered ? 0xFFF0F0F0 : 0xFFC6C6C6;
        int body = context.panel().isExpanded() ? 0xFF356DA5 : hovered ? 0xFF777777 : 0xFF555555;
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), border);
        graphics.fill(bounds.x() + 2, bounds.y() + 2,
                bounds.x() + bounds.width() - 2, bounds.y() + bounds.height() - 2, body);
    }

    @Override
    public void renderButton(PanelContext context, PanelButton button, GuiGraphics graphics,
                             int mouseX, int mouseY, boolean hovered, boolean pressed, boolean enabled) {
        PanelBounds bounds = button.bounds(context.layout());
        int color = !enabled ? 0xFF242424 : pressed ? 0xFF395579 : hovered ? 0xFF4F72A5 : 0xFF303030;
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), color);
    }
}
