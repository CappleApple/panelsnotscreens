package com.cappleapple.panelsnotscreens.api.panel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;

/** Internal bridge enforcing screen and panel tooltip ownership. Not part of the supported panel API. */
@ApiStatus.Internal
public final class PanelTooltipRenderHooks {
    private PanelTooltipRenderHooks() { }

    public static Result begin(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        return PanelStack.beginTooltipRender(screen, graphics, mouseX, mouseY);
    }

    public static void end(GuiGraphics graphics, Result result) {
        PanelStack.endTooltipRender(graphics, result);
    }

    public enum Result {
        SKIP,
        UNCHANGED,
        ADJUSTED
    }
}
