package com.cappleapple.panelsnotscreens.api.panel;

/** Resolved handle, outer panel, and clipped content rectangles for one frame. */
public record PanelLayout(PanelBounds handle, PanelBounds panel, PanelBounds content) {
}
