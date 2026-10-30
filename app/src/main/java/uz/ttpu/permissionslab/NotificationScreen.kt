package uz.ttpu.permissionslab

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val CHANNEL_ID = "permission_lab_notifications"

@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) sendTestNotification(context)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Notification permission")
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else if (hasPermission) {
                sendTestNotification(context)
            }
        }) {
            Text(if (hasPermission) "Send test notification" else "Request permission")
        }
    }
}

private fun sendTestNotification(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Permission lab",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("PermissionsLab")
        .setContentText("This is a test notification.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
    NotificationManagerCompat.from(context).notify(1, notification)
}
