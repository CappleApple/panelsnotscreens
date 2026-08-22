package com.cappleapple.panelsnotscreens.api.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cappleapple.panelsnotscreens.api.widget.PanelButton;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PanelManagerTest {
    private static final int SCREEN_WIDTH = 400;
    private static final int SCREEN_HEIGHT = 300;

    @Test
    void floatingItemsRenderAbovePanelsWithoutExceedingTheGuiDepthRange() {
        assertTrue(PanelStack.floatingItemZ() > PanelStack.maxBaseZ());
        assertEquals(
                GuiGraphics.MAX_GUI_Z - PanelStack.GUI_DEPTH_HEADROOM,
                PanelStack.floatingItemZ() + PanelStack.FLOATING_ITEM_DECORATION_Z);
    }

    @Test
    void screenTooltipsReserveAForegroundLayerAboveEveryPanel() {
        Panel first = panel("first", 20, 20);
        Panel second = panel("second", 200, 20);
        manager(first, second);

        assertTrue(PanelStack.tooltipForegroundBaseZ() > PanelStack.maxBaseZ());
        assertTrue(PanelStack.tooltipForegroundBaseZ() > PanelStack.z(second));
        assertEquals(GuiGraphics.MAX_GUI_Z, PanelStack.tooltipMaxZ());
    }

    @Test
    void screenTooltipOffsetNormalizesAnyIncomingPoseToTheForegroundLayer() {
        Panel panel = panel("panel", 20, 20);
        float panelZ = PanelStack.z(panel);

        assertEquals(PanelStack.tooltipForegroundBaseZ(), PanelStack.tooltipForegroundOffset(0.0F));
        assertEquals(
                PanelStack.tooltipForegroundBaseZ(),
                panelZ + PanelStack.tooltipForegroundOffset(panelZ));
    }

    @Test
    void backgroundPanelTooltipStaysBelowTheNextActivePanel() {
        Panel background = panel("background", 20, 20);
        Panel foreground = panel("foreground", 20, 20);
        Screen screen = screen();
        PanelStack.activate(background, screen);
        PanelStack.activate(foreground, screen);

        assertTrue(PanelStack.panelTooltipMaxZ(background, screen) < PanelStack.z(foreground));
    }

    @Test
    void unhandledMouseInputFallsBackToTheTopmostPanel() {
        Panel background = panel("background_input", 20, 20);
        Panel foreground = panel("foreground_input", 20, 20);
        Screen screen = screen();
        PanelStack.activate(background, screen);
        PanelStack.activate(foreground, screen);
        boolean backgroundExpanded = background.isExpanded();
        boolean foregroundExpanded = foreground.isExpanded();

        assertSame(foreground, PanelStack.topmostAt(screen, 25, 25));
        PanelStack.MouseInputAttempt press = PanelStack.beginMousePress(screen, 25, 25);
        assertTrue(PanelStack.finishMousePress(press, screen, 25, 25, 0));
        PanelStack.MouseInputAttempt release = PanelStack.beginMouseRelease(screen, 0);
        assertTrue(PanelStack.finishMouseRelease(release, screen, 25, 25, 0));
        assertEquals(!foregroundExpanded, foreground.isExpanded());
        assertEquals(backgroundExpanded, background.isExpanded());
    }

    @Test
    void topmostPanelConsumesScrollBeforeUnderlyingGuiEvenWhenItsContentDoesNot() {
        Panel background = panel("background_scroll_capture", 20, 20);
        Panel foreground = panel("foreground_scroll_capture", 20, 20);
        Screen screen = screen();
        PanelStack.activate(background, screen);
        PanelStack.activate(foreground, screen);

        assertSame(foreground, PanelStack.topmostAt(screen, 25, 25));
        PanelStack.MouseInputAttempt scroll = PanelStack.beginMouseScroll(screen, 25, 25);
        assertTrue(PanelStack.finishMouseScroll(scroll, screen, 25, 25, 1));
    }

    @Test
    void directConsumerHooksCannotClaimAnOverlappedBackgroundPanel() {
        Panel background = panel("direct_background", 20, 20);
        Panel foreground = panel("direct_foreground", 20, 20);
        Screen screen = screen();
        PanelStack.activate(background, screen);
        PanelStack.activate(foreground, screen);

        assertFalse(background.mouseClicked(screen, 25, 25, 0));
        assertTrue(foreground.mouseClicked(screen, 25, 25, 0));
    }

    @Test
    void consumerHandledMouseInputIsNotAppliedTwiceByTheFallback() {
        AtomicInteger scrollCalls = new AtomicInteger();
        Panel panel = PanelBuilder.create(id("consumer_handled"))
                .position(20, 20)
                .size(80, 60)
                .dockSide(DockSide.RIGHT)
                .automaticDocking(false)
                .content(new PanelContent() {
                    @Override
                    public boolean mouseScrolled(PanelContext context, double mouseX, double mouseY, double amount) {
                        scrollCalls.incrementAndGet();
                        return true;
                    }
                })
                .build();
        PanelManager manager = manager(panel);
        Screen screen = screen();
        PanelStack.activate(panel, screen);
        boolean initiallyExpanded = panel.isExpanded();

        PanelStack.MouseInputAttempt press = PanelStack.beginMousePress(screen, 25, 25);
        assertTrue(manager.mouseClicked(screen, 25, 25, 0));
        assertTrue(PanelStack.finishMousePress(press, screen, 25, 25, 0));
        PanelStack.MouseInputAttempt release = PanelStack.beginMouseRelease(screen, 0);
        assertTrue(manager.mouseReleased(screen, 25, 25, 0));
        assertTrue(PanelStack.finishMouseRelease(release, screen, 25, 25, 0));
        assertEquals(!initiallyExpanded, panel.isExpanded());

        panel.open();
        PanelStack.MouseInputAttempt scroll = PanelStack.beginMouseScroll(screen, 50, 30);
        assertTrue(manager.mouseScrolled(screen, 50, 30, 1));
        assertTrue(PanelStack.finishMouseScroll(scroll, screen, 50, 30, 1));
        assertEquals(1, scrollCalls.get());
    }

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
        Panel tooltipOwner = panel("deferred_owner", 20, 20);
        Panel foreground = panel("deferred_foreground", 20, 20);
        Screen screen = screen();
        PanelStack.activate(tooltipOwner, screen);
        PanelStack.activate(foreground, screen);
        Object beforeTooltip = PanelStack.deferredTooltip(screen);
        screen.setTooltipForNextRenderPass(List.of(Component.literal("Owned tooltip").getVisualOrderText()));
        PanelStack.captureDeferredTooltipOwner(tooltipOwner, screen, beforeTooltip);

        Object beforeForeground = PanelStack.deferredTooltip(screen);
        PanelStack.captureDeferredTooltipOwner(foreground, screen, beforeForeground);

        assertSame(tooltipOwner, PanelStack.deferredTooltipOwner(screen));
        assertTrue(PanelStack.panelTooltipMaxZ(tooltipOwner, screen) < PanelStack.z(foreground));

        tooltipOwner.bringToFront();

        assertTrue(PanelStack.panelTooltipMaxZ(tooltipOwner, screen) > PanelStack.z(foreground));
    }

    @Test
    void immediateTooltipUsesTheTopmostPanelRegardlessOfRenderOrder() {
        Panel tooltipOwner = panel("immediate_owner", 20, 20);
        Panel foreground = panel("immediate_foreground", 20, 20);
        Screen screen = screen();
        PanelStack.activate(tooltipOwner, screen);
        PanelStack.activate(foreground, screen);

        PanelStack.beginRenderCollection();
        PanelStack.captureTooltipCandidate(foreground, screen, null, 25, 25);
        PanelStack.captureTooltipCandidate(tooltipOwner, screen, null, 25, 25);
        PanelStack.endRenderCollection();

        assertSame(foreground, PanelStack.immediateTooltipOwner(screen, null, 25, 25));
        assertTrue(PanelStack.panelTooltipMaxZ(tooltipOwner, screen) < PanelStack.z(foreground));
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
