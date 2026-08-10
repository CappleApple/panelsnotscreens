package com.cappleapple.panelsnotscreens.api.render;

import com.cappleapple.panelsnotscreens.api.panel.PanelBounds;
import com.cappleapple.panelsnotscreens.api.panel.PanelContext;
import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import com.cappleapple.panelsnotscreens.api.widget.PanelButtonTextures;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** GUI-sprite renderer with procedural fallbacks for every omitted texture state. */
public final class TexturedPanelRenderer extends DefaultPanelRenderer {
    private final PanelTextureSet textures;

    public TexturedPanelRenderer(PanelTextureSet textures) {
        this.textures = Objects.requireNonNull(textures, "textures");
    }

    @Override
    public void renderPanel(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!draw(graphics, textures.background(), context.layout().panel())) {
            super.renderPanel(context, graphics, mouseX, mouseY);
        }
        draw(graphics, textures.frame(), context.layout().panel());
    }

    @Override
    public void renderHandle(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY,
                             boolean hovered, boolean pressed) {
        ResourceLocation sprite = pressed && textures.handlePressed() != null ? textures.handlePressed()
                : hovered && textures.handleHover() != null ? textures.handleHover()
                : context.panel().isExpanded() && textures.expandedHandle() != null ? textures.expandedHandle()
                : textures.handle();
        if (!draw(graphics, sprite, context.layout().handle())) {
            super.renderHandle(context, graphics, mouseX, mouseY, hovered, pressed);
        }
    }

    @Override
    public void renderButton(PanelContext context, PanelButton button, GuiGraphics graphics,
                             int mouseX, int mouseY, boolean hovered, boolean pressed, boolean enabled) {
        PanelButtonTextures buttonTextures = button.textures();
        ResourceLocation sprite = null;
        if (buttonTextures != null) {
            sprite = !enabled && buttonTextures.disabled() != null ? buttonTextures.disabled()
                    : pressed && buttonTextures.pressed() != null ? buttonTextures.pressed()
                    : hovered && buttonTextures.hovered() != null ? buttonTextures.hovered()
                    : buttonTextures.normal();
        }
        if (!draw(graphics, sprite, button.bounds(context.layout()))) {
            super.renderButton(context, button, graphics, mouseX, mouseY, hovered, pressed, enabled);
        }
    }

    private static boolean draw(GuiGraphics graphics, ResourceLocation sprite, PanelBounds bounds) {
        if (sprite == null) return false;
        graphics.blitSprite(sprite, bounds.x(), bounds.y(), bounds.width(), bounds.height());
        return true;
    }
}
