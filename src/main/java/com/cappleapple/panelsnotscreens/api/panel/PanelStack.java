package com.cappleapple.panelsnotscreens.api.panel;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Shared render order for every panel loaded from this library, including panels owned by different mods. */
final class PanelStack {
    private static final int BASE_Z = 300;
    private static final int PREFERRED_LAYER_SPACING = 500;
    private static final int MAX_BASE_Z = 9000;
    private static final ArrayList<WeakReference<Panel>> PANELS = new ArrayList<>();
    private static final WeakHashMap<Screen, DeferredTooltipOwner> DEFERRED_TOOLTIP_OWNERS = new WeakHashMap<>();
    private static final ThreadLocal<Panel> RENDERING_PANEL = new ThreadLocal<>();
    private static final ThreadLocal<TooltipAdjustment> TOOLTIP_ADJUSTMENT = new ThreadLocal<>();
    private static final ThreadLocal<TooltipCandidate> TOOLTIP_CANDIDATE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> RENDER_COLLECTION_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final Field DEFERRED_TOOLTIP_FIELD = findDeferredTooltipField();

    static {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PanelStack::beforeTooltip);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PanelStack::adjustTooltipDepth);
    }

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
        return z(panel) + 400;
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

    private static void beforeTooltip(RenderTooltipEvent.Pre event) {
        TOOLTIP_ADJUSTMENT.remove();
        if (RENDERING_PANEL.get() != null) return;
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null) return;
        Panel owner = deferredTooltipOwner(screen);
        synchronized (PanelStack.class) {
            DEFERRED_TOOLTIP_OWNERS.remove(screen);
        }
        if (owner == null) {
            owner = immediateTooltipOwner(screen, event.getGraphics(), event.getX(), event.getY());
        } else {
            TOOLTIP_CANDIDATE.remove();
        }
        if (owner != null) {
            TOOLTIP_ADJUSTMENT.set(new TooltipAdjustment(event.getGraphics(), z(owner)));
        }
    }

    private static void adjustTooltipDepth(RenderTooltipEvent.Color event) {
        TooltipAdjustment adjustment = TOOLTIP_ADJUSTMENT.get();
        TOOLTIP_ADJUSTMENT.remove();
        if (adjustment != null && adjustment.graphics() == event.getGraphics()) {
            event.getGraphics().pose().translate(0, 0, adjustment.z());
        }
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

    private record TooltipAdjustment(GuiGraphics graphics, int z) { }
}
