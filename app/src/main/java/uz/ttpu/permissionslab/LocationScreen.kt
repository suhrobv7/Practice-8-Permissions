package uz.ttpu.permissionslab

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun LocationScreen() {
    val context = LocalContext.current
    var fineGranted by remember {
        mutableStateOf(hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    var coarseGranted by remember {
        mutableStateOf(hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        fineGranted = hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        coarseGranted = hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Location permission: ${locationLabel(fineGranted, coarseGranted)}")
        Button(onClick = {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }) {
            Text("Request location")
        }
    }
}

private fun hasPermission(context: android.content.Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) ==
        PackageManager.PERMISSION_GRANTED
}

private fun locationLabel(fineGranted: Boolean, coarseGranted: Boolean): String {
    return when {
        fineGranted -> "precise"
        coarseGranted -> "approximate"
        else -> "none"
    }
}
