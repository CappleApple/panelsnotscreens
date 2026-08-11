# Panels Not Screens developer API

All supported consumer types live under `com.cappleapple.panelsnotscreens.api`.

## Custom content

```java
PanelContent content = new PanelContent() {
    @Override
    public void render(PanelContext context, GuiGraphics graphics, int mouseX, int mouseY) {
        PanelBounds area = context.layout().content();
        // Drawing is automatically clipped to this rectangle.
        graphics.drawString(font, "Quest content", area.x() + 4, area.y() + 4, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(PanelContext context, double x, double y, int button) {
        return handleQuestClick(context.layout().content(), x, y, button);
    }
};
```

## Attached textured button

```java
PanelButton settings = PanelButton.builder(id("settings"))
    .anchor(PanelAnchor.RIGHT_EDGE)
    .offset(2, -20)
    .size(18, 18)
    .tooltip(Component.literal("Settings"))
    .textures(new PanelButtonTextures(
        id("settings"),
        id("settings_hovered"),
        id("settings_pressed"),
        id("settings_disabled")
    ))
    .visibleWhen(() -> playerMayConfigure())
    .enabledWhen(() -> !configurationLocked())
    .onClick((panel, mouseButton) -> openSettings(mouseButton))
    .keyboardShortcut(keyCode -> keyCode == GLFW.GLFW_KEY_P)
    .build();

panel.addButton(settings);
```

Anchored offsets may place a button outside the panel. Attached controls follow every drag and normally participate only while expanded; use `expandedOnly(false)` for a persistent tab.

## Textured or procedural rendering

Textures are GUI sprite `ResourceLocation` values, not hardcoded asset paths. Minecraft GUI sprite metadata can provide scalable/nine-slice behavior.

```java
Panel textured = PanelBuilder.create(id("spell_book"))
    .size(220, 180)
    .texturedStyle(new PanelTextureSet(
        id("spell_panel"),
        id("spell_frame"),
        id("spell_handle"),
        id("spell_handle_open"),
        id("spell_handle_hover"),
        id("spell_handle_pressed")
    ))
    .content(spellContent)
    .addButton(settings)
    .build();

Panel noAssetsRequired = PanelBuilder.create(id("machine_panel"))
    .proceduralStyle()
    .content(machineContent)
    .build();
```

## Docking

```java
panel.setDockSide(DockSide.BOTTOM);
panel.setAutomaticDocking(false); // the selected side remains authoritative while dragging
```

With automatic docking enabled, the panel selects left/right/top/bottom from the handle's screen position after the configured dead zone is crossed.

## Persistence

Persistence is opt-in and consumer-owned:

```java
PanelStateStore store = new MyConfigBackedPanelStateStore();

Panel panel = PanelBuilder.create(id("persistent_panel"))
    .stateStore(store)
    .position(defaultX, defaultY)
    .build();
```

The store receives the namespaced panel ID and a `PanelState` containing X/Y handle position, docking side, expanded state, and visibility. Use separate keys when state varies by screen type, player, server, or world.

## Multiple panels and z order

Create a `PanelManager` owned by your mod and route rendering and input through it. The add order
sets only the initial back-to-front order. Whenever a panel handles a click, release, scroll,
keyboard event, or typed character, the manager moves it to the front so the most recently
interacted-with panel is rendered on top.

Render depth is coordinated across all `Panel` instances loaded from the same library, even when
different mods own separate managers. The shared stack uses weak references and does not share
panel content or persisted state. For an interaction handled outside the panel input methods, call
`panel.bringToFront()` explicitly.

Tooltips rendered directly by panel content inherit the panel's render depth. When panel content
defers a tooltip to the owning screen, the library records which panel requested it and elevates the
tooltip above that panel. A background panel's tooltip remains below panels stacked in front of its
owner; a focused panel's tooltip renders above every panel.

Tooltips rendered immediately after `PanelManager.render(...)` are associated with the last panel
in that render batch under the same mouse coordinates. This supports consumer-managed tooltip
lists without requiring the tooltip call to occur inside `PanelContent.render(...)`.
