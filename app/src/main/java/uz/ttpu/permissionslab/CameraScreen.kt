package uz.ttpu.permissionslab

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    var isGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    private fun Context.findActivity(): Activity? {
        var currentContext: Context = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return currentContext as? Activity
    }
    var deniedCount by rememberSaveable { mutableIntStateOf(0) }
    val shouldShowRationale = activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
    } ?: false

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isGranted = granted
        if (!granted) deniedCount += 1
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Camera permission")
        when {
            isGranted -> Text("Camera permission is granted.")
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->
                Text("Runtime permission is not needed on this Android version.")
            deniedCount >= 2 && !shouldShowRationale -> {
                Text("Permission was denied twice. Open Settings to allow it.")
                Button(onClick = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            }
            shouldShowRationale -> {
                Text("The app needs the camera to use this feature.")
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Request camera")
                }
            }
            else -> {
                Text("Camera permission has not been granted.")
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Request camera")
                }
            }
        }
    }
}
