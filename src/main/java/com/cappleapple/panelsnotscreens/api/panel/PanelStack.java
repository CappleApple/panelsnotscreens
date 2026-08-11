package com.cappleapple.panelsnotscreens.api.panel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

/** Shared render order for every panel loaded from this library, including panels owned by different mods. */
final class PanelStack {
    private static final int BASE_Z = 300;
    private static final int PREFERRED_LAYER_SPACING = 500;
    private static final int MAX_BASE_Z = 9000;
    private static final ArrayList<WeakReference<Panel>> PANELS = new ArrayList<>();

    private PanelStack() { }

    static synchronized void register(Panel panel) {
        Objects.requireNonNull(panel);
        remove(panel);
        PANELS.add(new WeakReference<>(panel));
    }

    static synchronized void bringToFront(Panel panel) {
        register(panel);
    }

    static synchronized int z(Panel panel) {
        compact();
        for (int index = 0; index < PANELS.size(); index++) {
            if (PANELS.get(index).get() == panel) return zForIndex(index);
        }
        PANELS.add(new WeakReference<>(panel));
        return zForIndex(PANELS.size() - 1);
    }

    private static void remove(Panel panel) {
        PANELS.removeIf(reference -> {
            Panel existing = reference.get();
            return existing == null || existing == panel;
        });
    }

    private static void compact() {
        PANELS.removeIf(reference -> reference.get() == null);
    }

    private static int zForIndex(int index) {
        int gaps = Math.max(1, PANELS.size() - 1);
        int spacing = Math.min(PREFERRED_LAYER_SPACING, (MAX_BASE_Z - BASE_Z) / gaps);
        return BASE_Z + index * Math.max(1, spacing);
    }
}
