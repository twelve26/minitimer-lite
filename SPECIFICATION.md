# MiniTimer lite for Android and iOS

Functional specification v0.16.

## Purpose

Record counts as you go and resume them later. For example, a church can use a session for each service to track attendance, staff, and first-time visitors together.

## Sessions

- The welcome screen appears when you open the app. **Start** opens the saved sessions list, where each row shows the session name and its description if provided.
- You can create, open, edit, and delete sessions from the list. A session has a name and an optional description. A new session suggests an available name, such as **Session 1**, and starts with **Counter 1** active at zero. You can save the form with those defaults. Each session must have at least one counter. New counters are active. The **Active** setting appears when you edit a saved session.
- You can create, edit, delete, and reorder counters within a session. Each counter has a name and an integer value.
- The app imposes no limit on the number of sessions or saved or active counters in a session. The list scrolls when the active counters exceed the available space.
- The app saves the active counter selection and order for each session. The first active counter appears at the top. When multiple counters are active, press and hold a counter’s handle on the counting screen and drag it to its new position.
- The pencil beside the session name opens **Edit session**. From this screen, you can edit session details, add, rename, reorder, and delete counters, and choose which counters are active. Deactivating a counter preserves its value.

## Counting

- New counters start at zero. The bottom bar adds or subtracts one from the selected counter.
- On Android, when the counting screen is visible and the device is unlocked, volume up adds one to the first active counter and volume down subtracts one. The volume buttons retain their normal behavior on other screens and when the device is locked.
- The pencil beside a counter name opens its value editor. The app accepts integers only.
- Each session has a setting to allow negative values. It is off by default. With the setting off, subtracting from zero leaves the counter at zero, and the value editor rejects negative numbers.
- To turn off negative values when a session contains a negative count, correct those counts first. The app explains the validation error and preserves the current values.
- In the value editor, **Reset** returns the counter to zero after confirmation.
- Deleting a counter or session requires confirmation. Deleting a session also deletes its counters.

The counting bar stays at the bottom of the session. It shows the selected
counter and its current value, with a wider **+1** button beside **−1**.
The first active counter is selected when you open the session. Tap a name
in the list or use the selector in the bar to switch. Android's volume buttons
continue to control the first active counter in the session order.

## Local storage

The app automatically saves each change on the device. If you close the app with a counter at 27, its value is still 27 when you reopen the session. You can create, edit, count, and delete offline. The app requires no account.

## Export

**Export** appears in the session header. It copies all counter names and values to the clipboard in session order. The first line contains the calculated total in `total - value` format. Each counter occupies its own line in `name - value` format. The export includes inactive counters and counters excluded from the total.

## Session total

You decide what each counter represents and which ones contribute to the total. Each counting action changes only the counter you use.

- Each session can show a **Total** field. It adds the values of the active counters included in it.
- New counters contribute by default. You can turn **Include in total** off and on for any counter. Excluding a counter preserves its value.
- **Total** has a fixed label and is read-only. It recalculates when counter values, active status, or inclusion changes. It shows zero when no active counters are included.
- The device stores the total visibility setting and which counters contribute.

## Appearance

The light theme is called Cuaderno and the dark theme is called Panel nocturno. Both use the same layout. The app starts in the light theme. Tap the moon to switch to dark or the sun to switch to light. The device stores your choice.

## Language

The interface follows the device language and supports English, Spanish, and Portuguese using Brazilian Portuguese wording. It falls back to English for other languages. Changing the device language preserves saved session names, counter names, and descriptions.

## Platforms

The project uses Kotlin Multiplatform and Compose Multiplatform. Android and iOS share the screens, themes, total calculation, and export format. Each platform saves data in its private storage. Android supports the physical volume buttons. iOS uses the on-screen counting buttons. The public Apple interfaces documented for physical button presses in this context are for camera capture. [Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/supported-platforms.html) · [Apple camera capture controls](https://developer.apple.com/videos/play/wwdc2025/253/)

## Acceptance criteria

- In airplane mode, create a session with a description and three named counters. Close and reopen the app and verify that the session and all three counters are still there.
- On Android, with three active counters, select the second counter and tap a button in the counting bar. Only that counter changes. Move it to the first position and verify that the volume buttons now change it.
- On Android, verify that the volume buttons do not change any counts on the sessions list or when the screen is locked.
- Activate four or more counters in one session and scroll to reach them. Reopen the session and verify that their active status and order persist.
- With negatives off, subtract one from zero and verify the value stays at zero. With negatives on, verify the same action produces −1. Verify the value editor rejects decimals in both cases.
- Set a value to 27, close the app, and reopen the session. Verify the value is still 27. Confirm a reset and verify it returns to zero.
- Confirm a session deletion and verify it disappears from the sessions list. Reopen the app and verify its counters no longer appear.
- Set the three active and included counters to 80, 10, and 5. The total shows 95. Exclude the counter with value 10. The total shows 85 and that counter still shows 10. Leave only the counter with value 80 active. The total shows 80.
- Switch between light and dark with a session open. The screen, counts, and total inclusion stay intact. Reopen the app and verify the theme choice persists.
- Rename a session and a counter and reopen the app. Verify the new names persist and the read-only field still shows the **Total** label.
- Reduce the number of active counters from three to two in **Edit session**. Reactivate the third counter and verify it retains its value and name.
- Tap **Start**, create a session, and save the form without changes. The session opens with **Counter 1** active at zero.
- Set the device language to English, Spanish, or Portuguese and verify the interface uses the corresponding language. Switch to an unsupported language and verify it uses English. Saved names and descriptions persist across language changes.

- Open a session with several active counters. Verify the bottom bar starts on the first. Select another counter and verify the bar changes only that counter. Scroll and verify the bar stays visible. On Android, verify the volume buttons still change the first active counter.
