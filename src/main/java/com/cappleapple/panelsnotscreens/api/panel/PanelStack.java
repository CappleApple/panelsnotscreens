package com.cappleapple.panelsnotscreens.api.panel;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;

/** Shared render order for every panel loaded from this library, including panels owned by different mods. */
final class PanelStack {
    private static final int BASE_Z = 300;
    private static final int VANILLA_TOOLTIP_Z = 400;
    // Tooltip Overhaul reaches Z 2300 and some icon/effect layers apply that depth a second time.
    private static final int TOOLTIP_OVERHAUL_Z = 4700;
    private static final int DEFAULT_LAYER_SPACING = 500;
    private static final int TOOLTIP_OVERHAUL_LAYER_SPACING = 4800;
    private static final int DEFAULT_MAX_BASE_Z = 9000;
    static final int FLOATING_ITEM_DECORATION_Z = 200;
    private static final String TOOLTIP_OVERHAUL_MOD_ID = "tooltipoverhaul";
    private static final ArrayList<WeakReference<Panel>> PANELS = new ArrayList<>();
    private static final WeakHashMap<Screen, DeferredTooltipOwner> DEFERRED_TOOLTIP_OWNERS = new WeakHashMap<>();
    private static final ThreadLocal<Panel> RENDERING_PANEL = new ThreadLocal<>();
    private static final ThreadLocal<TooltipCandidate> TOOLTIP_CANDIDATE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> RENDER_COLLECTION_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final Field DEFERRED_TOOLTIP_FIELD = findDeferredTooltipField();

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

    static synchronized int tooltipZ(Panel panel) {
        return z(panel) + tooltipDepth();
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
        int spacing = Math.min(preferredLayerSpacing(), (maxBaseZ() - BASE_Z) / gaps);
        return BASE_Z + index * Math.max(1, spacing);
    }

    private static int preferredLayerSpacing() {
        return hasTooltipOverhaul() ? TOOLTIP_OVERHAUL_LAYER_SPACING : DEFAULT_LAYER_SPACING;
    }

    private static int tooltipDepth() {
        return hasTooltipOverhaul() ? TOOLTIP_OVERHAUL_Z : VANILLA_TOOLTIP_Z;
    }

    static int maxBaseZ() {
        return hasTooltipOverhaul()
                ? (int) GuiGraphics.MAX_GUI_Z - TOOLTIP_OVERHAUL_Z
                : DEFAULT_MAX_BASE_Z;
    }

    static float floatingItemZ() {
        return GuiGraphics.MAX_GUI_Z - FLOATING_ITEM_DECORATION_Z;
    }

    private static boolean hasTooltipOverhaul() {
        return ModList.get().isLoaded(TOOLTIP_OVERHAUL_MOD_ID);
    }

    static void beginRender(Panel panel) {
        beginRenderCollection();
        RENDERING_PANEL.set(panel);
    }

    static void endRender(Panel panel) {
        if (RENDERING_PANEL.get() == panel) RENDERING_PANEL.remove();
        endRenderCollection();
    }

    static void beginRenderCollection() {
        int depth = RENDER_COLLECTION_DEPTH.get();
        if (depth == 0) TOOLTIP_CANDIDATE.remove();
        RENDER_COLLECTION_DEPTH.set(depth + 1);
    }

    static void endRenderCollection() {
        int depth = RENDER_COLLECTION_DEPTH.get() - 1;
        if (depth <= 0) RENDER_COLLECTION_DEPTH.remove();
        else RENDER_COLLECTION_DEPTH.set(depth);
    }

    static void captureTooltipCandidate(
            Panel panel, Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (panel.ownsPoint(screen, mouseX, mouseY)) {
            TOOLTIP_CANDIDATE.set(new TooltipCandidate(panel, screen, graphics, mouseX, mouseY));
        }
    }

    static Panel immediateTooltipOwner(
            Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        TooltipCandidate candidate = TOOLTIP_CANDIDATE.get();
        TOOLTIP_CANDIDATE.remove();
        if (candidate == null
                || candidate.screen() != screen
                || candidate.graphics() != graphics
                || candidate.mouseX() != mouseX
                || candidate.mouseY() != mouseY) {
            return null;
        }
        return candidate.panel();
    }

    static Object deferredTooltip(Screen screen) {
        if (DEFERRED_TOOLTIP_FIELD == null) return null;
        try {
            return DEFERRED_TOOLTIP_FIELD.get(screen);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    static synchronized void captureDeferredTooltipOwner(Panel panel, Screen screen, Object tooltipBeforeRender) {
        Object tooltipAfterRender = deferredTooltip(screen);
        if (tooltipAfterRender == null) {
            DEFERRED_TOOLTIP_OWNERS.remove(screen);
        } else if (tooltipAfterRender != tooltipBeforeRender) {
            DEFERRED_TOOLTIP_OWNERS.put(
                    screen, new DeferredTooltipOwner(new WeakReference<>(panel), tooltipAfterRender));
        }
    }

    static synchronized Panel deferredTooltipOwner(Screen screen) {
        DeferredTooltipOwner owner = DEFERRED_TOOLTIP_OWNERS.get(screen);
        if (owner == null || owner.tooltip() != deferredTooltip(screen)) return null;
        return owner.panel().get();
    }

    static boolean beginTooltipRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (screen == null || RENDERING_PANEL.get() != null) return false;
        Panel owner = deferredTooltipOwner(screen);
        synchronized (PanelStack.class) {
            DEFERRED_TOOLTIP_OWNERS.remove(screen);
        }
        if (owner == null) {
            owner = immediateTooltipOwner(screen, graphics, mouseX, mouseY);
        } else {
            TOOLTIP_CANDIDATE.remove();
        }
        if (owner == null) return false;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, z(owner));
        return true;
    }

    static void endTooltipRender(GuiGraphics graphics, boolean adjusted) {
        if (adjusted) graphics.pose().popPose();
    }

    private static Field findDeferredTooltipField() {
        try {
            Field field = Screen.class.getDeclaredField("deferredTooltipRendering");
            return field.trySetAccessible() ? field : null;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private record DeferredTooltipOwner(WeakReference<Panel> panel, Object tooltip) { }

    private record TooltipCandidate(
            Panel panel, Screen screen, GuiGraphics graphics, int mouseX, int mouseY) { }
}
