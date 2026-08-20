package com.cappleapple.panelsnotscreens.api.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

/**
 * Consumer-owned z-ordered panel collection. Panels that handle input are automatically moved to
 * the front, so the most recently interacted-with panel is rendered on top. No global singleton is
 * required.
 */
public final class PanelManager {
    private final ArrayList<Panel> panels = new ArrayList<>();

    public void add(Panel panel) {
        Objects.requireNonNull(panel);
        if (panels.stream().anyMatch(existing -> existing.id().equals(panel.id()))) {
            throw new IllegalArgumentException("Panel ID is already registered in this manager: " + panel.id());
        }
        panels.add(panel);
    }

    public boolean remove(ResourceLocation id) { return panels.removeIf(panel -> panel.id().equals(id)); }
    public List<Panel> panels() { return Collections.unmodifiableList(panels); }

    public void bringToFront(Panel panel) {
        if (panels.remove(panel)) {
            panels.add(panel);
            panel.bringToFront();
        }
    }

    public void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PanelStack.beginRenderCollection();
        try {
            for (Panel panel : panels) PanelStack.activate(panel, screen);
            for (Panel panel : panels) panel.render(screen, graphics, mouseX, mouseY, partialTick);
        } finally {
            PanelStack.endRenderCollection();
        }
    }

    public boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.mouseClicked(screen, mouseX, mouseY, button)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(Screen screen, double mouseX, double mouseY, int button) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.mouseReleased(screen, mouseX, mouseY, button)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(Screen screen, double mouseX, double mouseY, double amount) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.mouseScrolled(screen, mouseX, mouseY, amount)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }

    public boolean keyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.keyPressed(screen, keyCode, scanCode, modifiers)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }

    public boolean keyReleased(Screen screen, int keyCode, int scanCode, int modifiers) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.keyReleased(screen, keyCode, scanCode, modifiers)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }

    public boolean characterTyped(Screen screen, char codePoint, int modifiers) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            Panel panel = panels.get(index);
            if (!panel.characterTyped(screen, codePoint, modifiers)) continue;
            bringToFront(panel);
            return true;
        }
        return false;
    }
}
