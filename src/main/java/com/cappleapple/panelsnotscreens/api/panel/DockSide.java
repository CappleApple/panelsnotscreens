package com.cappleapple.panelsnotscreens.api.panel;

/** Side of the handle from which an expanded panel grows. */
public enum DockSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }
}
