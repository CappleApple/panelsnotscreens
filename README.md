# Panels Not Screens

Panels Not Screens is an independent client-side UI library for NeoForge 1.21.1. It allows mods to add movable, dockable panels on top of normal Minecraft screens without replacing the screen itself.

The library only handles the panel framework. It makes no assumptions about inventories or the type of information being displayed, and it does not depend on Stacks Not Slots or Bundled Not Siloed. A consuming mod provides the actual panel content while Panels Not Screens handles positioning, rendering, interaction, and layout behavior.

The framework manages panel movement, drag capture, docking, clipping, z-order, attached controls, visual chrome, and input routing.

## Features

Panels can be opened, closed, shown, hidden, or toggled as needed.

The framework also supports:

* Draggable panel handles.
* Expansion and docking from the left, right, top, or bottom.
* Optional automatic docking based on panel position.
* Configurable dead zones to prevent unwanted automatic side changes.
* Manually controlled docking when automatic docking is disabled.
* Configurable panel size, handle size, padding, gaps, margins, and minimum or maximum dimensions.
* Persistent panel state through namespaced panel IDs and `PanelStateStore`, while leaving storage ownership to the consuming mod.
* Procedural panel rendering matching the style originally developed for the inventory browser.
* Fully textured panels with separate panel, handle, hover, pressed, and expanded states.
* Resource-pack-compatible textures.
* Minecraft GUI sprite rendering, including scalable and nine-slice sprite metadata.
* Custom panel content with automatic clipping.
* Mouse, keyboard, and scroll callbacks for custom content.
* Attached buttons and controls.
* Button anchors and positional offsets.
* Conditional button visibility or availability.
* Tooltips, custom textures, click handlers, right-click values, and keyboard shortcuts.
* Controls positioned outside the normal panel bounds.
* Consumer-owned `PanelManager` instances, allowing multiple mods or independent panel systems to coexist while maintaining their own z-order.

## Minimal Procedural Panel

A basic panel can be created with `PanelBuilder`:

```java
Panel panel = PanelBuilder.create(ResourceLocation.fromNamespaceAndPath("example", "quest_panel"))
    .size(180, 140)
    .position(20, 30)
    .dockSide(DockSide.RIGHT)
    .automaticDocking(false)
    .proceduralStyle()
    .content(new QuestPanelContent())
    .build();
```

Here, the consuming mod defines the panel's identity, initial dimensions and position, docking behavior, visual style, and content. The framework handles the surrounding panel behavior.

The panel can then be rendered and given input from whichever client hooks are appropriate for the consuming mod:

```java
panel.render(screen, graphics, mouseX, mouseY, partialTick);

if (panel.mouseClicked(screen, mouseX, mouseY, button)) {
    cancelVanillaInput();
}

if (panel.mouseReleased(screen, mouseX, mouseY, button)) {
    cancelVanillaInput();
}

if (panel.mouseScrolled(screen, mouseX, mouseY, amount)) {
    cancelVanillaInput();
}
```

Input methods report whether the panel consumed the interaction. This allows the consuming mod to prevent the underlying Minecraft screen from handling the same input when necessary.

Existing `ScreenEvent` mouse handlers remain authoritative. Panels Not Screens tracks the globally topmost panel before those handlers run and applies a final fallback only when the owner did not handle the event, preventing the screen or JEI/EMI from claiming the same pixels. Route keyboard and character input through the panel or a `PanelManager` when your content uses those callbacks.

See [docs/API.md](docs/API.md) for complete examples covering textured panels, custom content, attached buttons, persistence, and other configuration options.

## Building

Build and run the test suite with:

```powershell
./gradlew test build
```

The resulting library JAR is written to:

```text
build/libs/panelsnotscreens-1.1.6.jar
```

## License

Panels Not Screens is licensed under the MIT License.

Minecraft and NeoForge remain subject to their respective licenses.
