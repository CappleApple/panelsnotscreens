package com.cappleapple.panelsnotscreens.api.panel;

import org.jetbrains.annotations.ApiStatus;

/** Internal bridge used by the carried-item mixin. Not part of the supported panel API. */
@ApiStatus.Internal
public final class PanelForegroundRenderHooks {
    private PanelForegroundRenderHooks() { }

    public static float floatingItemZ() {
        return PanelStack.floatingItemZ();
    }
}
