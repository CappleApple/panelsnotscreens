package com.cappleapple.panelsnotscreens.api.panel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;

/** Internal bridge used by the client tooltip mixin. Not part of the supported panel API. */
@ApiStatus.Internal
public final class PanelTooltipRenderHooks {
    private PanelTooltipRenderHooks() { }

    public static boolean begin(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        return PanelStack.beginTooltipRender(screen, graphics, mouseX, mouseY);
    }

    public static void end(GuiGraphics graphics, boolean adjusted) {
        PanelStack.endTooltipRender(graphics, adjusted);
    }
}
