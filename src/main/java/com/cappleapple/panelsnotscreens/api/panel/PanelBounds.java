package com.cappleapple.panelsnotscreens.api.panel;

import net.minecraft.client.renderer.Rect2i;

/** Immutable GUI-space rectangle. */
public record PanelBounds(int x, int y, int width, int height) {
    public PanelBounds {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Panel bounds cannot have negative dimensions");
    }

    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public Rect2i asRect() {
        return new Rect2i(x, y, width, height);
    }

    public PanelBounds inset(int amount) {
        int inset = Math.max(0, amount);
        return new PanelBounds(x + inset, y + inset,
                Math.max(0, width - inset * 2), Math.max(0, height - inset * 2));
    }
}
