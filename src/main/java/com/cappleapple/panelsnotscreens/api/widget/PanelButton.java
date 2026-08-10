package com.cappleapple.panelsnotscreens.api.widget;

import com.cappleapple.panelsnotscreens.api.panel.Panel;
import com.cappleapple.panelsnotscreens.api.panel.PanelAnchor;
import com.cappleapple.panelsnotscreens.api.panel.PanelBounds;
import com.cappleapple.panelsnotscreens.api.panel.PanelLayout;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Consumer-defined action attached to a panel, including positions outside its outer bounds. */
public final class PanelButton {
    private final ResourceLocation id;
    private final PanelAnchor anchor;
    private final int offsetX;
    private final int offsetY;
    private final int width;
    private final int height;
    private final Component tooltip;
    private final PanelButtonTextures textures;
    private final BooleanSupplier visible;
    private final BooleanSupplier enabled;
    private final BiConsumer<Panel, Integer> clickHandler;
    private final IntPredicate shortcut;
    private final boolean expandedOnly;

    private PanelButton(Builder builder) {
        id = builder.id;
        anchor = builder.anchor;
        offsetX = builder.offsetX;
        offsetY = builder.offsetY;
        width = builder.width;
        height = builder.height;
        tooltip = builder.tooltip;
        textures = builder.textures;
        visible = builder.visible;
        enabled = builder.enabled;
        clickHandler = builder.clickHandler;
        shortcut = builder.shortcut;
        expandedOnly = builder.expandedOnly;
    }

    public static Builder builder(ResourceLocation id) { return new Builder(id); }
    public ResourceLocation id() { return id; }
    public Component tooltip() { return tooltip; }
    public PanelButtonTextures textures() { return textures; }
    public boolean isVisible() { return visible.getAsBoolean(); }
    public boolean isEnabled() { return enabled.getAsBoolean(); }
    public boolean expandedOnly() { return expandedOnly; }
    public boolean matchesShortcut(int keyCode) { return shortcut.test(keyCode); }

    public void click(Panel panel, int button) {
        if (isVisible() && isEnabled()) clickHandler.accept(panel, button);
    }

    public PanelBounds bounds(PanelLayout layout) {
        PanelBounds panel = layout.panel();
        int baseX;
        int baseY;
        switch (anchor) {
            case TOP_LEFT -> { baseX = panel.x(); baseY = panel.y(); }
            case TOP_RIGHT -> { baseX = panel.x() + panel.width() - width; baseY = panel.y(); }
            case BOTTOM_LEFT -> { baseX = panel.x(); baseY = panel.y() + panel.height() - height; }
            case BOTTOM_RIGHT -> { baseX = panel.x() + panel.width() - width; baseY = panel.y() + panel.height() - height; }
            case LEFT_EDGE -> { baseX = panel.x() - width; baseY = panel.y() + (panel.height() - height) / 2; }
            case RIGHT_EDGE -> { baseX = panel.x() + panel.width(); baseY = panel.y() + (panel.height() - height) / 2; }
            case HEADER -> { baseX = panel.x() + (panel.width() - width) / 2; baseY = panel.y(); }
            case FOOTER -> { baseX = panel.x() + (panel.width() - width) / 2; baseY = panel.y() + panel.height() - height; }
            case CENTER -> { baseX = panel.x() + (panel.width() - width) / 2; baseY = panel.y() + (panel.height() - height) / 2; }
            default -> throw new IllegalStateException("Unhandled anchor " + anchor);
        }
        return new PanelBounds(baseX + offsetX, baseY + offsetY, width, height);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private PanelAnchor anchor = PanelAnchor.TOP_LEFT;
        private int offsetX;
        private int offsetY;
        private int width = 18;
        private int height = 18;
        private Component tooltip = Component.empty();
        private PanelButtonTextures textures;
        private BooleanSupplier visible = () -> true;
        private BooleanSupplier enabled = () -> true;
        private BiConsumer<Panel, Integer> clickHandler = (panel, button) -> { };
        private IntPredicate shortcut = keyCode -> false;
        private boolean expandedOnly = true;

        private Builder(ResourceLocation id) { this.id = Objects.requireNonNull(id, "id"); }
        public Builder anchor(PanelAnchor value) { anchor = Objects.requireNonNull(value); return this; }
        public Builder offset(int x, int y) { offsetX = x; offsetY = y; return this; }
        public Builder size(int value) { return size(value, value); }
        public Builder size(int newWidth, int newHeight) {
            if (newWidth <= 0 || newHeight <= 0) throw new IllegalArgumentException("Button dimensions must be positive");
            width = newWidth; height = newHeight; return this;
        }
        public Builder tooltip(Component value) { tooltip = Objects.requireNonNull(value); return this; }
        public Builder textures(PanelButtonTextures value) { textures = value; return this; }
        public Builder visibleWhen(BooleanSupplier value) { visible = Objects.requireNonNull(value); return this; }
        public Builder enabledWhen(BooleanSupplier value) { enabled = Objects.requireNonNull(value); return this; }
        public Builder onClick(BiConsumer<Panel, Integer> value) { clickHandler = Objects.requireNonNull(value); return this; }
        public Builder keyboardShortcut(IntPredicate value) { shortcut = Objects.requireNonNull(value); return this; }
        public Builder expandedOnly(boolean value) { expandedOnly = value; return this; }
        public PanelButton build() { return new PanelButton(this); }
    }
}
