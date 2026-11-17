# MiniTimer lite visual design

Cuaderno is the light theme and Panel nocturno is the dark theme. Both share the same layout, controls, and hierarchy.

## Themes

| Theme | Palette |
| --- | --- |
| Cuaderno | Cream `#F8F3E8`, ink `#252823`, pine green `#285647`, beige `#EDE4D4` |
| Panel nocturno | Graphite blue `#121823`, panel blue `#202B3D`, cool white `#EDF3FF`, lime `#B6EF73` |

Both themes use Instrument Serif for headings and Inter for text and controls. The light theme uses Inter for tabular numbers, and the dark theme uses IBM Plex Mono for numbers. The app bundles these fonts. The offline HTML mockup uses system fonts.

The app starts in the light theme. The moon icon switches to dark, and the sun icon switches to light. The device saves the theme choice. Switching themes preserves the current screen, counter values, and session settings.

## Screen layout and controls

- The welcome screen uses the selected theme, with the logo and name near the top and **Start** at the bottom.
- The sessions list shows each session’s name first, followed by its optional description.
- The counting screen aligns the values and highlights the selected counter. The design aims to show three rows without scrolling on a typical phone. The list scrolls when more rows are active than can fit. The layout should also be checked with enlarged text.
- The target minimum touch area for buttons is 48 dp. The add button has a solid fill, and the subtract button has a visible outline.
- On Android, a short label beside the first counter indicates that the volume buttons control it. The label follows the first counter when the order changes.
- The total, when shown, has its own band and is visually distinct from editable values. Set each counter’s inclusion when editing the session. The mockup exposes that setting in each row so you can try the calculation.
- The pencil beside the session name opens **Edit session**, where you can change the name and add, rename, reorder, or deactivate counters. Deactivating a counter preserves its value and removes it from the total. When several counters are active, each row has a handle you can press and hold to drag it into position.
- The pencil beside each counter opens the value editor, which also offers **Reset**. Edit, reset, and delete are secondary actions so the counting controls have room.
- Labels follow the device language in English, Spanish, or Portuguese. The Portuguese translation uses Brazilian wording. English is the fallback, and saved names stay as entered.

## References

- Flighty informed the hierarchy for information that needs to be read at a glance. [Behind the Design: Flighty](https://developer.apple.com/news/?id=970ncww4).
- Tally informed the approach to grouping counters and editing their values. [Tally on the App Store](https://apps.apple.com/us/app/tally-quick-counter/id957912407).
- Roman Tsukanov’s Counter informed touch and volume button counting on Android. [Counter on F-Droid](https://f-droid.org/packages/me.tsukanov.counter/).

## App icon

The launcher icon uses cream tally marks on a pine green background. Android
uses an adaptive icon with a monochrome layer for supported themed launchers.
iOS uses the 1024 × 1024 image in the AppIcon asset catalog.

## One-handed counting

The session header contains **Export**. The bottom bar keeps the selected
counter, its value, and the **−1** and **+1** buttons within thumb reach.
Both buttons have the same width. Tap a row name
or the bar selector to switch counters. The list scrolls above the bar.
