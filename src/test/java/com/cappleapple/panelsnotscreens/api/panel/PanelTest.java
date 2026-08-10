package com.cappleapple.panelsnotscreens.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PanelTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("example", "panel");

    @Test
    void opensClosesAndToggles() {
        Panel panel = PanelBuilder.create(ID).expanded(false).build();
        assertFalse(panel.isExpanded());
        panel.open();
        assertTrue(panel.isExpanded());
        panel.toggle();
        assertFalse(panel.isExpanded());
        panel.close();
        assertFalse(panel.isExpanded());
    }

    @Test
    void dragThresholdIgnoresClickJitter() {
        assertFalse(Panel.exceedsDragThreshold(40, 40, 43, 43));
        assertTrue(Panel.exceedsDragThreshold(40, 40, 45, 40));
    }

    @Test
    void manualDockingRemainsAuthoritativeWhenAutomaticDockingIsDisabled() {
        Panel panel = PanelBuilder.create(ID)
                .position(180, 80)
                .size(60, 40)
                .dockSide(DockSide.LEFT)
                .automaticDocking(false)
                .build();

        PanelLayout layout = panel.layout(240, 160);
        assertFalse(panel.isAutomaticDocking());
        assertEquals(DockSide.LEFT, panel.dockSide());
        assertEquals(panel.handleX() - 2 - 60, layout.panel().x());
    }

    @Test
    void attachedButtonsFollowPanelAndMaySitOutsideItsBounds() {
        PanelButton button = PanelButton.builder(ResourceLocation.fromNamespaceAndPath("example", "edge_button"))
                .anchor(PanelAnchor.RIGHT_EDGE)
                .offset(3, 0)
                .size(12, 14)
                .build();
        Panel panel = PanelBuilder.create(ID)
                .position(100, 100)
                .size(80, 60)
                .dockSide(DockSide.RIGHT)
                .automaticDocking(false)
                .addButton(button)
                .build();

        PanelBounds first = button.bounds(panel.layout(400, 300));
        panel.setHandlePosition(150, 135, 400, 300);
        PanelBounds second = button.bounds(panel.layout(400, 300));
        assertEquals(50, second.x() - first.x());
        assertEquals(35, second.y() - first.y());
    }

    @Test
    void buttonClickAndStatePersistenceAreConsumerControlled() {
        MemoryStore store = new MemoryStore();
        AtomicInteger clicks = new AtomicInteger();
        PanelButton button = PanelButton.builder(ResourceLocation.fromNamespaceAndPath("example", "action"))
                .onClick((panel, mouseButton) -> clicks.addAndGet(mouseButton + 1))
                .build();
        Panel first = PanelBuilder.create(ID).stateStore(store).addButton(button).build();
        first.setHandlePosition(33, 44, 300, 200);
        first.setDockSide(DockSide.BOTTOM);
        first.close();
        button.click(first, 1);

        Panel restored = PanelBuilder.create(ID).stateStore(store).build();
        assertEquals(2, clicks.get());
        assertEquals(33, restored.handleX());
        assertEquals(44, restored.handleY());
        assertEquals(DockSide.BOTTOM, restored.dockSide());
        assertFalse(restored.isExpanded());
    }

    private static final class MemoryStore implements PanelStateStore {
        private final Map<ResourceLocation, PanelState> states = new HashMap<>();
        @Override public Optional<PanelState> load(ResourceLocation panelId) { return Optional.ofNullable(states.get(panelId)); }
        @Override public void save(ResourceLocation panelId, PanelState state) { states.put(panelId, state); }
    }
}
