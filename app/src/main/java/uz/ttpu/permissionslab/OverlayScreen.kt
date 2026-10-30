package uz.ttpu.permissionslab

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun OverlayScreen() {
    val context = LocalContext.current
    var canDraw by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canDraw = Settings.canDrawOverlays(context)
    }

    Row {
        Text("Allow display over other apps")
        Switch(
            checked = canDraw,
            onCheckedChange = { openOverlaySettings(context) }
        )
    }
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}
