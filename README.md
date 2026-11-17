# MiniTimer lite

An offline tally counter for Android 8.0 or later and iOS 17 or later. The app follows the device language and supports English, Spanish, and Brazilian Portuguese. Other device languages use English. Saved session and counter names stay as entered.

## Android

Open the project folder in Android Studio and select `composeApp`. Use JDK 21, Android SDK 36, and Android SDK Build-Tools 36.0.0. To build the debug APK and run the unit tests:

```sh
./gradlew :composeApp:assembleDebug :composeApp:testDebugUnitTest
```

The debug APK is generated at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

## iOS

Open `iosApp/MiniTimer.xcodeproj` in Xcode and run the **MiniTimer** scheme on an Apple Silicon simulator. Xcode calls Gradle to build the shared framework. Set up JDK 21 and Android SDK 36 for Gradle. If you launch Xcode from Finder, make JDK 21 available to `/usr/libexec/java_home` or launch Xcode from an environment with `JAVA_HOME` set. To install on an iPhone, select your signing team in the MiniTimer target.

## Using the app

Tap **Start** and create a session. The form suggests a name and starts with **Counter 1** active at zero. Save right away or add more counters. In **Edit session**, you can change counter names, order, and whether they contribute to the total. Turn on **Active** for the counters you want to see.

The bottom bar counts for the selected counter. Tap a name in the list or use the bar selector to switch. **Export** is in the session header.

On Android, the volume buttons add or subtract one from the first active counter. The pencil beside each counter lets you edit its value or reset it in the same dialog. Changes are saved on the device. **Export** copies the total on the first line, followed by each counter.

## Implementation

### Shared code

[composeApp/src/commonMain](composeApp/src/commonMain) contains the Compose Multiplatform interface, themes, localized strings, and counting rules. Both platforms launch `ContadoresApp`. Each platform provides storage through `NotebookStore` and access to its clipboard.

In [Notebook.kt](composeApp/src/commonMain/kotlin/app/contadores/Notebook.kt), `Notebook` holds the sessions and selected theme. Each `Session` keeps its counters in order and calculates the total from active counters included in it. `NotebookStore` persists changes before publishing the new state to Compose. If a write fails, the UI keeps the last saved state and shows an error.

**Export** formats the text in shared Kotlin code. The first line is `total - value`, followed by one line per counter in session order. Each platform copies the resulting text through its native clipboard API.

The [Compose string resources](composeApp/src/commonMain/composeResources) provide English defaults and Spanish and Portuguese translations. Compose resolves them from the system locale on each platform. Titles saved as session data keep their original text.

### Android

[MainActivity.kt](composeApp/src/androidMain/kotlin/app/contadores/MainActivity.kt) mounts the shared interface with `setContent`. Its `AndroidViewModel` reads `notebook.json` from the app’s private storage. To save changes, it writes a temporary file and atomically replaces the previous file.

The activity copies text with `ClipboardManager`. It also directs volume button presses to the first active counter when the counting screen is in the foreground and the device is unlocked.

### iOS

[AppDelegate.swift](iosApp/AppDelegate.swift) creates the window with UIKit. The Kotlin bridge in [MiniTimerHost.kt](composeApp/src/iosMain/kotlin/app/contadores/MiniTimerHost.kt) mounts the shared interface with `ComposeUIViewController`. Swift reads `notebook.json` from **Application Support** and writes changes atomically. It copies text with `UIPasteboard`. Use the on-screen buttons to count.

The Xcode project runs `:composeApp:embedAndSignAppleFrameworkForXcode` in a build phase. Gradle builds `MiniTimerShared` for an iPhone or an ARM64 simulator and packages Compose resources, including fonts.

## Documentation

- [Functional specification](SPECIFICATION.md)
- [Visual design](DESIGN.md)
