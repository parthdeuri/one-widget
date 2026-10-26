# one-widget

Customizable Android home-screen widgets, written in Kotlin with no third-party libraries. Every colour is yours to change, and each widget can open any app you choose when tapped.

## Widgets

- **Digital clock**: 12/24-hour time, optional seconds, date line and next-alarm line.
- **Analog clock**: redrawn every minute; colours for face, ticks, hour hand and minute hand, optional numbers.
- **Calendar**: month grid with ‹ › navigation, tap the title to jump back to today, optional Monday start.

- **Clock + calendar**: both together in one widget, side by side or stacked, either one first. Each side opens its own app when tapped.
- **Countdown**: days until (or since) a date you pick, with your own label.

The calendar can show a dot under days that have events (asks for the Calendar permission).

The app itself has a sakura look: a pink blossom header, rounded cards, and rose-coloured controls on every screen.

## Customization

Every widget has its own settings screen (opens when you place it; long-press a widget and choose Reconfigure on Android 12+, or use the list on the app's home screen):

- Colours for every element, with a hex / HSV / opacity picker.
- Theme presets: Dark, Light, Ocean, Sunset, Cherry blossom.
- Background: solid colour or gradient, opacity from 0 to 100% (fully transparent is allowed), corner roundness.
- Background patterns: dots, grid, stripes, waves, sparkles, sakura petals, with colour and strength.
- Background image from your gallery, with dimming so text stays readable.
- Tap action: pick any installed app to open when the widget is tapped. If none is set, tapping opens the widget's settings.
- All widgets are resizable; backgrounds are redrawn at the new size.

## Install

There is no local Android SDK setup. GitHub Actions builds the APK:

1. Open the repository's **Actions** tab and select the latest successful **Build APK** run.
2. Download the `widgets-debug-apk` artifact and unzip it.
3. Copy the APK to your phone and install it (allow installs from unknown sources when asked).
4. Long-press the home screen, open Widgets, and add one-widget widgets.

The app id is `com.parth.onewidget`. Builds from before the rename (`com.parth.widgets`) are a separate app and can be uninstalled.

## Project layout

```
app/src/main/java/com/parth/onewidget/
  providers/   AppWidgetProvider for each widget
  config/      settings screens, colour picker, app picker
  render/      bitmap renderers (analog clock, calendar, backgrounds)
  prefs/       per-widget settings storage and tap actions
app/src/main/res/   layouts, widget info XML, launcher icon and previews
.github/workflows/build.yml   CI build
```

Settings are stored in SharedPreferences keyed by widget id. Background images are copied (downscaled) into app storage, so no storage permission is needed.

## Build

CI runs `gradle assembleDebug` with Gradle 8.9, JDK 17, AGP 8.5.2 and Kotlin 1.9.24 (minSdk 26, targetSdk 34). On failure the workflow copies compiler errors into run annotations so they are readable without log access.

## Permissions

- `SCHEDULE_EXACT_ALARM`: lets the analog clock redraw exactly on the minute. Without it the hands may lag by a minute or two; the clock's settings screen links to the system toggle on Android 12+.
- `READ_CALENDAR`: only used for the optional event dots on the calendar widget, and only asked for from its settings screen.

## Known limitations

- The analog clock has no seconds hand; home-screen widgets cannot redraw every second cheaply.
- Minute updates keep running while the screen is off, because a widget cannot be told when the screen turns on.
