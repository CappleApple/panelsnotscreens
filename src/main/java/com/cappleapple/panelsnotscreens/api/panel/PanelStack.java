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
    // Vanilla starts tooltips at Z 400; bundle item decorations can add another 200.
    private static final int VANILLA_TOOLTIP_DEPTH = 600;
    // Tooltip Overhaul reaches Z 2300 and some icon/effect layers apply that depth a second time.
    private static final int TOOLTIP_OVERHAUL_DEPTH = 4700;
    private static final int VANILLA_LAYER_SPACING = 700;
    private static final int TOOLTIP_OVERHAUL_LAYER_SPACING = 4800;
    private static final int PANEL_CONTENT_DEPTH = 200;
    static final int FLOATING_ITEM_DECORATION_Z = 200;
    static final int GUI_DEPTH_HEADROOM = 1;
    private static final String TOOLTIP_OVERHAUL_MOD_ID = "tooltipoverhaul";
    private static final ArrayList<WeakReference<Panel>> PANELS = new ArrayList<>();
    private static final WeakHashMap<Panel, WeakReference<Screen>> ACTIVE_SCREENS = new WeakHashMap<>();
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

    static synchronized void activate(Panel panel, Screen screen) {
        ACTIVE_SCREENS.put(panel, new WeakReference<>(screen));
    }

    static synchronized Panel topmostAt(Screen screen, double mouseX, double mouseY) {
        compact();
        for (int index = PANELS.size() - 1; index >= 0; index--) {
            Panel panel = PANELS.get(index).get();
            if (panel != null && isActiveOn(panel, screen) && panel.ownsPoint(screen, mouseX, mouseY)) {
                return panel;
            }
        }
        return null;
    }

    static boolean canReceivePointer(Panel panel, Screen screen, double mouseX, double mouseY) {
        Panel topmost = topmostAt(screen, mouseX, mouseY);
        return topmost == null || topmost == panel;
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

    private static boolean isActiveOn(Panel panel, Screen screen) {
        WeakReference<Screen> activeScreen = ACTIVE_SCREENS.get(panel);
        return activeScreen != null && activeScreen.get() == screen;
    }

    private static int zForIndex(int index) {
        int gaps = Math.max(1, PANELS.size() - 1);
        int spacing = Math.min(preferredLayerSpacing(), (maxBaseZ() - BASE_Z) / gaps);
        return BASE_Z + index * Math.max(1, spacing);
    }

    private static int preferredLayerSpacing() {
        return hasTooltipOverhaul() ? TOOLTIP_OVERHAUL_LAYER_SPACING : VANILLA_LAYER_SPACING;
    }

    private static int tooltipDepth() {
        return hasTooltipOverhaul() ? TOOLTIP_OVERHAUL_DEPTH : VANILLA_TOOLTIP_DEPTH;
    }

    static int maxBaseZ() {
        return (int) GuiGraphics.MAX_GUI_Z - tooltipDepth() - PANEL_CONTENT_DEPTH;
    }

    static int tooltipForegroundBaseZ() {
        return maxBaseZ() + PANEL_CONTENT_DEPTH;
    }

    static int tooltipMaxZ() {
        return tooltipForegroundBaseZ() + tooltipDepth();
    }

    static float tooltipForegroundOffset(float currentZ) {
        return tooltipForegroundBaseZ() - currentZ;
    }

    static synchronized float panelTooltipScale(Panel owner, Screen screen) {
        int ownerZ = z(owner);
        int ownerIndex = -1;
        for (int index = 0; index < PANELS.size(); index++) {
            if (PANELS.get(index).get() == owner) {
                ownerIndex = index;
                break;
            }
        }
        if (ownerIndex < 0) return 1.0F;
        for (int index = ownerIndex + 1; index < PANELS.size(); index++) {
            Panel next = PANELS.get(index).get();
            if (next == null || !isActiveOn(next, screen)) continue;
            int availableDepth = z(next) - ownerZ - 1;
            return Math.min(1.0F, Math.max(0.0F, (float) availableDepth / tooltipDepth()));
        }
        return 1.0F;
    }

    static float panelTooltipMaxZ(Panel owner, Screen screen) {
        return z(owner) + tooltipDepth() * panelTooltipScale(owner, screen);
    }

    static float floatingItemZ() {
        return GuiGraphics.MAX_GUI_Z - FLOATING_ITEM_DECORATION_Z - GUI_DEPTH_HEADROOM;
    }

    private static boolean hasTooltipOverhaul() {
        return ModList.get().isLoaded(TOOLTIP_OVERHAUL_MOD_ID);
    }

    static void beginRender(Panel panel, Screen screen) {
        beginRenderCollection();
        activate(panel, screen);
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
        if (!panel.ownsPoint(screen, mouseX, mouseY)) return;
        TooltipCandidate current = TOOLTIP_CANDIDATE.get();
        if (current != null
                && current.screen() == screen
                && current.graphics() == graphics
                && current.mouseX() == mouseX
                && current.mouseY() == mouseY
                && z(current.panel()) >= z(panel)) return;
        TOOLTIP_CANDIDATE.set(new TooltipCandidate(panel, screen, graphics, mouseX, mouseY));
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

    static PanelTooltipRenderHooks.Result beginTooltipRender(
            Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (screen == null) return PanelTooltipRenderHooks.Result.UNCHANGED;
        Panel owner = RENDERING_PANEL.get();
        if (owner == null) {
            owner = deferredTooltipOwner(screen);
            synchronized (PanelStack.class) {
                DEFERRED_TOOLTIP_OWNERS.remove(screen);
            }
            if (owner == null) {
                owner = immediateTooltipOwner(screen, graphics, mouseX, mouseY);
            } else {
                TOOLTIP_CANDIDATE.remove();
            }
        }

        Panel topmost = topmostAt(screen, mouseX, mouseY);
        if (owner == null) {
            if (topmost != null) return PanelTooltipRenderHooks.Result.SKIP;
            return adjustTooltip(graphics, tooltipForegroundBaseZ(), 1.0F);
        }
        if (!isActiveOn(owner, screen) || (topmost != null && topmost != owner)) {
            return PanelTooltipRenderHooks.Result.SKIP;
        }
        float scale = panelTooltipScale(owner, screen);
        if (scale <= 0.0F) return PanelTooltipRenderHooks.Result.SKIP;
        return adjustTooltip(graphics, z(owner), scale);
    }

    private static PanelTooltipRenderHooks.Result adjustTooltip(
            GuiGraphics graphics, float targetBaseZ, float depthScale) {
        float currentZ = graphics.pose().last().pose().m32();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, targetBaseZ - currentZ);
        if (depthScale < 1.0F) graphics.pose().scale(1.0F, 1.0F, depthScale);
        return PanelTooltipRenderHooks.Result.ADJUSTED;
    }

    static void endTooltipRender(GuiGraphics graphics, PanelTooltipRenderHooks.Result result) {
        if (result == PanelTooltipRenderHooks.Result.ADJUSTED) graphics.pose().popPose();
    }

    static MouseInputAttempt beginMousePress(
            Screen screen, double mouseX, double mouseY) {
        return attempt(topmostAt(screen, mouseX, mouseY));
    }

    static synchronized MouseInputAttempt beginMouseRelease(Screen screen, int button) {
        compact();
        for (int index = PANELS.size() - 1; index >= 0; index--) {
            Panel panel = PANELS.get(index).get();
            if (panel != null && panel.hasPointerCapture(screen, button)) return attempt(panel);
        }
        return null;
    }

    static MouseInputAttempt beginMouseScroll(
            Screen screen, double mouseX, double mouseY) {
        return attempt(topmostAt(screen, mouseX, mouseY));
    }

    static boolean finishMousePress(
            MouseInputAttempt attempt, Screen screen, double mouseX, double mouseY, int button) {
        if (attempt == null) return false;
        if (attempt.wasHandled()) return true;
        cancelPointerCaptures();
        return attempt.panel().mouseClicked(screen, mouseX, mouseY, button);
    }

    static boolean finishMouseRelease(
            MouseInputAttempt attempt, Screen screen, double mouseX, double mouseY, int button) {
        if (attempt == null) return false;
        return attempt.wasHandled() || attempt.panel().mouseReleased(screen, mouseX, mouseY, button);
    }

    static boolean finishMouseScroll(
            MouseInputAttempt attempt, Screen screen, double mouseX, double mouseY, double amount) {
        if (attempt == null) return false;
        if (attempt.wasHandled() || attempt.panel().mouseScrolled(screen, mouseX, mouseY, amount)) return true;
        if (!attempt.panel().ownsPoint(screen, mouseX, mouseY)) return false;
        attempt.panel().bringToFront();
        return true;
    }

    private static MouseInputAttempt attempt(Panel panel) {
        return panel == null ? null : new MouseInputAttempt(panel, panel.mouseInputRevision());
    }

    private static synchronized void cancelPointerCaptures() {
        compact();
        for (WeakReference<Panel> reference : PANELS) {
            Panel panel = reference.get();
            if (panel != null) panel.cancelPointerCapture();
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

    record MouseInputAttempt(Panel panel, long revision) {
        boolean wasHandled() {
            return panel.mouseInputRevision() != revision;
        }
    }
}
