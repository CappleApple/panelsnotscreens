package com.cappleapple.panelsnotscreens.api.panel;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** Optional persistence boundary. The consuming mod chooses where and how state is stored. */
public interface PanelStateStore {
    PanelStateStore NONE = new PanelStateStore() {
        @Override public Optional<PanelState> load(ResourceLocation panelId) { return Optional.empty(); }
        @Override public void save(ResourceLocation panelId, PanelState state) { }
    };

    Optional<PanelState> load(ResourceLocation panelId);

    void save(ResourceLocation panelId, PanelState state);
}
