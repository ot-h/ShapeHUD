# ShapeHUD

**Your HUD, your way.** A client-side Fabric mod to move, resize, hide and group any vanilla HUD element with a drag & drop editor.

[![Modrinth](https://img.shields.io/modrinth/dt/shapehud?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/shapehud)

## Features

- 18 HUD elements: hotbar, health, armor, hunger, air, mount health, XP bar & level, held item name, action bar, crosshair, status effects, boss bar, scoreboard, chat, titles, player list and sound subtitles
- Drag to move, scroll or drag the corner handle to resize
- Multi-select with a selection box or Shift+Click, and groups that move/resize together
- Hide anything with one switch
- Live preview of elements that aren't on screen (chat, boss bar, title, scoreboard…)
- Alignment guides, grid snapping, undo (Ctrl+Z)
- English & Arabic interface with a bundled Arabic font

## Usage

Press **Left Alt + H** in-game (configurable), or open the settings from **Mod Menu**.

| Action | How |
|---|---|
| Select | Click · Shift+Click to add · Alt+Click ignores groups |
| Select many | Drag on empty space |
| Move | Drag · Arrow keys (Shift = 10px) |
| Resize | Mouse wheel · white corner handle · + / − |
| Group / Ungroup | G / U |
| Hide / Show | V or the switch in the list |
| Undo | Ctrl+Z |
| Preview | P |
| Hide panel | Tab |
| Save & close | Esc |

Settings are stored in `config/shapehud.json`.

## Requirements

- Minecraft 1.21.11
- Fabric Loader + [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional)

## Building

```
./gradlew build
```

The jar is written to `build/libs/`. Requires Java 21+.

## Reporting issues

Found a bug or have an idea? [Open an issue](../../issues/new/choose).

## License

MIT — see [LICENSE](LICENSE). The bundled [Cairo](https://github.com/Gue3bara/Cairo) font is licensed under the SIL Open Font License 1.1.
