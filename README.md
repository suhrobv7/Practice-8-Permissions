# PermissionsLab

A small Compose app for the permissions practice. The screens show the network state, camera request states, overlay Settings flow, location result, and notification request.

## Open and run

Open this folder in Android Studio and let Gradle sync. Run the `app` configuration on an emulator or Android device. The project uses package name `uz.ttpu.permissionslab` and `minSdk 21`.

## Task 1

`Task1PermissionModel.kt` is the plain Kotlin model for Kotlin Playground. It makes the two denied requests, then tries another request and an allow. After two denials, later calls print `no dialog, denied immediately`.

## Screens

- Home: network state from `ConnectivityManager`; `INTERNET` and `ACCESS_NETWORK_STATE` are normal permissions, so there is no runtime request in the app.
- Camera: first request, rationale after a denial, and Settings after two denials. Returning from Settings refreshes the permission state.
- Overlay: opens the overlay Settings page and updates its switch on resume.
- Location: asks for precise and approximate location together and reads the `Map<String, Boolean>` result. It checks again on resume so a one-time grant does not stay displayed as granted after it is revoked.
- Notifications: asks for `POST_NOTIFICATIONS` only on API 33 or later and sends a small test notification after permission is available.

## Checks to do in Android Studio

The Android SDK and emulator were not available when these files were prepared, so device results and screenshots have not been recorded here. On an API 30+ emulator, try camera denial twice and then Settings. Also enable and disable overlay access in Settings, and try “Only this time” for location on a supported emulator.

For the network check, toggle airplane mode and observe the label. To test the manifest declaration, temporarily remove `INTERNET`, make a network request from a test, and restore the declaration. The home screen itself only reads connectivity state and does not make a network request.

For notifications, compare API 32 with API 34: API 32 does not show the Android 13 runtime notification prompt; API 34 does.

## Device observations

Fill in the API levels and actual results after running the project on your own emulators:

- API 30+: camera and overlay checks: ____________________
- One-time location test (API 30+): ____________________
- API 32 notification test: ____________________
- API 34 notification test: ____________________

Expected one-time location result: after choosing “Only this time”, the screen shows the granted location level while the app has access. When Android revokes that temporary grant, returning to the screen should refresh the label to “none”. Replace this expected result with what the emulator actually shows after testing.
