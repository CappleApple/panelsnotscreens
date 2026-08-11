package com.cappleapple.panelsnotscreens.api.panel;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PanelManagerTest {
    private static final int SCREEN_WIDTH = 400;
    private static final int SCREEN_HEIGHT = 300;

    @Test
    void clickedPanelBecomesTopmost() {
        Panel first = panel("first", 20, 20);
        Panel second = panel("second", 200, 20);
        PanelManager manager = manager(first, second);

        assertTrue(manager.mouseClicked(screen(), 25, 25, 0));

        assertSame(first, topmost(manager));
    }

    @Test
    void scrolledPanelBecomesTopmost() {
        Panel first = panel("first", 20, 20);
        Panel second = panel("second", 200, 20);
        PanelManager manager = manager(first, second);

        assertTrue(manager.mouseScrolled(screen(), 50, 30, 1));

        assertSame(first, topmost(manager));
    }

    @Test
    void panelHandlingKeyboardInputBecomesTopmost() {
        PanelButton shortcut = PanelButton.builder(id("shortcut"))
                .keyboardShortcut(keyCode -> keyCode == 32)
                .build();
        Panel first = PanelBuilder.create(id("first")).addButton(shortcut).build();
        Panel second = PanelBuilder.create(id("second")).build();
        PanelManager manager = manager(first, second);

        assertTrue(manager.keyPressed(screen(), 32, 0, 0));

        assertSame(first, topmost(manager));
    }

    @Test
    void interactionUpdatesSharedStackAcrossDifferentManagers() {
        Panel first = panel("first", 20, 20);
        Panel second = panel("second", 200, 20);
        PanelManager firstManager = manager(first);
        manager(second);
        Screen screen = screen();
        assertTrue(PanelStack.z(second) > PanelStack.z(first));

        assertTrue(firstManager.mouseClicked(screen, 25, 25, 0));

        assertTrue(PanelStack.z(first) > PanelStack.z(second));
    }

    @Test
    void deferredTooltipKeepsThePanelThatRequestedItAsOwner() {
        Panel tooltipOwner = panel("owner", 20, 20);
        Panel focused = panel("focused", 20, 20);
        Screen screen = screen();
        Object beforeTooltip = PanelStack.deferredTooltip(screen);
        screen.setTooltipForNextRenderPass(List.of(Component.literal("Owned tooltip").getVisualOrderText()));
        PanelStack.captureDeferredTooltipOwner(tooltipOwner, screen, beforeTooltip);

        Object beforeFocusedPanel = PanelStack.deferredTooltip(screen);
        PanelStack.captureDeferredTooltipOwner(focused, screen, beforeFocusedPanel);
        focused.bringToFront();

        assertSame(tooltipOwner, PanelStack.deferredTooltipOwner(screen));
        assertTrue(PanelStack.z(focused) > PanelStack.z(tooltipOwner));
        assertTrue(PanelStack.tooltipZ(tooltipOwner) < PanelStack.z(focused));

        tooltipOwner.bringToFront();

        assertTrue(PanelStack.tooltipZ(tooltipOwner) > PanelStack.z(focused));
    }

    @Test
    void immediateTooltipKeepsThePanelRenderedUnderThePointerAsOwner() {
        Panel tooltipOwner = panel("owner", 20, 20);
        Panel focused = panel("focused", 200, 20);
        Screen screen = screen();
        focused.bringToFront();

        PanelStack.beginRenderCollection();
        PanelStack.captureTooltipCandidate(tooltipOwner, screen, null, 25, 25);
        PanelStack.endRenderCollection();

        assertSame(tooltipOwner, PanelStack.immediateTooltipOwner(screen, null, 25, 25));
        assertTrue(PanelStack.tooltipZ(tooltipOwner) < PanelStack.z(focused));
    }

    private static Panel panel(String path, int x, int y) {
        return PanelBuilder.create(id(path))
                .position(x, y)
                .size(80, 60)
                .dockSide(DockSide.RIGHT)
                .automaticDocking(false)
                .build();
    }

    private static PanelManager manager(Panel... panels) {
        PanelManager manager = new PanelManager();
        for (Panel panel : panels) manager.add(panel);
        return manager;
    }

    private static Panel topmost(PanelManager manager) {
        return manager.panels().get(manager.panels().size() - 1);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }

    private static Screen screen() {
        return new TestScreen(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private static final class TestScreen extends Screen {
        private TestScreen(int width, int height) {
            super(Component.empty());
            this.width = width;
            this.height = height;
        }
    }
}
