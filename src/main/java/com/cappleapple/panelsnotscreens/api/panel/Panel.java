package com.cappleapple.panelsnotscreens.api.panel;

import com.cappleapple.panelsnotscreens.api.render.PanelRenderer;
import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

/**
 * Runtime panel instance. It owns positioning, drag capture, docking, clipping, attached controls,
 * and input routing while the consuming mod owns the content.
 */
public final class Panel {
    private static final double DRAG_THRESHOLD = 5.0;

    private final ResourceLocation id;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final int handleWidth;
    private final int handleHeight;
    private final int contentPadding;
    private final int panelGap;
    private final int screenMargin;
    private final boolean draggable;
    private final PanelRenderer renderer;
    private final PanelContent content;
    private final PanelStateStore stateStore;
    private final ArrayList<PanelButton> buttons;
    private int width;
    private int height;
    private int handleX;
    private int handleY;
    private int automaticDockDeadZoneX;
    private int automaticDockDeadZoneY;
    private DockSide dockSide;
    private boolean automaticDocking;
    private boolean expanded;
    private boolean visible;
    private Screen capturedScreen;
    private int capturedButton = -1;
    private boolean handleCaptured;
    private boolean contentCaptured;
    private boolean handlePressed;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private double pressX;
    private double pressY;
    private PanelButton pressedButton;

    Panel(PanelBuilder builder) {
        id = builder.id;
        minWidth = builder.minWidth;
        minHeight = builder.minHeight;
        maxWidth = builder.maxWidth;
        maxHeight = builder.maxHeight;
        handleWidth = builder.handleWidth;
        handleHeight = builder.handleHeight;
        contentPadding = builder.contentPadding;
        panelGap = builder.panelGap;
        screenMargin = builder.screenMargin;
        draggable = builder.draggable;
        renderer = builder.renderer;
        content = builder.content;
        stateStore = builder.stateStore;
        buttons = new ArrayList<>(builder.buttons);
        width = clamp(builder.width, minWidth, maxWidth);
        height = clamp(builder.height, minHeight, maxHeight);
        handleX = builder.handleX;
        handleY = builder.handleY;
        automaticDockDeadZoneX = builder.automaticDockDeadZoneX;
        automaticDockDeadZoneY = builder.automaticDockDeadZoneY;
        dockSide = builder.dockSide;
        automaticDocking = builder.automaticDocking;
        expanded = builder.expanded;
        visible = builder.visible;
        stateStore.load(id).ifPresent(this::restore);
        PanelStack.register(this);
    }

    public ResourceLocation id() { return id; }
    public int handleX() { return handleX; }
    public int handleY() { return handleY; }
    public int width() { return width; }
    public int height() { return height; }
    public DockSide dockSide() { return dockSide; }
    public boolean isAutomaticDocking() { return automaticDocking; }
    public boolean isExpanded() { return expanded; }
    public boolean isVisible() { return visible; }
    public boolean isDragging() { return dragging; }
    public List<PanelButton> buttons() { return Collections.unmodifiableList(buttons); }

    public void open() { setExpanded(true); }
    public void close() { setExpanded(false); }
    public void toggle() { setExpanded(!expanded); }
    public void show() { visible = true; persist(); }
    public void hide() { visible = false; clearPointerCapture(); persist(); }
    public void setVisible(boolean value) { if (value) show(); else hide(); }
    public void setExpanded(boolean value) { expanded = value; persist(); }
    public void setPanelSize(int newWidth, int newHeight) {
        width = clamp(newWidth, minWidth, maxWidth);
        height = clamp(newHeight, minHeight, maxHeight);
    }
    public void setHandlePosition(int x, int y, int screenWidth, int screenHeight) {
        handleX = x; handleY = y; constrainHandle(screenWidth, screenHeight); persist();
    }
    public void offsetHandle(int x, int y, int screenWidth, int screenHeight) {
        setHandlePosition(handleX + x, handleY + y, screenWidth, screenHeight);
    }
    public void setDockSide(DockSide value) { dockSide = Objects.requireNonNull(value); persist(); }
    public void setAutomaticDocking(boolean value) { automaticDocking = value; persist(); }
    public void setAutomaticDockDeadZone(int x, int y) {
        automaticDockDeadZoneX = Math.max(0, x); automaticDockDeadZoneY = Math.max(0, y);
    }
    public void addButton(PanelButton button) { buttons.add(Objects.requireNonNull(button)); }
    public boolean removeButton(ResourceLocation buttonId) { return buttons.removeIf(button -> button.id().equals(buttonId)); }
    public void cancelPointerCapture() { clearPointerCapture(); }
    /** Moves this panel above every other panel using this library, including panels from other mods. */
    public void bringToFront() { PanelStack.bringToFront(this); }

    public PanelState state() { return new PanelState(handleX, handleY, dockSide, expanded, visible); }

    public void restore(PanelState state) {
        Objects.requireNonNull(state);
        handleX = state.handleX();
        handleY = state.handleY();
        dockSide = state.dockSide();
        expanded = state.expanded();
        visible = state.visible();
    }

    public PanelLayout layout(int screenWidth, int screenHeight) {
        constrainHandle(screenWidth, screenHeight);
        PanelBounds handle = new PanelBounds(handleX, handleY, handleWidth, handleHeight);
        int panelX = switch (dockSide) {
            case LEFT -> handleX - panelGap - width;
            case RIGHT -> handleX + handleWidth + panelGap;
            case TOP, BOTTOM -> handleX + (handleWidth - width) / 2;
        };
        int panelY = switch (dockSide) {
            case TOP -> handleY - panelGap - height;
            case BOTTOM -> handleY + handleHeight + panelGap;
            case LEFT, RIGHT -> handleY + (handleHeight - height) / 2;
        };
        panelX = clamp(panelX, screenMargin, Math.max(screenMargin, screenWidth - screenMargin - width));
        panelY = clamp(panelY, screenMargin, Math.max(screenMargin, screenHeight - screenMargin - height));
        PanelBounds panel = new PanelBounds(panelX, panelY, width, height);
        return new PanelLayout(handle, panel, panel.inset(contentPadding));
    }

    public void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        updateDrag(screen, mouseX, mouseY);
        PanelContext context = context(screen, partialTick);
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, PanelStack.z(this));
        try {
            if (expanded) {
                renderer.renderPanel(context, graphics, mouseX, mouseY);
                PanelBounds clip = context.layout().content();
                graphics.enableScissor(clip.x(), clip.y(), clip.x() + clip.width(), clip.y() + clip.height());
                try {
                    content.render(context, graphics, mouseX, mouseY);
                } finally {
                    graphics.disableScissor();
                }
                for (PanelButton button : visibleButtons()) {
                    PanelBounds bounds = button.bounds(context.layout());
                    boolean hovered = bounds.contains(mouseX, mouseY);
                    renderer.renderButton(context, button, graphics, mouseX, mouseY,
                            hovered, button == pressedButton, button.isEnabled());
                    if (hovered && button.tooltip() != null && !button.tooltip().getString().isEmpty()) {
                        graphics.renderTooltip(Minecraft.getInstance().font, button.tooltip(), mouseX, mouseY);
                    }
                }
            }
            PanelBounds handle = context.layout().handle();
            renderer.renderHandle(context, graphics, mouseX, mouseY,
                    handle.contains(mouseX, mouseY), handlePressed);
            content.renderHandle(context, graphics, mouseX, mouseY);
        } finally {
            graphics.pose().popPose();
        }
    }

    public boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        if (!visible) return false;
        PanelContext context = context(screen, 0);
        if (context.layout().handle().contains(mouseX, mouseY)) {
            bringToFront();
            capturedScreen = screen;
            capturedButton = button;
            handleCaptured = true;
            if (button == 0 && draggable) {
                handlePressed = true;
                dragOffsetX = mouseX - handleX;
                dragOffsetY = mouseY - handleY;
                pressX = mouseX;
                pressY = mouseY;
            }
            return true;
        }
        if (!expanded) return false;
        for (int index = buttons.size() - 1; index >= 0; index--) {
            PanelButton candidate = buttons.get(index);
            if (!buttonParticipates(candidate) || !candidate.bounds(context.layout()).contains(mouseX, mouseY)) continue;
            bringToFront();
            capturedScreen = screen;
            capturedButton = button;
            pressedButton = candidate;
            return true;
        }
        if (!context.layout().panel().contains(mouseX, mouseY)) return false;
        bringToFront();
        capturedScreen = screen;
        capturedButton = button;
        contentCaptured = true;
        content.mouseClicked(context, mouseX, mouseY, button);
        return true;
    }

    public boolean mouseReleased(Screen screen, double mouseX, double mouseY, int button) {
        if (capturedScreen != screen || capturedButton != button) return false;
        PanelContext context = context(screen, 0);
        boolean consumed = handleCaptured || contentCaptured || pressedButton != null;
        if (handlePressed && button == 0) {
            updateDrag(screen, mouseX, mouseY);
            if (!dragging) toggle();
            else persist();
        } else if (pressedButton != null && pressedButton.bounds(context.layout()).contains(mouseX, mouseY)) {
            pressedButton.click(this, button);
        } else if (expanded) {
            content.mouseReleased(context, mouseX, mouseY, button);
        }
        clearPointerCapture();
        return consumed;
    }

    public boolean mouseScrolled(Screen screen, double mouseX, double mouseY, double amount) {
        if (!visible || !expanded) return false;
        PanelContext context = context(screen, 0);
        if (!context.layout().panel().contains(mouseX, mouseY)) return false;
        bringToFront();
        content.mouseScrolled(context, mouseX, mouseY, amount);
        return true;
    }

    public boolean keyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        if (expanded) {
            for (PanelButton button : visibleButtons()) {
                if (button.isEnabled() && button.matchesShortcut(keyCode)) {
                    bringToFront();
                    button.click(this, 0);
                    return true;
                }
            }
        }
        if (!expanded || !content.keyPressed(context(screen, 0), keyCode, scanCode, modifiers)) return false;
        bringToFront();
        return true;
    }

    public boolean keyReleased(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (!visible || !expanded || !content.keyReleased(context(screen, 0), keyCode, scanCode, modifiers)) return false;
        bringToFront();
        return true;
    }

    public boolean characterTyped(Screen screen, char codePoint, int modifiers) {
        if (!visible || !expanded || !content.characterTyped(context(screen, 0), codePoint, modifiers)) return false;
        bringToFront();
        return true;
    }

    public boolean ownsPoint(Screen screen, double mouseX, double mouseY) {
        if (!visible) return false;
        PanelLayout layout = layout(screen.width, screen.height);
        if (layout.handle().contains(mouseX, mouseY)) return true;
        if (!expanded) return false;
        if (layout.panel().contains(mouseX, mouseY)) return true;
        return visibleButtons().stream().anyMatch(button -> button.bounds(layout).contains(mouseX, mouseY));
    }

    public List<Rect2i> currentAreas(Screen screen) {
        if (!visible) return List.of();
        PanelLayout layout = layout(screen.width, screen.height);
        ArrayList<Rect2i> result = new ArrayList<>();
        result.add(layout.handle().asRect());
        if (expanded) {
            result.add(layout.panel().asRect());
            visibleButtons().stream().map(button -> button.bounds(layout).asRect()).forEach(result::add);
        }
        return List.copyOf(result);
    }

    private PanelContext context(Screen screen, float partialTick) {
        return new PanelContext(this, screen, layout(screen.width, screen.height), partialTick);
    }

    private List<PanelButton> visibleButtons() {
        return buttons.stream().filter(this::buttonParticipates).toList();
    }

    private boolean buttonParticipates(PanelButton button) {
        return button.isVisible() && (!button.expandedOnly() || expanded);
    }

    private void updateDrag(Screen screen, double mouseX, double mouseY) {
        if (!handlePressed || capturedScreen != screen) return;
        if (!dragging && !exceedsDragThreshold(pressX, pressY, mouseX, mouseY)) return;
        dragging = true;
        handleX = (int)Math.round(mouseX - dragOffsetX);
        handleY = (int)Math.round(mouseY - dragOffsetY);
        if (automaticDocking) chooseAutomaticSide(screen.width, screen.height);
        constrainHandle(screen.width, screen.height);
    }

    private void chooseAutomaticSide(int screenWidth, int screenHeight) {
        double dx = handleX + handleWidth / 2.0 - screenWidth / 2.0;
        double dy = handleY + handleHeight / 2.0 - screenHeight / 2.0;
        if (Math.abs(dx) <= automaticDockDeadZoneX && Math.abs(dy) <= automaticDockDeadZoneY) return;
        double horizontal = Math.max(0, Math.abs(dx) - automaticDockDeadZoneX);
        double vertical = Math.max(0, Math.abs(dy) - automaticDockDeadZoneY);
        dockSide = horizontal >= vertical ? (dx < 0 ? DockSide.LEFT : DockSide.RIGHT)
                : (dy < 0 ? DockSide.TOP : DockSide.BOTTOM);
    }

    private void constrainHandle(int screenWidth, int screenHeight) {
        handleX = clamp(handleX, 0, Math.max(0, screenWidth - handleWidth));
        handleY = clamp(handleY, 0, Math.max(0, screenHeight - handleHeight));
    }

    private void clearPointerCapture() {
        capturedScreen = null;
        capturedButton = -1;
        handleCaptured = false;
        contentCaptured = false;
        handlePressed = false;
        dragging = false;
        pressedButton = null;
    }

    private void persist() { stateStore.save(id, state()); }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    static boolean exceedsDragThreshold(double startX, double startY, double currentX, double currentY) {
        return Math.hypot(currentX - startX, currentY - startY) >= DRAG_THRESHOLD;
    }
}
