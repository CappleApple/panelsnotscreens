package com.cappleapple.panelsnotscreens.mixin;

import com.cappleapple.panelsnotscreens.api.panel.PanelForegroundRenderHooks;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @ModifyArg(
            method = "renderFloatingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 2)
    private float panelsnotscreens$renderFloatingItemInFront(float originalZ) {
        return PanelForegroundRenderHooks.floatingItemZ();
    }
}
