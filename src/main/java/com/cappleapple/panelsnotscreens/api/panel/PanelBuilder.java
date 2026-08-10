package com.cappleapple.panelsnotscreens.api.panel;

import com.cappleapple.panelsnotscreens.api.render.DefaultPanelRenderer;
import com.cappleapple.panelsnotscreens.api.render.PanelRenderer;
import com.cappleapple.panelsnotscreens.api.render.PanelTextureSet;
import com.cappleapple.panelsnotscreens.api.render.TexturedPanelRenderer;
import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/** Fluent public entry point for creating an independent panel. */
public final class PanelBuilder {
    final ResourceLocation id;
    int width = 160;
    int height = 140;
    int minWidth = 32;
    int minHeight = 32;
    int maxWidth = Integer.MAX_VALUE;
    int maxHeight = Integer.MAX_VALUE;
    int handleX;
    int handleY;
    int handleWidth = 20;
    int handleHeight = 18;
    int contentPadding = 4;
    int panelGap = 2;
    int screenMargin = 2;
    int automaticDockDeadZoneX = 36;
    int automaticDockDeadZoneY = 36;
    DockSide dockSide = DockSide.RIGHT;
    boolean draggable = true;
    boolean automaticDocking = true;
    boolean expanded = true;
    boolean visible = true;
    PanelRenderer renderer = new DefaultPanelRenderer();
    PanelContent content = PanelContent.EMPTY;
    PanelStateStore stateStore = PanelStateStore.NONE;
    final List<PanelButton> buttons = new ArrayList<>();

    private PanelBuilder(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public static PanelBuilder create(ResourceLocation id) { return new PanelBuilder(id); }
    public PanelBuilder size(int newWidth, int newHeight) { width = positive(newWidth); height = positive(newHeight); return this; }
    public PanelBuilder minimumSize(int newWidth, int newHeight) { minWidth = positive(newWidth); minHeight = positive(newHeight); return this; }
    public PanelBuilder maximumSize(int newWidth, int newHeight) { maxWidth = positive(newWidth); maxHeight = positive(newHeight); return this; }
    public PanelBuilder position(int x, int y) { handleX = x; handleY = y; return this; }
    public PanelBuilder handleSize(int newWidth, int newHeight) { handleWidth = positive(newWidth); handleHeight = positive(newHeight); return this; }
    public PanelBuilder contentPadding(int value) { contentPadding = nonNegative(value); return this; }
    public PanelBuilder panelGap(int value) { panelGap = nonNegative(value); return this; }
    public PanelBuilder screenMargin(int value) { screenMargin = nonNegative(value); return this; }
    public PanelBuilder dockSide(DockSide value) { dockSide = Objects.requireNonNull(value); return this; }
    public PanelBuilder draggable(boolean value) { draggable = value; return this; }
    public PanelBuilder automaticDocking(boolean value) { automaticDocking = value; return this; }
    public PanelBuilder automaticDockDeadZone(int x, int y) {
        automaticDockDeadZoneX = nonNegative(x); automaticDockDeadZoneY = nonNegative(y); return this;
    }
    public PanelBuilder expanded(boolean value) { expanded = value; return this; }
    public PanelBuilder visible(boolean value) { visible = value; return this; }
    public PanelBuilder renderer(PanelRenderer value) { renderer = Objects.requireNonNull(value); return this; }
    public PanelBuilder proceduralStyle() { renderer = new DefaultPanelRenderer(); return this; }
    public PanelBuilder texturedStyle(PanelTextureSet textures) { renderer = new TexturedPanelRenderer(textures); return this; }
    public PanelBuilder content(PanelContent value) { content = Objects.requireNonNull(value); return this; }
    public PanelBuilder stateStore(PanelStateStore value) { stateStore = Objects.requireNonNull(value); return this; }
    public PanelBuilder addButton(PanelButton value) { buttons.add(Objects.requireNonNull(value)); return this; }

    public Panel build() {
        if (minWidth > maxWidth || minHeight > maxHeight) throw new IllegalStateException("Minimum panel size exceeds maximum size");
        return new Panel(this);
    }

    private static int positive(int value) {
        if (value <= 0) throw new IllegalArgumentException("Value must be positive");
        return value;
    }

    private static int nonNegative(int value) {
        if (value < 0) throw new IllegalArgumentException("Value cannot be negative");
        return value;
    }
}
