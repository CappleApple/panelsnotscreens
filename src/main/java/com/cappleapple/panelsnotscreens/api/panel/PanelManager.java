package com.cappleapple.panelsnotscreens.api.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

/** Consumer-owned z-ordered panel collection. No global singleton is required. */
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
        if (panels.remove(panel)) panels.add(panel);
    }

    public void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (Panel panel : panels) panel.render(screen, graphics, mouseX, mouseY, partialTick);
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
            if (panels.get(index).mouseReleased(screen, mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public boolean mouseScrolled(Screen screen, double mouseX, double mouseY, double amount) {
        for (int index = panels.size() - 1; index >= 0; index--) {
            if (panels.get(index).mouseScrolled(screen, mouseX, mouseY, amount)) return true;
        }
        return false;
    }
}
