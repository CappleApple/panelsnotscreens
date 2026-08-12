package com.cappleapple.panelsnotscreens.mixin;

import com.cappleapple.panelsnotscreens.api.panel.PanelTooltipRenderHooks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphics.class)
abstract class GuiGraphicsMixin {
    @WrapMethod(method = "renderTooltipInternal")
    private void panelsnotscreens$renderTooltipAtOwnerDepth(
            Font font,
            List<ClientTooltipComponent> components,
            int mouseX,
            int mouseY,
            ClientTooltipPositioner positioner,
            Operation<Void> original) {
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        boolean adjusted = PanelTooltipRenderHooks.begin(
                Minecraft.getInstance().screen, graphics, mouseX, mouseY);
        try {
            original.call(font, components, mouseX, mouseY, positioner);
        } finally {
            PanelTooltipRenderHooks.end(graphics, adjusted);
        }
    }
}
