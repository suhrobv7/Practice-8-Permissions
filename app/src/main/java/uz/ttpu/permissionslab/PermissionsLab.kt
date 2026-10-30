package uz.ttpu.permissionslab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class LabPage {
    HOME,
    CAMERA,
    OVERLAY,
    LOCATION,
    NOTIFICATIONS
}

@Composable
fun PermissionsLab() {
    var pageName by rememberSaveable { mutableStateOf(LabPage.HOME.name) }
    val page = LabPage.valueOf(pageName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (page != LabPage.HOME) {
            OutlinedButton(onClick = { pageName = LabPage.HOME.name }) {
                Text("Home")
            }
        }

        when (page) {
            LabPage.HOME -> {
                Text("PermissionsLab")
                Text("Choose a small example to try.")
                // These are normal permissions, so Android does not show a runtime dialog.
                NetworkState()
                PageButton("Camera permission") { pageName = LabPage.CAMERA.name }
                PageButton("Overlay permission") { pageName = LabPage.OVERLAY.name }
                PageButton("Location permission") { pageName = LabPage.LOCATION.name }
                PageButton("Notification permission") { pageName = LabPage.NOTIFICATIONS.name }
            }

            LabPage.CAMERA -> CameraScreen()
            LabPage.OVERLAY -> OverlayScreen()
            LabPage.LOCATION -> LocationScreen()
            LabPage.NOTIFICATIONS -> NotificationScreen()
        }
    }
}

@Composable
private fun PageButton(label: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(label)
    }
}
