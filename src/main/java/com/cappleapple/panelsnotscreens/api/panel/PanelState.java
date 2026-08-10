package com.cappleapple.panelsnotscreens.api.panel;

/** Serializable consumer-owned panel preference state. */
public record PanelState(int handleX, int handleY, DockSide dockSide, boolean expanded, boolean visible) {
    public PanelState {
        if (dockSide == null) throw new IllegalArgumentException("dockSide is required");
    }
}
