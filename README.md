# Panels Not Screens

Panels Not Screens is an independent client-side NeoForge 1.21.1 library for movable, dockable panels layered over normal Minecraft screens. It contains no inventory assumptions and does not depend on Stacks Not Slots or Bundled Not Siloed.

The framework owns panel positioning, drag capture, docking, clipping, z order, attached controls, rendering chrome, and input routing. A consuming mod supplies the content.

## Features

- open, close, toggle, show, and hide operations;
- draggable handles and four expansion/docking sides;
- optional automatic side selection with configurable dead zones;
- authoritative manual docking when automatic docking is disabled;
- configurable panel, handle, padding, gap, margin, and min/max dimensions;
- consumer-owned persistence through namespaced panel IDs and `PanelStateStore`;
- procedural rendering matching the original inventory browser;
- resource-pack-compatible textured panel, handle, hover, pressed, and expanded states;
- GUI-sprite rendering compatible with Minecraft scalable/nine-slice sprite metadata;
- custom content with clipping and mouse/keyboard/scroll callbacks;
- attached buttons with anchors, offsets, conditions, tooltips, textures, click handlers, right-click values, and shortcuts;
- buttons outside panel bounds; and
- consumer-owned `PanelManager` instances for multiple mods/panels and z order.

## Minimal procedural panel

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

Render and route topmost input from the client hooks appropriate to your mod:

```java
panel.render(screen, graphics, mouseX, mouseY, partialTick);
if (panel.mouseClicked(screen, mouseX, mouseY, button)) cancelVanillaInput();
if (panel.mouseReleased(screen, mouseX, mouseY, button)) cancelVanillaInput();
if (panel.mouseScrolled(screen, mouseX, mouseY, amount)) cancelVanillaInput();
```

See [docs/API.md](docs/API.md) for textured panels, custom buttons, content, and persistence examples.

## Building

```powershell
./gradlew test build
```

The output is `build/libs/panelsnotscreens-1.0.jar`.

## License

MIT. Minecraft and NeoForge remain subject to their respective licenses.
