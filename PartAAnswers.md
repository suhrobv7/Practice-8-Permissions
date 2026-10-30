# Part A. Theory answers

## 1. Permission workflow

First, check whether the feature can work without a permission. Then declare the permission in the manifest, check whether it is already granted, and request it from the user if it is needed and not granted. For example, an app can let the user choose a photo with the system photo picker instead of reading all photos itself. That is better because the app asks for less access.

## 2. Permission categories

- **Install-time:** normal permissions are granted automatically when the app is installed. Signature permissions are granted only when the app has the required signing certificate.
- **Runtime:** the app declares the permission and asks the user while it is running. It must also check the result and handle denial.
- **Special:** the user changes the permission in a Settings screen. The app opens that screen and checks the permission again when it returns.

The category matters because install-time permissions need little extra code, runtime permissions need a request and result handler, and special permissions need a Settings flow and a later re-check.

## 3. Normal permissions

`INTERNET` is a normal permission. Android grants it automatically, so there is no runtime dialog and no `checkSelfPermission` call is needed. If the manifest declaration is missing and the app tries to use the network, it can get a `SecurityException`. Examples of normal permissions are `INTERNET`, `ACCESS_NETWORK_STATE`, and `VIBRATE`.

## 4. Signature permission

A signature permission is normally granted only to apps signed with the same certificate as the app that declared the permission. The declaration uses `android:protectionLevel="signature"`. If Wallet has the matching signature, it can receive the permission and bind to the service. The unrelated third-party app cannot. Android does not show the user a permission dialog.

## 5. Install-time permissions

Install-time permissions are granted as part of installing the app, not from a dialog opened by the app while it is running. Older Android installers showed permission details during installation; the Play Store can also show permission information before installation. Normal and signature permissions are install-time permissions, but signature permissions have the extra signing-certificate rule.

## 6. Runtime permissions and older Android

Runtime permissions were introduced in Android 6.0 (API 23). On API 22 the runtime-permission system was not used, so a permission such as `CAMERA` was granted at installation. In the lecture's model, `granted(kind, sdk)` returns true for a runtime permission when `sdk < 23`; at API 23 and later it depends on the user's grant.

## 7. Denial states

1. **Not asked yet:** `shouldShowRationale` is false. Explain the feature briefly and make the first request.
2. **Denied once:** it is normally true. Explain why the permission is useful, then let the user try again.
3. **Denied twice:** it is normally false because Android will not show the request dialog again. Do not keep launching it; show an “Open Settings” action instead.

After two denials, send the user to the app's details page in Settings.

## 8. Accompanist example

- `isGranted`: show the camera feature or a message that permission is available.
- `shouldShowRationale`: explain why the app needs the camera and offer the request button.
- Otherwise: show that permission is missing and offer the first request button.

`rememberPermissionState` keeps its state through Compose recompositions, so the value does not reset every time the UI is redrawn. `@OptIn(ExperimentalPermissionsApi::class)` means the library API is marked experimental and may change.

## 9. Requesting without Accompanist

Use `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` and call `launch(Manifest.permission.CAMERA)` when the user presses the request button. Read the current grant with `ContextCompat.checkSelfPermission(context, permission)`. Use `ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)` to decide whether to explain the request. This uses AndroidX Activity Result APIs and does not need the Accompanist permission library.

## 10. Two location permissions

`RequestMultiplePermissions` returns a `Map<String, Boolean>`. Read each result with its permission name, for example `result[Manifest.permission.ACCESS_FINE_LOCATION] == true`. Android presents these related location requests together so the user can choose precise or approximate access. If both are denied, keep location features off and let the user continue without them.

## 11. Special permissions

Special permissions are not requested with the normal runtime dialog. The app opens the relevant Settings page and checks the result after the user returns.

| Permission | What it allows | Check call |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw windows over other apps | `Settings.canDrawOverlays(context)` |
| `WRITE_SETTINGS` | Change system settings | `Settings.System.canWrite(context)` |
| `MANAGE_EXTERNAL_STORAGE` | Broad access to shared storage | `Environment.isExternalStorageManager()` |
| `REQUEST_INSTALL_PACKAGES` | Request installation of other packages | `packageManager.canRequestPackageInstalls()` |
| `ACCESS_BACKGROUND_LOCATION` | Access location while the app is in the background | `ContextCompat.checkSelfPermission(context, permission)` |

Background location has a separate, restricted request flow on newer Android versions, even though its current grant is checked with `checkSelfPermission`.

## 12. Overlay permission and returning from Settings

Opening Settings does not send a permission result callback to the app. The user changes the setting in another screen, then returns to this screen. The app must read `Settings.canDrawOverlays(context)` again on `ON_RESUME`.

```kotlin
@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDraw by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDraw = Settings.canDrawOverlays(context)
    }

    Switch(
        checked = canDraw,
        onCheckedChange = {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        }
    )
}
```

## 13. Background location order

First request foreground location (`ACCESS_COARSE_LOCATION` and/or `ACCESS_FINE_LOCATION`). Only after the user grants foreground access, explain the background feature and request `ACCESS_BACKGROUND_LOCATION` separately. Android 10 (API 29) introduced this separate permission, so the second request is guarded with `SDK_INT >= Build.VERSION_CODES.Q`.

## 14. Permission groups

A permission group contains related permissions. Android may combine permissions from one group into a simpler user interface, so five requested permissions do not always make five dialogs. Examples are location, contacts, and camera. Group membership and dialog behavior can change between Android versions, so app code should check each permission itself instead of relying on a group.

## 15. Missing manifest declaration

If `CAMERA` is not declared, the app has not requested it correctly. The permission will not be granted, and trying to use protected camera APIs can fail with a `SecurityException`; the runtime request flow also cannot replace the missing manifest declaration. Install-time, runtime, and special permissions all need their applicable manifest declarations. Runtime permissions also need a request in code, while special permissions need a Settings flow. `android:maxSdkVersion` can limit an old permission such as `WRITE_EXTERNAL_STORAGE` to Android versions where the app still needs it; newer Android versions use other storage access methods.

## 16. Optional camera hardware

With `android:required="true"`, the Play Store can hide the app from devices without camera hardware. With `required="false"`, the app can be installed on those devices, but it should check `packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)` before showing camera features.

The four combinations are:

| Camera feature exists | `required` | Store result |
|---|---|---|
| Yes | `true` | Visible |
| No | `true` | Hidden |
| Yes | `false` | Visible |
| No | `false` | Visible |

## 17. One-time permission

On Android 11 (API 30) and later, a user can grant some permissions for one time. The grant can be revoked after the app is no longer being used, so a permission check done only once can become stale. Re-check it when the screen resumes, for example in `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)`, rather than caching “granted” forever.

## 18. Auto-reset and removing unused permissions

1. On Android 11 and later, Android can auto-reset permissions for an app that has not been used for a long time. The app should check permissions again when it is used and ask again only if the feature needs them. The user can manage the unused-app permission setting from the app's details in Settings; the app can also open that page with `Settings.ACTION_AUTO_REVOKE_PERMISSIONS` and a `package:` URI where the action is available.
2. On Android 13 (API 33), `revokeSelfPermissionOnKill(permission)` lets an app revoke a runtime permission it no longer needs. Android applies the change after stopping the app process. This reduces access to user data that the app does not need anymore.

## 19. Android version changes

- **API 23:** Android introduced runtime permissions, so apps must request some permissions while running.
- **API 30:** Android added one-time permission choices and automatic reset of permissions for apps that are not used.
- **API 33:** Android made `POST_NOTIFICATIONS` a runtime permission for notifications.

Request `POST_NOTIFICATIONS` only on API 33 and above. On earlier versions it is not a runtime permission, and asking for it by name is not supported there.
