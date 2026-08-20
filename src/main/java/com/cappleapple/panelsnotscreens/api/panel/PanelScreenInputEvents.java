package com.cappleapple.panelsnotscreens.api.panel;

import java.util.WeakHashMap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Lets panel owners process screen input normally, then claims any still-unhandled topmost panel
 * before the underlying screen or ingredient overlay can receive it.
 */
@ApiStatus.Internal
public final class PanelScreenInputEvents {
    private static final WeakHashMap<Object, PanelStack.MouseInputAttempt> ATTEMPTS = new WeakHashMap<>();

    private PanelScreenInputEvents() { }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beginMousePress(ScreenEvent.MouseButtonPressed.Pre event) {
        remember(event, PanelStack.beginMousePress(event.getScreen(), event.getMouseX(), event.getMouseY()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void finishMousePress(ScreenEvent.MouseButtonPressed.Pre event) {
        PanelStack.MouseInputAttempt attempt = forget(event);
        if (PanelStack.finishMousePress(
                attempt, event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beginMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        remember(event, PanelStack.beginMouseRelease(event.getScreen(), event.getButton()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void finishMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        PanelStack.MouseInputAttempt attempt = forget(event);
        if (PanelStack.finishMouseRelease(
                attempt, event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beginMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        remember(event, PanelStack.beginMouseScroll(event.getScreen(), event.getMouseX(), event.getMouseY()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void finishMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        PanelStack.MouseInputAttempt attempt = forget(event);
        if (PanelStack.finishMouseScroll(
                attempt, event.getScreen(), event.getMouseX(), event.getMouseY(), event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    private static synchronized void remember(Object event, PanelStack.MouseInputAttempt attempt) {
        if (attempt != null) ATTEMPTS.put(event, attempt);
    }

    private static synchronized PanelStack.MouseInputAttempt forget(Object event) {
        return ATTEMPTS.remove(event);
    }
}
