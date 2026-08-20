# Changelog

All notable changes to Panels Not Screens are documented here.

## 1.1.6 - 2026-08-20

### Fixed

- Inventory, screen, JEI, and EMI tooltips outside panel-owned pointer areas now render above every
  panel and attached button.
- Panel-owned tooltips now render above their owning panel but below panels stacked in front, and
  covered background panels no longer show a competing tooltip.
- Hovering a panel suppresses tooltips from the underlying screen and ingredient overlays.
- Mouse presses, releases, and scrolling over a panel are now captured by the topmost panel before
  the screen, JEI, or EMI can handle them.
- Existing consumer `ScreenEvent` handlers remain authoritative, and the library fallback no longer
  bypasses owner capture state or applies the same input twice when multiple panel mods are loaded.
- Vanilla bundle tooltip items and Tooltip Overhaul's full styled depth range are reserved within
  the applicable foreground or panel layer without exceeding Minecraft's maximum GUI depth.

## 1.1.5 - 2026-08-13

### Fixed

- Items carried by the mouse, including items returning to a slot, now render in front of panels.

## 1.1.4 - 2026-08-12

### Fixed

- Tooltip depth now remains scoped to its owning panel even when another mod replaces and cancels
  Minecraft's tooltip renderer.
- Tooltip Overhaul's styled tooltip layers no longer split above and below focused panels, and its
  wider depth range is reserved between stacked panels.

## 1.1.3 - 2026-08-11

### Fixed

- Tooltips rendered immediately after a panel render now retain that panel as their owner, fixing
  focused-panel tooltips that regressed behind their own panel in `1.1.2`.

## 1.1.2 - 2026-08-11

### Fixed

- Deferred tooltips render above their owning panel. Tooltips from background panels remain below
  panels stacked in front, while tooltips from the focused panel render above every panel.

## 1.1.1 - 2026-08-11

### Fixed

- Deferred tooltips displayed over a focused panel are elevated above that panel instead of being
  hidden behind it.

## [1.1] - 2026-08-11

### Added

- Shared panel focus and render ordering across different mods, even when each mod owns a separate
  `PanelManager`.
- `Panel.bringToFront()` for interactions managed outside the library's normal input methods.
- `PanelManager` routing for key presses, key releases, and typed characters.
- Automated coverage for click, scroll, keyboard, and cross-manager stacking behavior.

### Changed

- The most recently interacted-with panel now renders above older panels instead of remaining in a
  fixed mod or registration order.
- Handled mouse releases, scrolling, keyboard events, and typed characters now also update panel
  focus.
- Panel render layers reserve enough depth for item icons, decorations, and tooltips so content from
  an older panel does not bleed through the focused panel.

### Fixed

- Fixed panels from one mod always appearing beneath panels from another mod after being clicked.

## [1.0] - 2026-08-10

- Initial release of the reusable NeoForge 1.21.1 draggable, dockable panel framework.

[1.1]: https://github.com/CappleApple/panelsnotscreens/compare/bb0853e...da45399
[1.0]: https://github.com/CappleApple/panelsnotscreens/commit/bb0853e
